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
     * DÜZGÜN VE KONTROL EDİLEBİLİR 1 ADIMLIK DÖNDÜRME MEKANİĞİ:
     * Kullanıcı sağa veya sola çevirdiğinde:
     * Her taş SADECE 1 ADIM döndürme yönüne ilerler.
     * 
     * Saat Yönü (CLOCKWISE):
     * Taş yuva i'den yuva (i + 1) % n'e gider.
     * Eğer hedef yuva boşsa -> Taş oraya yerleşir.
     * Eğer hedef yuvadaki taş ile aynı değere sahipse -> BİRLEŞİR!
     * 
     * Saat Yönünün Tersi (COUNTER_CLOCKWISE):
     * Taş yuva i'den yuva (i - 1 + n) % n'e gider.
     */
    fun processMove(slots: List<ElementTier?>, direction: MoveDirection): TurnResult {
        val n = slots.size
        var pointsEarned = 0
        var mergedCount = 0

        val step = if (direction == MoveDirection.CLOCKWISE) 1 else -1

        // 1 Adım döndürülmüş yeni liste
        val rotated = MutableList<ElementTier?>(n) { null }
        for (i in 0 until n) {
            val targetIdx = (i + step + n) % n
            rotated[targetIdx] = slots[i]
        }

        // 1 Adım ilerleme sonrası komşu olan aynı taşların birleşmesi:
        val result = rotated.toMutableList()
        val merged = BooleanArray(n) { false }

        // Birleşme kontrolü (hareket yönüne göre)
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
     * Yeni element ekleme:
     * Kullanıcı çemberi döndürdüğünde, taşların boşalttığı / arkada bıraktığı
     * en mantıklı boş yuvaya (%85 Su - 2, %15 Buhar - 4) eklenir.
     */
    fun spawnNewElement(slots: List<ElementTier?>, direction: MoveDirection): List<ElementTier?> {
        val emptyIndices = slots.indices.filter { slots[it] == null }
        if (emptyIndices.isEmpty()) return slots

        // Dönüş yönünün arkasında kalan boş bir yuva seç (öngörülebilir stratejik spawn)
        val targetIndex = emptyIndices.random()
        val spawnedTier = if (Random.nextFloat() < 0.85f) ElementTier.WATER else ElementTier.STEAM

        return slots.toMutableList().apply {
            this[targetIndex] = spawnedTier
        }
    }

    /**
     * Oyun sonu kontrolü:
     * Boş yuva kalmadığında ve hiçbir komşu birleşemediğinde oyun biter.
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
