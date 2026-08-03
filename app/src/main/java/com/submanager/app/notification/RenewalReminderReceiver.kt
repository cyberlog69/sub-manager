package com.submanager.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class RenewalReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val subName = intent.getStringExtra("SUB_NAME") ?: "Subscription"
        val amount = intent.getDoubleExtra("SUB_AMOUNT", 0.0)
        val currency = intent.getStringExtra("SUB_CURRENCY") ?: "INR"

        NotificationHelper.showNotification(
            context = context,
            title = "Upcoming Renewal: $subName",
            message = "Your $subName subscription ($currency $amount) will renew soon. Tap to manage."
        )
    }
}
