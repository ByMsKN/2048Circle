package com.alchemist.circle.engine

import com.alchemist.circle.model.ElementTier
import com.alchemist.circle.model.MoveDirection
import kotlin.random.Random

class Circular2048Engine {

    data class TurnResult(
        val newSlots: List<ElementTier?>,
        val pointsEarned: Int,
        val mergedCount: Int,
        val hasChanged: Boolean
    )

    /**
     * Dairesel itme ve birleşme:
     * Taşlar kendi aralarındaki boşluklar boyunca yön doğrultusunda kayar (rotation shift).
     * Önündeki boşluklara doğru ilerler, aynı değere rastlarsa birleşir.
     * Bu sayede taşlar sıfırdan 0. indekse toplanmaz, dairesel yerleri yönün akışına göre kayar!
     */
    fun processMove(slots: List<ElementTier?>, direction: MoveDirection): TurnResult {
        val n = slots.size
        val current = slots.toMutableList()
        var pointsEarned = 0
        var mergedCount = 0
        var anyMoved = false

        // Adım 1: Boşluklara doğru dairesel kaydırma (Shift)
        // Yöne göre komşu yuva indeksi:
        // CLOCKWISE (Saat yönü): Taş (i) -> (i + 1) % n yönüne doğru akar.
        // COUNTER_CLOCKWISE: Taş (i) -> (i - 1 + n) % n yönüne doğru akar.
        val step = if (direction == MoveDirection.CLOCKWISE) 1 else -1

        // Taşları boş olan komşu yuvalara doğru it (Bubbling / Shift)
        for (pass in 0 until n) {
            var shiftedInPass = false
            for (k in 0 until n) {
                val from = if (direction == MoveDirection.CLOCKWISE) (n - 1 - k) else k
                val to = (from + step + n) % n

                if (current[from] != null && current[to] == null) {
                    current[to] = current[from]
                    current[from] = null
                    shiftedInPass = true
                    anyMoved = true
                }
            }
            if (!shiftedInPass) break
        }

        // Adım 2: Aynı olan bitişik elementleri hareket yönünde birleştir
        val mergedIndices = BooleanArray(n) { false }
        for (k in 0 until n) {
            val from = if (direction == MoveDirection.CLOCKWISE) (n - 1 - k) else k
            val to = (from + step + n) % n

            val fromVal = current[from]
            val toVal = current[to]

            if (fromVal != null && toVal != null && fromVal == toVal && !mergedIndices[to] && !mergedIndices[from]) {
                val next = toVal.nextTier()
                if (next != null) {
                    current[to] = next
                    current[from] = null
                    mergedIndices[to] = true
                    pointsEarned += next.value
                    mergedCount++
                    anyMoved = true
                }
            }
        }

        // Adım 3: Birleşme sonrası açılan boşlukları bir kez daha yön doğrultusunda sıkıştır
        if (mergedCount > 0) {
            for (pass in 0 until n) {
                var shiftedInPass = false
                for (k in 0 until n) {
                    val from = if (direction == MoveDirection.CLOCKWISE) (n - 1 - k) else k
                    val to = (from + step + n) % n

                    if (current[from] != null && current[to] == null) {
                        current[to] = current[from]
                        current[from] = null
                        shiftedInPass = true
                    }
                }
                if (!shiftedInPass) break
            }
        }

        val hasChanged = current != slots
        return TurnResult(current, pointsEarned, mergedCount, hasChanged)
    }

    /**
     * Hamle yapıldıktan sonra boş kalan rastgele bir yuvaya yeni element ekler.
     */
    fun spawnNewElement(slots: List<ElementTier?>): List<ElementTier?> {
        val emptyIndices = slots.indices.filter { slots[it] == null }
        if (emptyIndices.isEmpty()) return slots

        val targetIndex = emptyIndices[Random.nextInt(emptyIndices.size)]
        val spawnedTier = if (Random.nextFloat() < 0.85f) ElementTier.WATER else ElementTier.STEAM

        return slots.toMutableList().apply {
            this[targetIndex] = spawnedTier
        }
    }

    /**
     * Boş yuva kalmadıysa ve hiçbir bitişik komşu aynı değilse oyun biter.
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
