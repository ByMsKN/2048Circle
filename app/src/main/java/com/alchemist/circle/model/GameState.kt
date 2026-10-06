package com.alchemist.circle.model

data class GameState(
    val slots: List<ElementTier?> = List(SLOT_COUNT) { null },
    val score: Int = 0,
    val bestScore: Int = 0,
    val cauldronCharge: Float = 0f,
    val lightningTargets: List<Int> = emptyList(),
    val isGameOver: Boolean = false
) {
    companion object {
        // 8 yuva: 45 derecelik kusursuz simetri, ferah aralıklar ve mükemmel 2048 birleşme matematiği
        const val SLOT_COUNT = 8
    }
}
