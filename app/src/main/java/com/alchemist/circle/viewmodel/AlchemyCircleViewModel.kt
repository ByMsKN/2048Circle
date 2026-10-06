package com.alchemist.circle.viewmodel

import androidx.lifecycle.ViewModel
import com.alchemist.circle.data.ScorePreferences
import com.alchemist.circle.engine.Circular2048Engine
import com.alchemist.circle.model.ElementTier
import com.alchemist.circle.model.GameState
import com.alchemist.circle.model.MoveDirection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AlchemyCircleViewModel(
    private val scorePreferences: ScorePreferences,
    private val engine: Circular2048Engine = Circular2048Engine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    init {
        startNewGame()
    }

    fun startNewGame() {
        val initialSlots = List(GameState.SLOT_COUNT) { null }
        val slotsWithFirst = engine.spawnNewElement(initialSlots)
        val slotsWithSecond = engine.spawnNewElement(slotsWithFirst)

        _uiState.value = GameState(
            slots = slotsWithSecond,
            score = 0,
            bestScore = scorePreferences.getBestScore(),
            hammerCharge = 0f,
            isHammerActive = false,
            isGameOver = false,
            rotationAngleDegrees = 0f
        )
    }

    fun makeMove(direction: MoveDirection) {
        val state = _uiState.value
        if (state.isGameOver || state.isHammerActive) return

        val result = engine.processMove(state.slots, direction)

        if (result.hasChanged) {
            val updatedSlots = engine.spawnNewElement(result.newSlots)
            val newScore = state.score + result.pointsEarned
            scorePreferences.saveBestScore(newScore)

            val chargeBonus = result.mergedCount * 0.20f
            val newCharge = (state.hammerCharge + chargeBonus).coerceAtMost(1.0f)

            val deltaAngle = if (direction == MoveDirection.CLOCKWISE) 45f else -45f
            val newAngle = state.rotationAngleDegrees + deltaAngle

            val gameOver = engine.isGameOver(updatedSlots)

            _uiState.update {
                it.copy(
                    slots = updatedSlots,
                    score = newScore,
                    bestScore = maxOf(newScore, it.bestScore),
                    hammerCharge = newCharge,
                    rotationAngleDegrees = newAngle,
                    isGameOver = gameOver
                )
            }
        }
    }

    fun toggleHammerMode() {
        val current = _uiState.value
        if (current.hammerCharge >= 1.0f) {
            _uiState.update { it.copy(isHammerActive = !it.isHammerActive) }
        }
    }

    fun onSlotTapped(index: Int) {
        val state = _uiState.value
        if (state.isHammerActive && state.slots[index] != null) {
            val updatedSlots = state.slots.toMutableList().apply {
                this[index] = null
            }
            _uiState.update {
                it.copy(
                    slots = updatedSlots,
                    hammerCharge = 0f,
                    isHammerActive = false,
                    isGameOver = false
                )
            }
        }
    }
}
