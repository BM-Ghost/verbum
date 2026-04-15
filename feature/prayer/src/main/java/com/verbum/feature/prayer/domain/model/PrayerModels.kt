package com.verbum.feature.prayer.domain.model

data class Prayer(
    val id: String,
    val title: String,
    val category: PrayerCategory,
    val text: String,
    val latinText: String? = null,
)

enum class PrayerCategory(val displayName: String, val emoji: String) {
    ROSARY("Rosary", "📿"),
    DEVOTION("Devotions", "🕯️"),
    MORNING("Morning Prayers", "🌅"),
    EVENING("Evening Prayers", "🌙"),
    SAINTS("Prayers to Saints", "⭐"),
}
