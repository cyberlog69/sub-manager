package com.submanager.app.engine

import com.submanager.app.data.model.BillingCycle
import com.submanager.app.data.model.DiscoveredItem
import com.submanager.app.data.model.ProviderType
import com.submanager.app.data.model.SubscriptionCategory
import java.util.Date
import java.util.regex.Pattern

object EmailReceiptParserEngine {

    private val EMAIL_PATTERNS = mapOf(
        "Google Play" to SubscriptionCategory.ENTERTAINMENT,
        "Apple Bill" to SubscriptionCategory.AI_CLOUD,
        "AWS" to SubscriptionCategory.WORK_SAAS,
        "Adobe Creative Cloud" to SubscriptionCategory.WORK_SAAS,
        "Dropbox" to SubscriptionCategory.AI_CLOUD,
        "Zoom" to SubscriptionCategory.WORK_SAAS,
        "Slack" to SubscriptionCategory.WORK_SAAS,
        "Duolingo" to SubscriptionCategory.NEWS,
        "Coursera" to SubscriptionCategory.NEWS,
        "Medium" to SubscriptionCategory.NEWS
    )

    fun parseEmailReceiptText(subject: String, body: String): DiscoveredItem? {
        val fullText = "$subject $body"
        val lowerText = fullText.lowercase()

        val isReceipt = lowerText.contains("receipt") || lowerText.contains("invoice") || 
                        lowerText.contains("subscription confirmed") || lowerText.contains("renewal notice")

        if (!isReceipt) return null

        var detectedVendor = "Digital Service"
        var category = SubscriptionCategory.OTHER

        for ((vendor, cat) in EMAIL_PATTERNS) {
            if (lowerText.contains(vendor.lowercase())) {
                detectedVendor = vendor
                category = cat
                break
            }
        }

        // Regex amount search
        val pattern = Pattern.compile("(?:RS|INR|USD|\\$)\\.?\\s*([\\d,]+(?:\\.\\d{1,2})?)", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(fullText)
        var amount = 0.0
        var currency = if (lowerText.contains("usd") || lowerText.contains("$")) "USD" else "INR"

        if (matcher.find()) {
            amount = matcher.group(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0
        }

        if (amount <= 0.0) return null

        return DiscoveredItem(
            serviceName = detectedVendor,
            amount = amount,
            currency = currency,
            billingCycle = if (lowerText.contains("annual") || lowerText.contains("year")) BillingCycle.YEARLY else BillingCycle.MONTHLY,
            source = ProviderType.EMAIL_DETECTED,
            rawTextSnippet = subject,
            category = category,
            detectedDate = Date()
        )
    }
}
