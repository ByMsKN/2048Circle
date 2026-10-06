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
     * KUSURSUZ 1 ADIM DÖNDÜRME & ÇARPIŞMA (1-Step Circular Physics):
     * 
     * Saat Yönü (CLOCKWISE):
     * - Yuva i'deki taş, yuva (i + 1) % n'e doğru 1 adım döner.
     * 
     * Saat Yönünün Tersi (COUNTER_CLOCKWISE):
     * - Yuva i'deki taş, yuva (i - 1 + n) % n'e doğru 1 adım döner.
     * 
     * Birleşme:
     * - Eğer hedef yuva doluysa ve aynı elementse -> 1 üst seviyeye birleşir!
     * - Eğer hedef yuva boşsa -> Taş oraya kayar.
     */
    fun processMove(slots: List<ElementTier?>, direction: MoveDirection): TurnResult {
        val n = slots.size
        var pointsEarned = 0
        var mergedCount = 0
        val step = if (direction == MoveDirection.CLOCKWISE) 1 else -1

        // Adım 1: Bütün taşları 1 adım ilerlet
        val shifted = MutableList<ElementTier?>(n) { null }
        for (i in 0 until n) {
            val target = (i + step + n) % n
            shifted[target] = slots[i]
        }

        // Adım 2: Çarpışma / Birleşme (Dönüş yönünde yan yana gelen aynı taşlar)
        val finalSlots = shifted.toMutableList()
        val merged = BooleanArray(n) { false }

        for (k in 0 until n) {
            val from = if (direction == MoveDirection.CLOCKWISE) (n - 1 - k) else k
            val to = (from + step + n) % n

            val fromElem = finalSlots[from]
            val toElem = finalSlots[to]

            if (fromElem != null && toElem != null && fromElem == toElem && !merged[from] && !merged[to]) {
                val nextTier = toElem.nextTier()
                if (nextTier != null) {
                    finalSlots[to] = nextTier
                    finalSlots[from] = null
                    merged[to] = true
                    pointsEarned += nextTier.value
                    mergedCount++
                }
            }
        }

        return TurnResult(finalSlots, pointsEarned, mergedCount)
    }

    /**
     * Öngörülebilir Yeni Element Girişi:
     * Yeni taş daima dönüşün başlangıç kapısından (Portal) oyuna girer.
     * CLOCKWISE ise Yuva 0 (en üst), COUNTER_CLOCKWISE ise Yuva (n - 1).
     * Eğer portal doluysa en yakın ilk boş yuvaya yerleşir.
     */
    fun spawnAtPortal(slots: List<ElementTier?>, direction: MoveDirection): List<ElementTier?> {
        val emptyIndices = slots.indices.filter { slots[it] == null }
        if (emptyIndices.isEmpty()) return slots

        val portal = if (direction == MoveDirection.CLOCKWISE) 0 else (slots.size - 1)
        val targetIndex = if (slots[portal] == null) {
            portal
        } else {
            emptyIndices.minByOrNull { Math.abs(it - portal) } ?: emptyIndices.first()
        }

        val spawned = if (Random.nextFloat() < 0.85f) ElementTier.WATER else ElementTier.STEAM

        return slots.toMutableList().apply {
            this[targetIndex] = spawned
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
