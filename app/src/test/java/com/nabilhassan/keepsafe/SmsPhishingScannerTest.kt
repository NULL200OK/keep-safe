package com.nabilhassan.keepsafe

import org.junit.Assert.*
import org.junit.Test

class SmsPhishingScannerTest {
    @Test fun phishingDetected() {
        val r = SmsPhishingScanner.analyzeSms(
            "+1234567890", "Urgent! Verify your account now: http://fake.com/login")
        assertTrue(r.isPhishing)
    }

    @Test fun legitNotFlagged() {
        val r = SmsPhishingScanner.analyzeSms("+1234567890", "See you at 5pm")
        assertFalse(r.isPhishing)
    }
}