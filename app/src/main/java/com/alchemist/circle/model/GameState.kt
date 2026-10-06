package com.alchemist.circle.model

data class GameState(
    val slots: List<ElementTier?> = List(SLOT_COUNT) { null },
    val score: Int = 0,
    val bestScore: Int = 0,
    val cauldronCharge: Float = 0f,
    val lightningTargets: List<Int> = emptyList(),
    val isGameOver: Boolean = false,
    val won2048: Boolean = false
) {
    companion object {
        const val SLOT_COUNT = 8 // Tek çember, 8 geniş ve ferah yuva (45 derece)
    }
}
