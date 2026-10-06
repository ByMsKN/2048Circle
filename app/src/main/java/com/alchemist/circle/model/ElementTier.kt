package com.alchemist.circle.model

import androidx.compose.ui.graphics.Color

enum class ElementTier(
    val value: Int,
    val symbol: String,
    val title: String,
    val color: Color,
    val borderColor: Color
) {
    WATER(2, "💧", "Su", Color(0xFF00B0FF), Color(0xFF80D8FF)),
    STEAM(4, "💨", "Buhar", Color(0xFF00E676), Color(0xFFB9F6CA)),
    CLOUD(8, "☁️", "Bulut", Color(0xFFFFD600), Color(0xFFFFFF8D)),
    ENERGY(16, "⚡", "Enerji", Color(0xFFFF9100), Color(0xFFFFD180)),
    PLASMA(32, "🔥", "Ateş", Color(0xFFFF3D00), Color(0xFFFF9E80)),
    STAR(64, "⭐", "Yıldız", Color(0xFFFF1744), Color(0xFFFF80AB)),
    UNIVERSE(128, "🌈", "Gökkuşağı", Color(0xFFD500F9), Color(0xFFEA80FC)),
    DIAMOND(256, "👑", "Taç", Color(0xFFAA00FF), Color(0xFFE040FB));

    fun nextTier(): ElementTier? {
        val nextIndex = ordinal + 1
        val all = values()
        return if (nextIndex < all.size) all[nextIndex] else null
    }

    companion object {
        fun fromValue(value: Int): ElementTier? = values().firstOrNull { it.value == value }
    }
}
