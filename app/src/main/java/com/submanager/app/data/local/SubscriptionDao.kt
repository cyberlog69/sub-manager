package com.submanager.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions ORDER BY nextDueDate ASC")
    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    suspend fun getSubscriptionById(id: Long): SubscriptionEntity?

    @Query("SELECT * FROM subscriptions WHERE status = 'ACTIVE' OR status = 'TRIAL'")
    fun getActiveSubscriptions(): Flow<List<SubscriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: SubscriptionEntity): Long

    @Update
    suspend fun updateSubscription(subscription: SubscriptionEntity)

    @Delete
    suspend fun deleteSubscription(subscription: SubscriptionEntity)

    @Query("DELETE FROM subscriptions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM subscriptions WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun findByName(name: String): SubscriptionEntity?

    // Payment History Queries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentHistory(history: PaymentHistoryEntity): Long

    @Query("SELECT * FROM payment_history WHERE subscriptionId = :subId ORDER BY paymentDate DESC")
    fun getHistoryForSubscription(subId: Long): Flow<List<PaymentHistoryEntity>>

    @Query("SELECT * FROM payment_history ORDER BY paymentDate DESC")
    fun getAllPaymentHistory(): Flow<List<PaymentHistoryEntity>>
}
