package com.alchemist.circle.model

data class GameState(
    val slots: List<ElementTier?> = List(SLOT_COUNT) { null },
    val score: Int = 0,
    val bestScore: Int = 0,
    val cauldronCharge: Float = 0f, // 0.0f .. 1.0f (Güç Kazanı)
    val lightningTargets: List<Int> = emptyList(), // Yıldırım fırlatılan yuva indeksleri
    val isGameOver: Boolean = false,
    val rotationAngleDegrees: Float = 0f
) {
    companion object {
        const val SLOT_COUNT = 10
    }
}
