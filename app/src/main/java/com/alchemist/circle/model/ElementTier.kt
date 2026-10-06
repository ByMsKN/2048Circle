package com.alchemist.circle.model

import androidx.compose.ui.graphics.Color

enum class ElementTier(
    val value: Int,
    val symbol: String,
    val title: String,
    val color: Color,
    val glowColor: Color
) {
    WATER(2, "💧", "Su", Color(0xFF1E88E5), Color(0x6642A5F5)),
    STEAM(4, "💨", "Buhar", Color(0xFF26A69A), Color(0x6680CBC4)),
    CLOUD(8, "☁️", "Bulut", Color(0xFF78909C), Color(0x66B0BEC5)),
    ENERGY(16, "⚡", "Enerji", Color(0xFFFFB300), Color(0x66FFE082)),
    PLASMA(32, "🔥", "Plazma", Color(0xFFE64A19), Color(0x66FF8A65)),
    STAR(64, "✨", "Yıldız", Color(0xFF8E24AA), Color(0x66BA68C8)),
    UNIVERSE(128, "🌌", "Evren", Color(0xFF3949AB), Color(0x667986CB)),
    DIAMOND(256, "💎", "Elmas", Color(0xFF00ACC1), Color(0x664DD0E1));

    fun nextTier(): ElementTier? {
        val nextIndex = ordinal + 1
        val all = values()
        return if (nextIndex < all.size) all[nextIndex] else null
    }

    companion object {
        fun fromValue(value: Int): ElementTier? = values().firstOrNull { it.value == value }
    }
}
