package com.submanager.app.engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.submanager.app.notification.NotificationHelper

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (message in messages) {
                val sender = message.originatingAddress ?: "Unknown"
                val body = message.messageBody ?: continue

                Log.d("SmsReceiver", "Received SMS from $sender: $body")

                val detected = SmsParserEngine.parseSmsText(sender, body)
                if (detected != null) {
                    NotificationHelper.showNotification(
                        context = context,
                        title = "Subscription Detected!",
                        message = "Found ${detected.serviceName} (${detected.currency} ${detected.amount}). Tap to confirm adding to SubSync."
                    )
                }
            }
        }
    }
}
