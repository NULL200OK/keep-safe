package com.nabilhassan.keepsafe

object CallerFraudChecker {
    private val KNOWN_FRAUD_PREFIXES = listOf("+1900", "+88")

    fun isKnownFraud(number: String): Boolean =
        KNOWN_FRAUD_PREFIXES.any { number.startsWith(it) }

    fun calculateRisk(number: String): Int {
        var score = 0
        if (number.length < 7) score += 15
        if (isKnownFraud(number)) score += 60
        if (number.startsWith("+88") || number.startsWith("+234")) score += 40
        if (Regex("""(\d)\1{5,}""").containsMatchIn(number)) score += 20
        return score.coerceIn(0, 100)
    }
}