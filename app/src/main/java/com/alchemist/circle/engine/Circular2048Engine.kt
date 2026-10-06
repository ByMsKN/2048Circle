package com.alchemist.circle.engine

import com.alchemist.circle.model.ElementTier
import com.alchemist.circle.model.GameState
import com.alchemist.circle.model.MoveDirection
import kotlin.random.Random

class Circular2048Engine {

    data class TurnResult(
        val newSlots: List<ElementTier?>,
        val pointsEarned: Int,
        val mergedCount: Int,
        val hasMoved: Boolean
    )

    /**
     * EŞMERKEZLİ 3 HALKALI 2048 HAREKET MOTORU:
     * 
     * 1) CLOCKWISE (Saat Yönü):
     *    Her halkadaki 8 sektör kendi içinde saat yönünde kayar ve birleşir.
     * 
     * 2) COUNTER_CLOCKWISE (Ters Yön):
     *    Her halkadaki 8 sektör kendi içinde saat yönünün tersine kayar ve birleşir.
     * 
     * 3) INWARD (İçe / Merkeze Doğru):
     *    Her bir 8 sektör (sütun) boyunca dış halkadan iç halkaya doğru taşlar merkeze kayar ve birleşir.
     *    (Dış -> Orta -> İç)
     * 
     * 4) OUTWARD (Dışa Doğru):
     *    Her bir 8 sektör boyunca iç halkadan dış halkaya doğru taşlar kenarlara kayar ve birleşir.
     *    (İç -> Orta -> Dış)
     */
    fun processMove(slots: List<ElementTier?>, direction: MoveDirection): TurnResult {
        val mutableSlots = slots.toMutableList()
        var totalPoints = 0
        var totalMerged = 0
        var hasChanged = false

        when (direction) {
            MoveDirection.CLOCKWISE, MoveDirection.COUNTER_CLOCKWISE -> {
                // Her halka bağımsız bir çember olarak döner ve kayar
                for (r in 0 until GameState.RINGS) {
                    val ringList = (0 until GameState.SLOTS_PER_RING).map { s ->
                        slots[GameState.getIndex(r, s)]
                    }

                    val (newRingList, pts, merges, changed) = slideAndMergeRing(
                        ringList,
                        isClockwise = (direction == MoveDirection.CLOCKWISE)
                    )

                    totalPoints += pts
                    totalMerged += merges
                    if (changed) hasChanged = true

                    for (s in 0 until GameState.SLOTS_PER_RING) {
                        mutableSlots[GameState.getIndex(r, s)] = newRingList[s]
                    }
                }
            }

            MoveDirection.INWARD, MoveDirection.OUTWARD -> {
                // Her sektör (açı) boyunca içe veya dışa radyal kayma
                for (s in 0 until GameState.SLOTS_PER_RING) {
                    // radialList: [İç (0), Orta (1), Dış (2)]
                    val radialList = (0 until GameState.RINGS).map { r ->
                        slots[GameState.getIndex(r, s)]
                    }

                    // INWARD: Dıştan İçe doğru kayar -> index 2 -> 1 -> 0 (hedef: index 0)
                    // OUTWARD: İçten Dışa doğru kayar -> index 0 -> 1 -> 2 (hedef: index 2)
                    val (newRadialList, pts, merges, changed) = slideAndMergeRadial(
                        radialList,
                        toInner = (direction == MoveDirection.INWARD)
                    )

                    totalPoints += pts
                    totalMerged += merges
                    if (changed) hasChanged = true

                    for (r in 0 until GameState.RINGS) {
                        mutableSlots[GameState.getIndex(r, s)] = newRadialList[r]
                    }
                }
            }
        }

        return TurnResult(mutableSlots, totalPoints, totalMerged, hasChanged)
    }

    /**
     * Dairesel bir halkada klasik 2048 sıkıştırma ve birleştirme:
     * Dairesel listede boşluklar kapanır, ardışık aynı sayılar birleşir.
     */
    private fun slideAndMergeRing(
        ring: List<ElementTier?>,
        isClockwise: Boolean
    ): RingResult {
        val n = ring.size
        // Yön sıralaması:
        // CLOCKWISE ise taşlar saat yönünde akar (indis artış yönü: 0 -> 1 -> 2... veya son dolu taşa doğru)
        // 2048 kuralı: Mevcut dolu taşları toplayıp sırayla çarpıştır
        val nonNull = ring.filterNotNull()
        if (nonNull.isEmpty()) {
            return RingResult(ring, 0, 0, false)
        }

        // Dairesel sıkıştırmada döngü:
        // Saat yönünde veya tersinde sıralı akış
        val orderedItems = if (isClockwise) nonNull else nonNull.reversed()
        val mergedList = mutableListOf<ElementTier>()
        var skip = false
        var points = 0
        var merges = 0

        for (i in 0 until orderedItems.size) {
            if (skip) {
                skip = false
                continue
            }
            val curr = orderedItems[i]
            val next = if (i + 1 < orderedItems.size) orderedItems[i + 1] else null

            if (next != null && curr == next) {
                val nextTier = curr.nextTier()
                if (nextTier != null) {
                    mergedList.add(nextTier)
                    points += nextTier.value
                    merges++
                    skip = true
                } else {
                    mergedList.add(curr)
                }
            } else {
                mergedList.add(curr)
            }
        }

        val resultOrdered = if (isClockwise) mergedList else mergedList.reversed()
        // Sonuçları orijinal halkanın akış yönündeki yuvalarına yerleştir
        val newRing = MutableList<ElementTier?>(n) { null }
        for (i in resultOrdered.indices) {
            newRing[i] = resultOrdered[i]
        }

        val changed = (newRing != ring)
        return RingResult(newRing, points, merges, changed)
    }

    private data class RingResult(
        val list: List<ElementTier?>,
        val points: Int,
        val merges: Int,
        val changed: Boolean
    )

    /**
     * Radyal doğrultuda (İç <-> Orta <-> Dış) 3 hücreli klasik 2048 kaydırma ve birleştirme:
     * toInner=true: 2 -> 1 -> 0 yönünde kayar (0 en iç)
     * toInner=false: 0 -> 1 -> 2 yönünde kayar (2 en dış)
     */
    private fun slideAndMergeRadial(
        radial: List<ElementTier?>,
        toInner: Boolean
    ): RingResult {
        // radial[0] = Inner, radial[1] = Middle, radial[2] = Outer
        val nonNull = (if (toInner) radial else radial.reversed()).filterNotNull()
        if (nonNull.isEmpty()) return RingResult(radial, 0, 0, false)

        val merged = mutableListOf<ElementTier>()
        var skip = false
        var points = 0
        var merges = 0

        for (i in nonNull.indices) {
            if (skip) {
                skip = false
                continue
            }
            val curr = nonNull[i]
            val next = if (i + 1 < nonNull.size) nonNull[i + 1] else null

            if (next != null && curr == next) {
                val nextTier = curr.nextTier()
                if (nextTier != null) {
                    merged.add(nextTier)
                    points += nextTier.value
                    merges++
                    skip = true
                } else {
                    merged.add(curr)
                }
            } else {
                merged.add(curr)
            }
        }

        // toInner ise merged elemanları 0, 1, 2 sırasıyla yerleşir
        // toOuter ise tersine (dışa) yerleşir
        val result = MutableList<ElementTier?>(3) { null }
        if (toInner) {
            for (i in merged.indices) {
                result[i] = merged[i]
            }
        } else {
            // Dışa yerleşme: en dış (index 2) den başlayarak geriye doldur
            var rIdx = 2
            for (elem in merged.reversed()) {
                result[rIdx--] = elem
            }
        }

        val changed = (result != radial)
        return RingResult(result, points, merges, changed)
    }

    /**
     * Boş yuvalardan birine yeni taş ekle (Klasik 2048 gibi %90 ihtimalle 2, %10 ihtimalle 4)
     */
    fun spawnRandomTile(slots: List<ElementTier?>): List<ElementTier?> {
        val emptyIndices = slots.indices.filter { slots[it] == null }
        if (emptyIndices.isEmpty()) return slots

        val targetIndex = emptyIndices.random()
        val spawned = if (Random.nextFloat() < 0.90f) ElementTier.T_2 else ElementTier.T_4

        return slots.toMutableList().apply {
            this[targetIndex] = spawned
        }
    }

    /**
     * Hamle kalıp kalmadığını kontrol eder (Oyun bitti mi?)
     */
    fun isGameOver(slots: List<ElementTier?>): Boolean {
        // 1. Boş yuva varsa oyun bitmemiştir
        if (slots.any { it == null }) return false

        // 2. Halka içinde komşu aynı sayılar var mı? (Saat yönü komşuluk)
        for (r in 0 until GameState.RINGS) {
            for (s in 0 until GameState.SLOTS_PER_RING) {
                val curr = slots[GameState.getIndex(r, s)]
                val nextSector = (s + 1) % GameState.SLOTS_PER_RING
                val neighbor = slots[GameState.getIndex(r, nextSector)]
                if (curr == neighbor) return false
            }
        }

        // 3. Radyal yönde komşu aynı sayılar var mı? (İç-Dış komşuluk)
        for (s in 0 until GameState.SLOTS_PER_RING) {
            for (r in 0 until (GameState.RINGS - 1)) {
                val curr = slots[GameState.getIndex(r, s)]
                val neighbor = slots[GameState.getIndex(r + 1, s)]
                if (curr == neighbor) return false
            }
        }

        return true
    }
}
