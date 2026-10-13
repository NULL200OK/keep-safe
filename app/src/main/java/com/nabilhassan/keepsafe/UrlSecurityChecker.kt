package com.nabilhassan.keepsafe

import java.net.IDN
import java.net.URI

data class RiskResult(
    val score: Int,
    val verdictAr: String,
    val verdictEn: String,
    val reasons: List<Pair<String, String>>
)

data class UrlReport(
    val originalUrl: String,
    val host: String,
    val registeredDomain: String,
    val risk: RiskResult
)

object UrlSecurityChecker {

    private val SHORTENERS = setOf(
        "bit.ly", "t.co", "goo.gl", "tinyurl.com", "ow.ly", "is.gd",
        "buff.ly", "rebrand.ly", "cutt.ly", "shorturl.at", "rb.gy"
    )

    private val SUSPICIOUS_KEYWORDS = listOf(
        "login", "signin", "verify", "account", "update", "secure",
        "bank", "paypal", "apple", "google", "microsoft", "wallet",
        "crypto", "password", "confirm", "recovery"
    )

    private val CONFUSABLES = mapOf(
        'а' to 'a', 'е' to 'e', 'о' to 'o', 'р' to 'p', 'с' to 'c',
        'х' to 'x', 'у' to 'y', 'і' to 'i', 'ј' to 'j', 'ѕ' to 's'
    )

    fun analyze(url: String, db: ThreatDatabase): UrlReport {
        val normalized = if (url.startsWith("http://") || url.startsWith("https://"))
            url else "https://$url"
        val host = try { URI(normalized).host?.lowercase() ?: "" } catch (e: Exception) { "" }

        val scores = mutableListOf<Int>()
        val reasons = mutableListOf<Pair<String, String>>()

        val asciiHost = try { IDN.toASCII(host) } catch (e: Exception) { host }
        if (asciiHost.contains("xn--")) {
            scores += 25
            reasons += "يستخدم Punycode" to "Uses Punycode"
        }

        if (host.any { it in CONFUSABLES }) {
            scores += 35
            reasons += "حروف متشابهة مخادعة" to "Homograph characters"
        }

        if (host in SHORTENERS) {
            scores += 20
            reasons += "رابط مختصر" to "Shortened URL"
        }

        if (Regex("""^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$""").matches(host)) {
            scores += 30
            reasons += "عنوان IP مباشر" to "Direct IP"
        }

        if (url.contains("@")) {
            scores += 25
            reasons += "يحتوي @" to "Contains @"
        }

        val kw = SUSPICIOUS_KEYWORDS.count { normalized.lowercase().contains(it) }
        if (kw > 0) {
            scores += kw * 5
            reasons += "كلمات مفتاحية: $kw" to "Keywords: $kw"
        }

        if (host.split(".").size > 4) {
            scores += 15
            reasons += "نطاقات فرعية كثيرة" to "Too many subdomains"
        }

        val dbHit = try { db.isMalicious(normalized) } catch (e: Exception) { null }
        if (dbHit != null) {
            scores += 100
            reasons += "قاعدة محلية" to "Local DB"
        }

        val total = scores.sum().coerceAtMost(100)
        val (vAr, vEn) = when {
            total <= 20 -> "آمن نسبيًا" to "Relatively Safe"
            total <= 50 -> "مشبوه" to "Suspicious"
            total <= 80 -> "خطر مرتفع" to "High Risk"
            else -> "خطر شديد" to "Critical"
        }

        return UrlReport(
            originalUrl = url,
            host = host,
            registeredDomain = extractDomain(host),
            risk = RiskResult(total, vAr, vEn, reasons)
        )
    }

    private fun extractDomain(host: String): String {
        val parts = host.split(".")
        return if (parts.size >= 2) parts.takeLast(2).joinToString(".") else host
    }
}