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
     * TEK ÇEMBER (8 YUVA) AKICI 2048 ÇEVİRME & BİRLEŞTİRME:
     * 
     * Saat Yönü (CLOCKWISE / Sağa):
     * Taşlar saat yönünde 1 adım ilerler. Yan yana gelen aynı sayılar birleşir (2+2=4, 4+4=8...).
     * 
     * Saat Yönünün Tersi (COUNTER_CLOCKWISE / Sola):
     * Taşlar ters yönde 1 adım ilerler ve birleşir.
     */
    fun processMove(slots: List<ElementTier?>, direction: MoveDirection): TurnResult {
        val n = slots.size
        val step = if (direction == MoveDirection.CLOCKWISE) 1 else -1

        // 1. Taşları yön doğrultusunda 1 adım ilerlet
        val shifted = MutableList<ElementTier?>(n) { null }
        for (i in 0 until n) {
            val target = (i + step + n) % n
            shifted[target] = slots[i]
        }

        // 2. Çarpışma ve birleşme kontrolü (aynı sayılar birleşir)
        val finalSlots = shifted.toMutableList()
        val merged = BooleanArray(n) { false }
        var pointsEarned = 0
        var mergedCount = 0

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

        val hasMoved = (finalSlots != slots)
        return TurnResult(finalSlots, pointsEarned, mergedCount, hasMoved)
    }

    /**
     * Yeni 2 veya 4 taşının gelişi:
     * Dönüş yönünün başlangıç noktasından (Portal) veya en yakın boş yuvaya yerleşir.
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

        val spawned = if (Random.nextFloat() < 0.90f) ElementTier.T_2 else ElementTier.T_4

        return slots.toMutableList().apply {
            this[targetIndex] = spawned
        }
    }

    /**
     * Boş yuvalara rastgele taş ekleme (Yeni oyun başlangıcı için)
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
     * Oyun bitti mi kontrolü
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
