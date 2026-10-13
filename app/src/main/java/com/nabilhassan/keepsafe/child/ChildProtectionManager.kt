package com.nabilhassan.keepsafe.child

import android.content.Context

data class ChildProfile(
    val name: String,
    val age: Int,
    val restrictionLevel: RestrictionLevel,
    val blockedCategories: Set<ContentCategory>
)

enum class RestrictionLevel { LOW, MEDIUM, HIGH, STRICT }

enum class ContentCategory {
    ADULT, GAMBLING, SOCIAL_MEDIA, VIOLENCE, DRUGS
}

object ChildProtectionManager {

    private const val PREFS = "child_protection_prefs"

    fun saveProfile(context: Context, profile: ChildProfile) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString("level", profile.restrictionLevel.name)
            .putStringSet("cats", profile.blockedCategories.map { it.name }.toSet())
            .apply()
    }

    fun isContentAllowed(profile: ChildProfile, domain: String, hour: Int): Boolean {
        if (hour < 7 || hour > 21) return false
        val cat = categorize(domain) ?: return true
        return cat !in profile.blockedCategories
    }

    private fun categorize(domain: String): ContentCategory? {
        val d = domain.lowercase()
        return when {
            listOf("porn", "xxx", "adult").any { d.contains(it) } -> ContentCategory.ADULT
            listOf("bet", "casino").any { d.contains(it) } -> ContentCategory.GAMBLING
            listOf("facebook", "tiktok").any { d.contains(it) } -> ContentCategory.SOCIAL_MEDIA
            else -> null
        }
    }
}