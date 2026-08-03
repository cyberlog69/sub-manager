package com.submanager.app.engine

import com.submanager.app.data.model.BillingCycle
import com.submanager.app.data.model.DiscoveredItem
import com.submanager.app.data.model.ProviderType
import com.submanager.app.data.model.SubscriptionCategory
import java.util.Date
import java.util.regex.Pattern

object SmsParserEngine {

    private val VENDOR_PATTERNS = mapOf(
        "Netflix" to Pair(SubscriptionCategory.ENTERTAINMENT, "movie"),
        "Spotify" to Pair(SubscriptionCategory.ENTERTAINMENT, "music_note"),
        "Amazon Prime" to Pair(SubscriptionCategory.ENTERTAINMENT, "shopping_cart"),
        "Prime Video" to Pair(SubscriptionCategory.ENTERTAINMENT, "movie"),
        "YouTube Premium" to Pair(SubscriptionCategory.ENTERTAINMENT, "play_circle"),
        "Hotstar" to Pair(SubscriptionCategory.ENTERTAINMENT, "live_tv"),
        "Disney+" to Pair(SubscriptionCategory.ENTERTAINMENT, "movie"),
        "Apple" to Pair(SubscriptionCategory.AI_CLOUD, "phone_iphone"),
        "iCloud" to Pair(SubscriptionCategory.AI_CLOUD, "cloud"),
        "Google One" to Pair(SubscriptionCategory.AI_CLOUD, "cloud"),
        "ChatGPT" to Pair(SubscriptionCategory.AI_CLOUD, "psychology"),
        "OpenAI" to Pair(SubscriptionCategory.AI_CLOUD, "psychology"),
        "Claude" to Pair(SubscriptionCategory.AI_CLOUD, "psychology"),
        "GitHub" to Pair(SubscriptionCategory.WORK_SAAS, "code"),
        "Notion" to Pair(SubscriptionCategory.WORK_SAAS, "description"),
        "Figma" to Pair(SubscriptionCategory.WORK_SAAS, "brush"),
        "LinkedIn" to Pair(SubscriptionCategory.WORK_SAAS, "work"),
        "Adobe" to Pair(SubscriptionCategory.WORK_SAAS, "palette"),
        "Microsoft 365" to Pair(SubscriptionCategory.WORK_SAAS, "window"),
        "PlayStation Plus" to Pair(SubscriptionCategory.GAMING, "sports_esports"),
        "Xbox Game Pass" to Pair(SubscriptionCategory.GAMING, "sports_esports"),
        "Airtel" to Pair(SubscriptionCategory.UTILITIES, "cell_tower"),
        "Jio" to Pair(SubscriptionCategory.UTILITIES, "wifi"),
        "Cult.fit" to Pair(SubscriptionCategory.FITNESS, "fitness_center")
    )

    private val RECURRING_KEYWORDS = listOf(
        "subscription", "debited for", "auto-renew", "auto renew", "autodebit",
        "mandate", "recurring", "monthly plan", "yearly plan", "renewed", "membership"
    )

    private val AMOUNT_PATTERNS = listOf(
        Pattern.compile("(?:RS|INR|USD|\\$)\\.?\\s*([\\d,]+(?:\\.\\d{1,2})?)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("([\\d,]+(?:\\.\\d{1,2})?)\\s*(?:RS|INR|USD)", Pattern.CASE_INSENSITIVE)
    )

    fun parseSmsText(sender: String, messageBody: String): DiscoveredItem? {
        val lowerBody = messageBody.lowercase()

        // Check if SMS mentions subscription or recurring keywords
        val isSubscriptionRelated = RECURRING_KEYWORDS.any { keyword -> lowerBody.contains(keyword) }
        
        // Find matching vendor
        var matchedVendor: String? = null
        var matchedCategory = SubscriptionCategory.OTHER

        for ((vendor, categoryPair) in VENDOR_PATTERNS) {
            if (lowerBody.contains(vendor.lowercase())) {
                matchedVendor = vendor
                matchedCategory = categoryPair.first
                break
            }
        }

        // If no known vendor matched and it's not subscription related, ignore
        if (matchedVendor == null && !isSubscriptionRelated) {
            return null
        }

        // Extract amount
        var extractedAmount = 0.0
        var currency = "INR"

        if (lowerBody.contains("usd") || lowerBody.contains("$")) {
            currency = "USD"
        }

        for (pattern in AMOUNT_PATTERNS) {
            val matcher = pattern.matcher(messageBody)
            if (matcher.find()) {
                val amountStr = matcher.group(1)?.replace(",", "")
                extractedAmount = amountStr?.toDoubleOrNull() ?: 0.0
                if (extractedAmount > 0.0) break
            }
        }

        if (extractedAmount <= 0.0) return null

        val finalVendorName = matchedVendor ?: "Subscription Payment"

        // Determine cycle
        val cycle = if (lowerBody.contains("year") || lowerBody.contains("annual")) {
            BillingCycle.YEARLY
        } else {
            BillingCycle.MONTHLY
        }

        return DiscoveredItem(
            serviceName = finalVendorName,
            amount = extractedAmount,
            currency = currency,
            billingCycle = cycle,
            source = ProviderType.SMS_DETECTED,
            rawTextSnippet = messageBody.take(120),
            category = matchedCategory,
            detectedDate = Date()
        )
    }

    fun scanSampleSmsList(smsList: List<Pair<String, String>>): List<DiscoveredItem> {
        val results = mutableListOf<DiscoveredItem>()
        for ((sender, body) in smsList) {
            val parsed = parseSmsText(sender, body)
            if (parsed != null) {
                results.add(parsed)
            }
        }
        return results
    }
}
