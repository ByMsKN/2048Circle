package com.alchemist.circle.model

import androidx.compose.ui.graphics.Color

enum class ElementTier(
    val value: Int,
    val symbol: String,
    val title: String,
    val color: Color,
    val textColor: Color = Color.White
) {
    T_2(2, "2", "2", Color(0xFFEEE4DA), Color(0xFF776E65)),
    T_4(4, "4", "4", Color(0xFFEDE0C8), Color(0xFF776E65)),
    T_8(8, "8", "8", Color(0xFFF2B179), Color(0xFFF9F6F2)),
    T_16(16, "16", "16", Color(0xFFF59563), Color(0xFFF9F6F2)),
    T_32(32, "32", "32", Color(0xFFF67C5F), Color(0xFFF9F6F2)),
    T_64(64, "64", "64", Color(0xFFF65E3B), Color(0xFFF9F6F2)),
    T_128(128, "128", "128", Color(0xFFEDCF72), Color(0xFFF9F6F2)),
    T_256(256, "256", "256", Color(0xFFEDCC61), Color(0xFFF9F6F2)),
    T_512(512, "512", "512", Color(0xFFEDC850), Color(0xFFF9F6F2)),
    T_1024(1024, "1024", "1024", Color(0xFFEDC53F), Color(0xFFF9F6F2)),
    T_2048(2048, "2048", "2048", Color(0xFFEDC22E), Color(0xFFF9F6F2));

    fun nextTier(): ElementTier? {
        val nextIndex = ordinal + 1
        val all = values()
        return if (nextIndex < all.size) all[nextIndex] else null
    }

    companion object {
        fun fromValue(value: Int): ElementTier? = values().firstOrNull { it.value == value }
    }
}
