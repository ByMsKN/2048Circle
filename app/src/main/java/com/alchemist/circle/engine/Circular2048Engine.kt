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

    fun processMove(slots: List<ElementTier?>, direction: MoveDirection): TurnResult {
        val size = slots.size
        val workingList = if (direction == MoveDirection.CLOCKWISE) {
            slots.toMutableList()
        } else {
            slots.reversed().toMutableList()
        }

        var pointsEarned = 0
        var mergedCount = 0

        val nonNulls = workingList.filterNotNull().toMutableList()
        val merged = mutableListOf<ElementTier?>()
        var i = 0
        while (i < nonNulls.size) {
            if (i < nonNulls.size - 1 && nonNulls[i] == nonNulls[i + 1]) {
                val current = nonNulls[i]
                val next = current.nextTier()
                if (next != null) {
                    merged.add(next)
                    pointsEarned += next.value
                    mergedCount++
                } else {
                    merged.add(current)
                }
                i += 2
            } else {
                merged.add(nonNulls[i])
                i++
            }
        }

        while (merged.size < size) {
            merged.add(null)
        }

        val finalSlots = if (direction == MoveDirection.CLOCKWISE) {
            merged
        } else {
            merged.reversed()
        }

        val hasChanged = finalSlots != slots
        return TurnResult(finalSlots, pointsEarned, mergedCount, hasChanged)
    }

    fun spawnNewElement(slots: List<ElementTier?>): List<ElementTier?> {
        val emptyIndices = slots.indices.filter { slots[it] == null }
        if (emptyIndices.isEmpty()) return slots

        val targetIndex = emptyIndices[Random.nextInt(emptyIndices.size)]
        val spawnedTier = if (Random.nextFloat() < 0.85f) ElementTier.WATER else ElementTier.STEAM

        return slots.toMutableList().apply {
            this[targetIndex] = spawnedTier
        }
    }

    fun isGameOver(slots: List<ElementTier?>): Boolean {
        if (slots.any { it == null }) return false

        val n = slots.size
        for (i in 0 until n) {
            val current = slots[i]
            val next = slots[(i + 1) % n]
            if (current == next) return false
        }
        return true
    }
}
