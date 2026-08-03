package com.submanager.app.data.repository

import com.submanager.app.data.local.PaymentHistoryEntity
import com.submanager.app.data.local.SubscriptionDao
import com.submanager.app.data.local.SubscriptionEntity
import kotlinx.coroutines.flow.Flow

class SubscriptionRepository(private val subscriptionDao: SubscriptionDao) {

    val allSubscriptions: Flow<List<SubscriptionEntity>> = subscriptionDao.getAllSubscriptions()
    val activeSubscriptions: Flow<List<SubscriptionEntity>> = subscriptionDao.getActiveSubscriptions()

    suspend fun getSubscriptionById(id: Long): SubscriptionEntity? {
        return subscriptionDao.getSubscriptionById(id)
    }

    suspend fun insertSubscription(subscription: SubscriptionEntity): Long {
        return subscriptionDao.insertSubscription(subscription)
    }

    suspend fun updateSubscription(subscription: SubscriptionEntity) {
        subscriptionDao.updateSubscription(subscription)
    }

    suspend fun deleteSubscription(subscription: SubscriptionEntity) {
        subscriptionDao.deleteSubscription(subscription)
    }

    suspend fun deleteById(id: Long) {
        subscriptionDao.deleteById(id)
    }

    suspend fun findByName(name: String): SubscriptionEntity? {
        return subscriptionDao.findByName(name)
    }

    // Payment History Methods
    suspend fun insertPaymentHistory(history: PaymentHistoryEntity): Long {
        return subscriptionDao.insertPaymentHistory(history)
    }

    fun getHistoryForSubscription(subId: Long): Flow<List<PaymentHistoryEntity>> {
        return subscriptionDao.getHistoryForSubscription(subId)
    }
}
