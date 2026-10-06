package com.alchemist.circle.model

data class GameState(
    val slots: List<ElementTier?> = List(SLOT_COUNT) { null },
    val score: Int = 0,
    val bestScore: Int = 0,
    val hammerCharge: Float = 0f,
    val isHammerActive: Boolean = false,
    val isGameOver: Boolean = false,
    val rotationAngleDegrees: Float = 0f
) {
    companion object {
        const val SLOT_COUNT = 8
    }
}
