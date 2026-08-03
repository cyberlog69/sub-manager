package com.submanager.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.submanager.app.data.local.AppDatabase
import com.submanager.app.data.local.PaymentHistoryEntity
import com.submanager.app.data.local.SubscriptionEntity
import com.submanager.app.data.model.BillingCycle
import com.submanager.app.data.model.DiscoveredItem
import com.submanager.app.data.model.ProviderType
import com.submanager.app.data.model.SubscriptionCategory
import com.submanager.app.data.model.SubscriptionStatus
import com.submanager.app.data.repository.SubscriptionRepository
import com.submanager.app.engine.EmailReceiptParserEngine
import com.submanager.app.engine.SmsParserEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

data class SpendAnalytics(
    val totalMonthlySpend: Double,
    val totalYearlySpend: Double,
    val activeCount: Int,
    val upcomingCount7Days: Int,
    val spendByCategory: Map<SubscriptionCategory, Double>
)

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SubscriptionRepository
    val subscriptions: StateFlow<List<SubscriptionEntity>>

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<SubscriptionCategory?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _discoveredItems = MutableStateFlow<List<DiscoveredItem>>(emptyList())
    val discoveredItems = _discoveredItems.asStateFlow()

    private val _userCurrency = MutableStateFlow("INR")
    val userCurrency = _userCurrency.asStateFlow()

    val filteredSubscriptions: StateFlow<List<SubscriptionEntity>>
    val analytics: StateFlow<SpendAnalytics>

    init {
        val dao = AppDatabase.getDatabase(application).subscriptionDao()
        repository = SubscriptionRepository(dao)
        subscriptions = repository.allSubscriptions.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        filteredSubscriptions = combine(subscriptions, _searchQuery, _selectedCategory) { list, query, category ->
            list.filter { sub ->
                val matchesQuery = query.isBlank() || sub.name.contains(query, ignoreCase = true) || sub.notes.contains(query, ignoreCase = true)
                val matchesCategory = category == null || sub.category == category.name
                matchesQuery && matchesCategory
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        analytics = subscriptions.combine(_userCurrency) { list, _ ->
            calculateAnalytics(list)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SpendAnalytics(0.0, 0.0, 0, 0, emptyMap()))

        // Populate mock data if DB is empty on first launch
        viewModelScope.launch {
            repository.allSubscriptions.collect { currentList ->
                if (currentList.isEmpty()) {
                    seedDefaultData()
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: SubscriptionCategory?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    fun setCurrency(currency: String) {
        _userCurrency.value = currency
    }

    fun getPaymentHistoryForSubscription(subId: Long): Flow<List<PaymentHistoryEntity>> {
        return repository.getHistoryForSubscription(subId)
    }

    /**
     * Smart Add / Deduplication logic:
     * If a subscription with the same service name already exists, update it and add a new entry to PaymentHistory!
     */
    fun addSubscription(
        name: String,
        amount: Double,
        currency: String,
        billingCycle: BillingCycle,
        category: SubscriptionCategory,
        providerType: ProviderType = ProviderType.MANUAL,
        notes: String = "",
        paymentMethod: String = "UPI / Card",
        nextDueDate: Date = getDefaultDueDate(),
        rawSnippet: String = ""
    ) {
        viewModelScope.launch {
            val existing = repository.findByName(name.trim())
            if (existing != null) {
                // Update existing subscription instead of creating a duplicate
                val updated = existing.copy(
                    amount = amount,
                    currency = currency,
                    billingCycle = billingCycle.name,
                    category = category.name,
                    providerType = providerType.name,
                    nextDueDate = nextDueDate.time,
                    notes = if (notes.isNotBlank()) notes else existing.notes
                )
                repository.updateSubscription(updated)

                // Insert payment history log
                repository.insertPaymentHistory(
                    PaymentHistoryEntity(
                        subscriptionId = existing.id,
                        serviceName = existing.name,
                        amount = amount,
                        currency = currency,
                        paymentDate = System.currentTimeMillis(),
                        source = providerType.name,
                        rawSnippet = rawSnippet.ifBlank { "Recorded payment charge" }
                    )
                )
            } else {
                // Insert new subscription
                val entity = SubscriptionEntity(
                    name = name.trim(),
                    amount = amount,
                    currency = currency,
                    billingCycle = billingCycle.name,
                    category = category.name,
                    providerType = providerType.name,
                    status = SubscriptionStatus.ACTIVE.name,
                    startDate = System.currentTimeMillis(),
                    nextDueDate = nextDueDate.time,
                    notes = notes,
                    paymentMethod = paymentMethod,
                    iconName = category.iconName
                )
                val newId = repository.insertSubscription(entity)

                // Record initial payment history
                repository.insertPaymentHistory(
                    PaymentHistoryEntity(
                        subscriptionId = newId,
                        serviceName = name.trim(),
                        amount = amount,
                        currency = currency,
                        paymentDate = System.currentTimeMillis(),
                        source = providerType.name,
                        rawSnippet = rawSnippet.ifBlank { "Initial detected payment" }
                    )
                )
            }
        }
    }

    fun updateSubscription(entity: SubscriptionEntity) {
        viewModelScope.launch {
            repository.updateSubscription(entity)
        }
    }

    fun deleteSubscription(entity: SubscriptionEntity) {
        viewModelScope.launch {
            repository.deleteSubscription(entity)
        }
    }

    fun scanSampleSmsMessages() {
        val sampleSmsList = listOf(
            "AXIS BANK" to "Alert: INR 649.00 debited from a/c xx8912 on 03-AUG for Netflix Monthly Plan. Auto-renew active.",
            "HDFC BANK" to "INR 179.00 debited for Spotify Subscription via UPI mandate. Ref: 489128.",
            "ICICI BANK" to "Dear customer, Rs 1499.00 debited for Amazon Prime Annual Membership auto-renew.",
            "SBI BANK" to "Alert: USD 20.00 debited for ChatGPT Plus subscription monthly billing.",
            "AIRTEL" to "Rs 999 debited for Airtel Xstream Broadband monthly plan renewal."
        )

        val found = SmsParserEngine.scanSampleSmsList(sampleSmsList)
        _discoveredItems.value = (_discoveredItems.value + found).distinctBy { it.serviceName }
    }

    fun parseManualSmsInput(smsBody: String) {
        val parsed = SmsParserEngine.parseSmsText("ManualSMS", smsBody)
        if (parsed != null) {
            _discoveredItems.value = (_discoveredItems.value + parsed).distinctBy { it.serviceName }
        }
    }

    fun parseManualEmailInput(subject: String, body: String) {
        val parsed = EmailReceiptParserEngine.parseEmailReceiptText(subject, body)
        if (parsed != null) {
            _discoveredItems.value = (_discoveredItems.value + parsed).distinctBy { it.serviceName }
        }
    }

    fun confirmDiscoveredItem(item: DiscoveredItem) {
        addSubscription(
            name = item.serviceName,
            amount = item.amount,
            currency = item.currency,
            billingCycle = item.billingCycle,
            category = item.category,
            providerType = item.source,
            notes = "Auto-detected from ${item.source.displayName}",
            rawSnippet = item.rawTextSnippet
        )
        dismissDiscoveredItem(item)
    }

    fun dismissDiscoveredItem(item: DiscoveredItem) {
        _discoveredItems.value = _discoveredItems.value.filter { it.id != item.id }
    }

    private fun calculateAnalytics(list: List<SubscriptionEntity>): SpendAnalytics {
        var totalMonthly = 0.0
        var totalYearly = 0.0
        var activeCount = 0
        var upcoming7Days = 0

        val categoryMap = mutableMapOf<SubscriptionCategory, Double>()
        val now = System.currentTimeMillis()
        val in7Days = now + (7L * 24 * 60 * 60 * 1000)

        for (sub in list) {
            if (sub.status == SubscriptionStatus.ACTIVE.name || sub.status == SubscriptionStatus.TRIAL.name) {
                activeCount++
                val cycle = try { BillingCycle.valueOf(sub.billingCycle) } catch (_: Exception) { BillingCycle.MONTHLY }
                val cat = try { SubscriptionCategory.valueOf(sub.category) } catch (_: Exception) { SubscriptionCategory.OTHER }

                val monthlyValue = when (cycle) {
                    BillingCycle.WEEKLY -> sub.amount * 4.33
                    BillingCycle.MONTHLY -> sub.amount
                    BillingCycle.QUARTERLY -> sub.amount / 3.0
                    BillingCycle.YEARLY -> sub.amount / 12.0
                    BillingCycle.ONE_TIME -> 0.0
                }

                totalMonthly += monthlyValue
                totalYearly += (monthlyValue * 12.0)

                categoryMap[cat] = (categoryMap[cat] ?: 0.0) + monthlyValue

                if (sub.nextDueDate in now..in7Days) {
                    upcoming7Days++
                }
            }
        }

        return SpendAnalytics(
            totalMonthlySpend = totalMonthly,
            totalYearlySpend = totalYearly,
            activeCount = activeCount,
            upcomingCount7Days = upcoming7Days,
            spendByCategory = categoryMap
        )
    }

    private suspend fun seedDefaultData() {
        val cal = Calendar.getInstance()

        val sample1 = SubscriptionEntity(
            name = "Netflix",
            amount = 649.0,
            currency = "INR",
            billingCycle = BillingCycle.MONTHLY.name,
            category = SubscriptionCategory.ENTERTAINMENT.name,
            providerType = ProviderType.SMS_DETECTED.name,
            nextDueDate = cal.apply { add(Calendar.DAY_OF_MONTH, 3) }.timeInMillis,
            paymentMethod = "Credit Card",
            notes = "Auto-renew active"
        )
        val id1 = repository.insertSubscription(sample1)

        // Seed 3 past monthly payment logs for Netflix
        val monthMs = 30L * 24 * 60 * 60 * 1000
        repository.insertPaymentHistory(PaymentHistoryEntity(subscriptionId = id1, serviceName = "Netflix", amount = 649.0, currency = "INR", paymentDate = System.currentTimeMillis() - (1 * monthMs), source = "SMS_DETECTED", rawSnippet = "INR 649.00 debited for Netflix Monthly Plan"))
        repository.insertPaymentHistory(PaymentHistoryEntity(subscriptionId = id1, serviceName = "Netflix", amount = 649.0, currency = "INR", paymentDate = System.currentTimeMillis() - (2 * monthMs), source = "SMS_DETECTED", rawSnippet = "INR 649.00 debited for Netflix Monthly Plan"))
        repository.insertPaymentHistory(PaymentHistoryEntity(subscriptionId = id1, serviceName = "Netflix", amount = 649.0, currency = "INR", paymentDate = System.currentTimeMillis() - (3 * monthMs), source = "SMS_DETECTED", rawSnippet = "INR 649.00 debited for Netflix Monthly Plan"))

        val sample2 = SubscriptionEntity(
            name = "Spotify",
            amount = 179.0,
            currency = "INR",
            billingCycle = BillingCycle.MONTHLY.name,
            category = SubscriptionCategory.ENTERTAINMENT.name,
            providerType = ProviderType.SMS_DETECTED.name,
            nextDueDate = cal.apply { add(Calendar.DAY_OF_MONTH, 12) }.timeInMillis,
            paymentMethod = "UPI AutoPay"
        )
        val id2 = repository.insertSubscription(sample2)
        repository.insertPaymentHistory(PaymentHistoryEntity(subscriptionId = id2, serviceName = "Spotify", amount = 179.0, currency = "INR", paymentDate = System.currentTimeMillis() - (1 * monthMs), source = "SMS_DETECTED", rawSnippet = "INR 179.00 debited for Spotify Subscription"))

        val sample3 = SubscriptionEntity(
            name = "ChatGPT Plus",
            amount = 20.0,
            currency = "USD",
            billingCycle = BillingCycle.MONTHLY.name,
            category = SubscriptionCategory.AI_CLOUD.name,
            providerType = ProviderType.EMAIL_DETECTED.name,
            nextDueDate = cal.apply { add(Calendar.DAY_OF_MONTH, 18) }.timeInMillis,
            paymentMethod = "Debit Card"
        )
        val id3 = repository.insertSubscription(sample3)
        repository.insertPaymentHistory(PaymentHistoryEntity(subscriptionId = id3, serviceName = "ChatGPT Plus", amount = 20.0, currency = "USD", paymentDate = System.currentTimeMillis() - (1 * monthMs), source = "EMAIL_DETECTED", rawSnippet = "Receipt for OpenAI ChatGPT Plus subscription"))

        val sample4 = SubscriptionEntity(
            name = "Amazon Prime",
            amount = 1499.0,
            currency = "INR",
            billingCycle = BillingCycle.YEARLY.name,
            category = SubscriptionCategory.ENTERTAINMENT.name,
            providerType = ProviderType.MANUAL.name,
            nextDueDate = cal.apply { add(Calendar.DAY_OF_MONTH, 45) }.timeInMillis,
            paymentMethod = "Credit Card"
        )
        val id4 = repository.insertSubscription(sample4)
        repository.insertPaymentHistory(PaymentHistoryEntity(subscriptionId = id4, serviceName = "Amazon Prime", amount = 1499.0, currency = "INR", paymentDate = System.currentTimeMillis() - (365 * 24 * 60 * 60 * 1000L), source = "MANUAL", rawSnippet = "Annual Membership auto-renew"))
    }

    private fun getDefaultDueDate(): Date {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, 1)
        return cal.time
    }
}
