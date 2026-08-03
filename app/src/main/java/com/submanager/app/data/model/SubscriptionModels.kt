package com.submanager.app.data.model

import java.util.Date

enum class SubscriptionCategory(val displayName: String, val iconName: String) {
    ENTERTAINMENT("Entertainment", "movie"),
    UTILITIES("Utilities & Bills", "flash_on"),
    WORK_SAAS("Work & Productivity", "work"),
    AI_CLOUD("AI & Cloud Services", "cloud"),
    GAMING("Gaming", "sports_esports"),
    FITNESS("Health & Fitness", "fitness_center"),
    NEWS("News & Education", "menu_book"),
    OTHER("Other", "category")
}

enum class BillingCycle(val displayName: String, val monthsMultiplier: Float) {
    WEEKLY("Weekly", 0.25f),
    MONTHLY("Monthly", 1.0f),
    QUARTERLY("Quarterly", 3.0f),
    YEARLY("Yearly", 12.0f),
    ONE_TIME("One Time", 0.0f)
}

enum class ProviderType(val displayName: String) {
    MANUAL("Manual Entry"),
    SMS_DETECTED("Mobile SMS"),
    EMAIL_DETECTED("Email Receipt")
}

enum class SubscriptionStatus(val displayName: String) {
    ACTIVE("Active"),
    TRIAL("Free Trial"),
    PAUSED("Paused"),
    CANCELLED("Cancelled")
}

data class DiscoveredItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val serviceName: String,
    val amount: Double,
    val currency: String = "INR",
    val billingCycle: BillingCycle = BillingCycle.MONTHLY,
    val source: ProviderType,
    val rawTextSnippet: String,
    val category: SubscriptionCategory = SubscriptionCategory.ENTERTAINMENT,
    val detectedDate: Date = Date()
)
