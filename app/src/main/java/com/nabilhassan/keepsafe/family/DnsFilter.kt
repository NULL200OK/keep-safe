package com.nabilhassan.keepsafe.family

import java.io.ByteArrayOutputStream

/** DNS packet helpers and the filtering decision used by the VPN service. */
object DnsFilter {

    /** [end] is the offset just past the question section, relative to the DNS message start. */
    class Question(val name: String, val type: Int, val end: Int)

    sealed class Decision {
        object Forward : Decision()
        class Block(val reason: BlockReason) : Decision()
        class Redirect(val ip: ByteArray) : Decision()
    }

    private val strictSuffixes = setOf(
        "bet365.com", "1xbet.com", "888casino.com", "pokerstars.com", "betway.com",
        "draftkings.com", "stake.com", "bwin.com", "unibet.com", "williamhill.com",
        "22bet.com", "melbet.com", "mostbet.com", "parimatch.com", "betfair.com",
        "liveleak.com", "bestgore.com", "kaotic.com", "theync.com", "crazyshit.com",
        "documentingreality.com", "goregrish.com"
    )
    private val strictKeywords = listOf("casino", "betting", "gambling", "sportsbet")

    private val googleSafe = byteArrayOf(216.toByte(), 239.toByte(), 38, 120)
    private val bingSafe = byteArrayOf(204.toByte(), 79, 197.toByte(), 220.toByte())
    private val youtubeSafe = byteArrayOf(216.toByte(), 239.toByte(), 38, 120)
    private val googleHost = Regex("^www\\.google\\.[a-z]{2,3}(\\.[a-z]{2})?$")
    private val youtubeHosts = setOf(
        "www.youtube.com", "m.youtube.com", "youtubei.googleapis.com",
        "youtube.googleapis.com", "www.youtube-nocookie.com"
    )

    /** Parses the first question of a DNS message found at [msg]. */
    fun parseQuestion(msg: ByteArray): Question? {
        if (msg.size < 17) return null
        var i = 12
        val sb = StringBuilder()
        while (i < msg.size) {
            val len = msg[i].toInt() and 0xFF
            if (len == 0) { i++; break }
            if (len and 0xC0 != 0 || i + 1 + len > msg.size) return null
            if (sb.isNotEmpty()) sb.append('.')
            for (k in 1..len) sb.append((msg[i + k].toInt() and 0xFF).toChar().lowercaseChar())
            i += len + 1
        }
        if (i + 4 > msg.size) return null
        val type = ((msg[i].toInt() and 0xFF) shl 8) or (msg[i + 1].toInt() and 0xFF)
        return Question(sb.toString(), type, i + 4)
    }

    private fun matches(domain: String, set: Collection<String>) =
        set.any { domain == it || domain.endsWith(".$it") }

    fun decide(domain: String): Decision {
        if (matches(domain, FamilySettings.customAllow)) return Decision.Forward
        if (FamilySettings.isScheduleBlockedNow()) return Decision.Block(BlockReason.SCHEDULE)
        if (matches(domain, FamilySettings.customBlock)) return Decision.Block(BlockReason.CUSTOM)
        if (FamilySettings.level == ProtectionLevel.STRICT &&
            (matches(domain, strictSuffixes) || strictKeywords.any { domain.contains(it) }))
            return Decision.Block(BlockReason.STRICT)
        if (FamilySettings.safeSearch) {
            if (googleHost.matches(domain)) return Decision.Redirect(googleSafe)
            if (domain == "www.bing.com") return Decision.Redirect(bingSafe)
            if (domain in youtubeHosts) return Decision.Redirect(youtubeSafe)
        }
        return Decision.Forward
    }

    /**
     * Builds a DNS reply for [query]. [ip] is the IPv4 answer for A queries;
     * null means a block answer (0.0.0.0 for A, :: for AAAA).
     */
    fun buildReply(query: ByteArray, q: Question, ip: ByteArray?): ByteArray {
        val isA = q.type == 1
        val isAaaa = q.type == 28
        // A redirect has no IPv6 address, so AAAA gets an empty answer; a block answers :: instead.
        val answer = isA || (isAaaa && ip == null)
        val rdata = when {
            isA -> ip ?: ByteArray(4)
            answer -> ByteArray(16)
            else -> ByteArray(0)
        }
        val out = ByteArrayOutputStream()
        out.write(query, 0, 2)
        out.write(0x81); out.write(0x80)
        out.write(0); out.write(1)
        out.write(0); out.write(if (answer) 1 else 0)
        out.write(0); out.write(0); out.write(0); out.write(0)
        out.write(query, 12, q.end - 12)
        if (answer) {
            out.write(0xC0); out.write(0x0C)
            out.write(0); out.write(q.type)
            out.write(0); out.write(1)
            out.write(0); out.write(0); out.write(0); out.write(60)
            out.write(0); out.write(rdata.size)
            out.write(rdata)
        }
        return out.toByteArray()
    }

    /** True if the resolver answered with an all-zero address (its way of blocking). */
    fun isProviderBlock(resp: ByteArray): Boolean {
        val n = resp.size
        if (n < 24) return false
        val ancount = ((resp[6].toInt() and 0xFF) shl 8) or (resp[7].toInt() and 0xFF)
        val rcode = resp[3].toInt() and 0x0F
        if (ancount == 0 || rcode != 0) return false
        val a4 = resp[n - 6].toInt() == 0 && resp[n - 5].toInt() == 4 &&
            (n - 4 until n).all { resp[it].toInt() == 0 }
        val a16 = n >= 18 && resp[n - 18].toInt() == 0 && resp[n - 17].toInt() == 16 &&
            (n - 16 until n).all { resp[it].toInt() == 0 }
        return a4 || a16
    }
}
