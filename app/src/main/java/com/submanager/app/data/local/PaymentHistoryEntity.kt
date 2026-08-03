package com.submanager.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_history")
data class PaymentHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subscriptionId: Long,
    val serviceName: String,
    val amount: Double,
    val currency: String = "INR",
    val paymentDate: Long = System.currentTimeMillis(),
    val source: String = "SMS_DETECTED",
    val rawSnippet: String = ""
)
