package com.alchemist.circle.engine

import com.alchemist.circle.model.ElementTier
import com.alchemist.circle.model.MoveDirection
import kotlin.random.Random

class Circular2048Engine {

    data class TurnResult(
        val newSlots: List<ElementTier?>,
        val pointsEarned: Int,
        val mergedCount: Int
    )

    /**
     * KULLANICININ TAM KONTROLÜNDE OLAN DÖNDÜRME MEKANİĞİ:
     * 
     * Saat yönünde (CLOCKWISE) çevrildiğinde:
     * - Yuva 0 -> Yuva 1'e gider
     * - Yuva 1 -> Yuva 2'ye gider
     * - ...
     * - Yuva 9 -> Yuva 0'a gider.
     * 
     * Saat yönünün tersine (COUNTER_CLOCKWISE) çevrildiğinde:
     * - Yuva 1 -> Yuva 0'a gider
     * - Yuva 0 -> Yuva 9'a gider.
     * 
     * Birleşme Kuralı:
     * Eğer iki komşu taş aynıysa ve hareket yönünde birbirlerine doğru dönüyorlarsa,
     * hedef yuvada bir üst elemente evrilirler!
     */
    fun processMove(slots: List<ElementTier?>, direction: MoveDirection): TurnResult {
        val n = slots.size
        var pointsEarned = 0
        var mergedCount = 0

        val step = if (direction == MoveDirection.CLOCKWISE) 1 else -1

        // 1. Her taş yön doğrultusunda tam 1 yuva ilerler:
        val rotated = MutableList<ElementTier?>(n) { null }
        for (i in 0 until n) {
            val targetIdx = (i + step + n) % n
            rotated[targetIdx] = slots[i]
        }

        // 2. Birleşme kontrolü:
        // Eğer hareket sonrasında komşu olan iki taş aynı değere sahipse,
        // hareketin vardığı öndeki yuvada birleşirler!
        val result = rotated.toMutableList()
        val merged = BooleanArray(n) { false }

        for (k in 0 until n) {
            val i = if (direction == MoveDirection.CLOCKWISE) (n - 1 - k) else k
            val nextIdx = (i + step + n) % n

            val currentElem = result[i]
            val nextElem = result[nextIdx]

            if (currentElem != null && nextElem != null && currentElem == nextElem && !merged[i] && !merged[nextIdx]) {
                val upgraded = nextElem.nextTier()
                if (upgraded != null) {
                    result[nextIdx] = upgraded
                    result[i] = null
                    merged[nextIdx] = true
                    pointsEarned += upgraded.value
                    mergedCount++
                }
            }
        }

        return TurnResult(result, pointsEarned, mergedCount)
    }

    /**
     * SABİT VE ÖNGÖRÜLEBİLİR YENİ ELEMENT DOĞMA NOKTASI:
     * Yeni taşlar rastgele bir yere ışınlanmaz!
     * Döndürme yapıldığında, hareketin başlangıç noktasına (kuyruğuna) yeni taş girer.
     * 
     * Saat yönünde çevrildiğinde: Taşlar 0 -> 1 -> 2 diye aktığı için,
     * en tepedeki Yuva 0 boşalır ve yeni taş Yuva 0'dan oyuna girer.
     * Eğer Yuva 0 doluysa, en yakın boş komşu yuvadan girer.
     */
    fun spawnNewElementAtPortal(slots: List<ElementTier?>, direction: MoveDirection): List<ElementTier?> {
        val emptyIndices = slots.indices.filter { slots[it] == null }
        if (emptyIndices.isEmpty()) return slots

        // Sabit portal girişi: Saat yönünde Yuva 0 (en tepe), Ters yönde Yuva 9
        val preferredPortal = if (direction == MoveDirection.CLOCKWISE) 0 else (slots.size - 1)
        val targetIndex = if (slots[preferredPortal] == null) {
            preferredPortal
        } else {
            // Tercih edilen portal doluysa boş olan en yakın yuvaya yerleştir
            emptyIndices.minByOrNull { Math.abs(it - preferredPortal) } ?: emptyIndices.first()
        }

        val spawnedTier = if (Random.nextFloat() < 0.85f) ElementTier.WATER else ElementTier.STEAM

        return slots.toMutableList().apply {
            this[targetIndex] = spawnedTier
        }
    }

    /**
     * Oyun sonu kontrolü:
     */
    fun isGameOver(slots: List<ElementTier?>): Boolean {
        if (slots.any { it == null }) return false

        val n = slots.size
        for (i in 0 until n) {
            val curr = slots[i]
            val next = slots[(i + 1) % n]
            if (curr == next) return false
        }
        return true
    }
}
