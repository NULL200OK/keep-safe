package com.nabilhassan.keepsafe.security

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

/** App PIN: stored as a salted SHA-256 hash, never in plain text. */
object PinLock {
    private const val PREFS = "pin_lock"

    private fun prefs(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isSet(c: Context): Boolean = prefs(c).contains("hash")

    fun set(c: Context, pin: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        prefs(c).edit()
            .putString("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString("hash", hash(salt, pin))
            .apply()
    }

    fun verify(c: Context, pin: String): Boolean {
        val p = prefs(c)
        val salt = Base64.decode(p.getString("salt", null) ?: return false, Base64.NO_WRAP)
        return hash(salt, pin) == p.getString("hash", null)
    }

    fun clear(c: Context) { prefs(c).edit().clear().apply() }

    private fun hash(salt: ByteArray, pin: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt)
        return Base64.encodeToString(md.digest(pin.toByteArray()), Base64.NO_WRAP)
    }
}
