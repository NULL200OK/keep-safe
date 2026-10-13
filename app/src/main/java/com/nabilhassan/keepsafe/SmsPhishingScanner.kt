package com.nabilhassan.keepsafe

data class SmsPhishingResult(
    val isPhishing: Boolean,
    val probability: Double,
    val verdictAr: String,
    val verdictEn: String
)

object SmsPhishingScanner {

    private val URGENCY = listOf("urgent", "immediate", "act now", "expires",
        "عاجل", "فوري", "محدود", "ينتهي")
    private val CREDENTIAL = listOf("verify", "confirm", "update", "login",
        "password", "account", "تحقق", "أكد", "حدث", "كلمة المرور")
    private val FINANCIAL = listOf("bank", "credit", "loan", "prize",
        "winner", "cash", "بنك", "قرض", "جائزة")
    private val IMPERSONATION = listOf("paypal", "apple", "microsoft",
        "google", "amazon", "netflix", "بنك", "مصرف")

    fun analyzeSms(sender: String, body: String): SmsPhishingResult {
        val text = "$sender $body".lowercase()
        var score = 0.0

        val urls = Regex("""https?://\S+""").findAll(text).count()
        if (urls > 0) score += 0.25

        score += URGENCY.count { text.contains(it) } * 0.08
        score += CREDENTIAL.count { text.contains(it) } * 0.15
        score += FINANCIAL.count { text.contains(it) } * 0.12
        score += IMPERSONATION.count { text.contains(it) } * 0.15

        if (Regex("""[A-Z]{5,}""").containsMatchIn(body)) score += 0.08

        val prob = score.coerceIn(0.0, 1.0)
        val isPhishing = prob >= 0.5

        return SmsPhishingResult(
            isPhishing = isPhishing,
            probability = prob,
            verdictAr = if (isPhishing) "رسالة تصيّد" else "رسالة طبيعية",
            verdictEn = if (isPhishing) "SMS Phishing" else "Legitimate SMS"
        )
    }
}