package com.alchemist.circle.model

data class GameState(
    val slots: List<ElementTier?> = List(TOTAL_SLOTS) { null },
    val score: Int = 0,
    val bestScore: Int = 0,
    val cauldronCharge: Float = 0f,
    val lightningTargets: List<Int> = emptyList(),
    val isGameOver: Boolean = false,
    val won2048: Boolean = false
) {
    companion object {
        const val RINGS = 3           // 3 Eşmerkezli Halka: 0=İç (Inner), 1=Orta (Middle), 2=Dış (Outer)
        const val SLOTS_PER_RING = 8  // Her halkada 8 sektör (45 derece)
        const val TOTAL_SLOTS = RINGS * SLOTS_PER_RING // 24 Yuva

        fun getIndex(ring: Int, sector: Int): Int = ring * SLOTS_PER_RING + sector
        fun getRing(index: Int): Int = index / SLOTS_PER_RING
        fun getSector(index: Int): Int = index % SLOTS_PER_RING
    }
}
