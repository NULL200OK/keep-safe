package com.nabilhassan.keepsafe

data class SpamResult(
    val isSpam: Boolean,
    val probability: Double,
    val verdictAr: String,
    val verdictEn: String
)

object SpamChecker {

    private val SPAM_KEYWORDS = listOf(
        "win", "winner", "free", "prize", "congratulations", "urgent",
        "click here", "act now", "limited time", "cash", "money",
        "bitcoin", "crypto", "investment", "loan",
        "ربح", "جائزة", "مجاني", "عاجل", "اضغط هنا", "تحويل"
    )

    private val SPAM_PATTERNS = listOf(
        Regex("""\b\d{10,}\b"""),
        Regex("""https?://\S+"""),
        Regex("""[A-Z]{5,}"""),
        Regex("""(.)\1{4,}"""),
        Regex("""[!?]{3,}""")
    )

    fun checkLocal(text: String): SpamResult {
        if (text.isBlank()) return SpamResult(false, 0.0, "نص فارغ", "Empty")

        val lower = text.lowercase()
        var score = 0.0
        score += SPAM_KEYWORDS.count { lower.contains(it) } * 0.15
        SPAM_PATTERNS.forEach { if (it.containsMatchIn(text)) score += 0.1 }

        val upperRatio = text.count { it.isUpperCase() }.toDouble() / text.length
        if (upperRatio > 0.5) score += 0.15
        if (text.length < 10) score += 0.05

        val prob = score.coerceIn(0.0, 1.0)
        val isSpam = prob >= 0.5

        return SpamResult(
            isSpam = isSpam,
            probability = prob,
            verdictAr = if (isSpam) "سبام مشبوه" else "حقيقي",
            verdictEn = if (isSpam) "Spam" else "Ham"
        )
    }
}