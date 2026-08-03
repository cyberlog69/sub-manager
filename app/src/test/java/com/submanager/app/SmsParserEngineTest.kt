package com.submanager.app

import com.submanager.app.data.model.BillingCycle
import com.submanager.app.data.model.ProviderType
import com.submanager.app.data.model.SubscriptionCategory
import com.submanager.app.engine.SmsParserEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SmsParserEngineTest {

    @Test
    fun testParseNetflixSms() {
        val sms = "Alert: INR 649.00 debited from A/C xx8912 for Netflix Monthly Subscription. Auto-renew active."
        val result = SmsParserEngine.parseSmsText("BANK", sms)

        assertNotNull(result)
        assertEquals("Netflix", result?.serviceName)
        assertEquals(649.0, result?.amount!!, 0.01)
        assertEquals("INR", result.currency)
        assertEquals(SubscriptionCategory.ENTERTAINMENT, result.category)
        assertEquals(BillingCycle.MONTHLY, result.billingCycle)
        assertEquals(ProviderType.SMS_DETECTED, result.source)
    }

    @Test
    fun testParseSpotifySms() {
        val sms = "INR 179.00 debited for Spotify Subscription via UPI auto debit mandate."
        val result = SmsParserEngine.parseSmsText("HDFC", sms)

        assertNotNull(result)
        assertEquals("Spotify", result?.serviceName)
        assertEquals(179.0, result?.amount!!, 0.01)
        assertEquals("INR", result.currency)
        assertEquals(SubscriptionCategory.ENTERTAINMENT, result.category)
    }

    @Test
    fun testParseChatGptUsdSms() {
        val sms = "Alert: USD 20.00 debited from card ending 4401 for ChatGPT subscription renewal."
        val result = SmsParserEngine.parseSmsText("ICICI", sms)

        assertNotNull(result)
        assertEquals("ChatGPT", result?.serviceName)
        assertEquals(20.0, result?.amount!!, 0.01)
        assertEquals("USD", result.currency)
        assertEquals(SubscriptionCategory.AI_CLOUD, result.category)
    }

    @Test
    fun testIgnoreIrrelevantSms() {
        val sms = "Your OTP for logging into ABC portal is 984102. Valid for 5 mins."
        val result = SmsParserEngine.parseSmsText("VERIFY", sms)

        assertNull(result)
    }
}
