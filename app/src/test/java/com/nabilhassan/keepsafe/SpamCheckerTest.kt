package com.nabilhassan.keepsafe

import org.junit.Assert.*
import org.junit.Test

class SpamCheckerTest {
    @Test fun spamTextDetected() {
        val r = SpamChecker.checkLocal("WINNER! You won 1000000. Click here NOW!!!")
        assertTrue(r.isSpam)
    }

    @Test fun legitTextNotFlagged() {
        val r = SpamChecker.checkLocal("Meeting scheduled for tomorrow at 10 AM.")
        assertFalse(r.isSpam)
    }

    @Test fun emptyTextZero() {
        val r = SpamChecker.checkLocal("")
        assertEquals(0.0, r.probability, 0.01)
    }
}