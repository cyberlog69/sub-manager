package com.submanager.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.submanager.app.data.model.BillingCycle
import com.submanager.app.data.model.ProviderType
import com.submanager.app.data.model.SubscriptionCategory
import com.submanager.app.data.model.SubscriptionStatus
import java.util.Date

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val currency: String = "INR",
    val billingCycle: String = BillingCycle.MONTHLY.name,
    val category: String = SubscriptionCategory.ENTERTAINMENT.name,
    val providerType: String = ProviderType.MANUAL.name,
    val status: String = SubscriptionStatus.ACTIVE.name,
    val startDate: Long = System.currentTimeMillis(),
    val nextDueDate: Long,
    val reminderDaysBefore: Int = 2,
    val autoRenew: Boolean = true,
    val notes: String = "",
    val paymentMethod: String = "UPI / Card",
    val iconName: String = "category"
)

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time
}
