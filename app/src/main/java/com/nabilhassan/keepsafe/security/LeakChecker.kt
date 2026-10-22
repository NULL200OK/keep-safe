package com.nabilhassan.keepsafe.security

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * Checks a password against the Have I Been Pwned range API using k-anonymity:
 * only the first 5 hex characters of the SHA-1 hash ever leave the device.
 */
object LeakChecker {

    /** Returns how many times the password appears in known leaks, or null on network error. */
    suspend fun timesLeaked(password: String): Int? = withContext(Dispatchers.IO) {
        try {
            val sha1 = MessageDigest.getInstance("SHA-1").digest(password.toByteArray())
                .joinToString("") { "%02X".format(it) }
            val prefix = sha1.substring(0, 5)
            val suffix = sha1.substring(5)
            val conn = URL("https://api.pwnedpasswords.com/range/$prefix")
                .openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("Add-Padding", "true")
            if (conn.responseCode != 200) return@withContext null
            conn.inputStream.bufferedReader().useLines { lines ->
                for (l in lines) {
                    val p = l.trim().split(":")
                    if (p.size == 2 && p[0].equals(suffix, ignoreCase = true))
                        return@useLines p[1].trim().toIntOrNull() ?: 0
                }
                0
            }
        } catch (_: Exception) { null }
    }
}
