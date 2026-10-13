package com.nabilhassan.keepsafe

import org.junit.Assert.*
import org.junit.Test

class CallerFraudCheckerTest {
    @Test fun knownFraudPrefix() {
        assertTrue(CallerFraudChecker.isKnownFraud("+19001234567"))
    }

    @Test fun normalNumber() {
        assertFalse(CallerFraudChecker.isKnownFraud("+963912345678"))
    }
}