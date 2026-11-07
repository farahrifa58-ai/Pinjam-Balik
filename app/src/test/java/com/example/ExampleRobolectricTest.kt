package com.example

import com.example.pinjambalik.util.AppConfig
import com.example.pinjambalik.util.AppStrings
import com.example.pinjambalik.util.PrivacyUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun testMaskIdentityNumber() {
        val masked = PrivacyUtils.maskIdentityNumber("3201234567890001")
        assertTrue(masked.startsWith("320123"))
        assertTrue(masked.endsWith("0001"))
        assertTrue(masked.contains("****"))
    }

    @Test
    fun testAppStrings() {
        val idStrings = AppStrings.get("id")
        assertEquals("PinjamBalik", idStrings.appName)
        assertEquals("Sewa Baru", idStrings.newRental)

        val enStrings = AppStrings.get("en")
        assertEquals("PinjamBalik", enStrings.appName)
        assertEquals("New Rental", enStrings.newRental)
    }

    @Test
    fun testAppConfig() {
        assertEquals("PinjamBalik", AppConfig.APP_NAME)
        assertEquals("Rafline Dev", AppConfig.DEVELOPER_NAME)
        assertEquals(3600L, AppConfig.TRIAL_DURATION_SECONDS)
    }
}
