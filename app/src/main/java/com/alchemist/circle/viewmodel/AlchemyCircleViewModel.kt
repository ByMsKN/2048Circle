package com.alchemist.circle.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alchemist.circle.data.ScorePreferences
import com.alchemist.circle.engine.Circular2048Engine
import com.alchemist.circle.model.ElementTier
import com.alchemist.circle.model.GameState
import com.alchemist.circle.model.MoveDirection
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlchemyCircleViewModel(
    private val scorePreferences: ScorePreferences,
    private val engine: Circular2048Engine = Circular2048Engine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    private var isDischarging = false

    init {
        startNewGame()
    }

    fun startNewGame() {
        val initialSlots = List(GameState.SLOT_COUNT) { null }
        // Başlangıçta tam olarak 2 tanesi dolu olsun
        val slotsWithFirst = engine.spawnNewElement(initialSlots)
        val slotsWithSecond = engine.spawnNewElement(slotsWithFirst)

        _uiState.value = GameState(
            slots = slotsWithSecond,
            score = 0,
            bestScore = scorePreferences.getBestScore(),
            cauldronCharge = 0f,
            lightningTargets = emptyList(),
            isGameOver = false,
            rotationAngleDegrees = 0f
        )
    }

    fun makeMove(direction: MoveDirection) {
        val state = _uiState.value
        if (state.isGameOver || isDischarging) return

        val result = engine.processMove(state.slots, direction)

        if (result.hasChanged) {
            val updatedSlots = engine.spawnNewElement(result.newSlots)
            val newScore = state.score + result.pointsEarned
            scorePreferences.saveBestScore(newScore)

            // Her birleşme güç kazanını %25 doldurur
            val chargeBonus = result.mergedCount * 0.25f
            val newCharge = (state.cauldronCharge + chargeBonus).coerceAtMost(1.0f)

            // 10 daire olduğu için dönüş açısı 360/10 = 36 derece
            val deltaAngle = if (direction == MoveDirection.CLOCKWISE) 36f else -36f
            val newAngle = state.rotationAngleDegrees + deltaAngle

            val gameOver = engine.isGameOver(updatedSlots)

            _uiState.update {
                it.copy(
                    slots = updatedSlots,
                    score = newScore,
                    bestScore = maxOf(newScore, it.bestScore),
                    cauldronCharge = newCharge,
                    rotationAngleDegrees = newAngle,
                    isGameOver = gameOver
                )
            }

            // Güç kazanı %100 dolduysa otomatik yıldırım patlaması tetikle!
            if (newCharge >= 1.0f) {
                triggerCauldronDischarge()
            }
        }
    }

    private fun triggerCauldronDischarge() {
        isDischarging = true
        viewModelScope.launch {
            delay(350) // Dönüş animasyonu tamamlansın
            val currentSlots = _uiState.value.slots
            val filledIndices = currentSlots.indices.filter { currentSlots[it] != null }

            if (filledIndices.isNotEmpty()) {
                // Rastgele 2 dolu çemberi seç (2 veya daha az varsa hepsini)
                val targets = filledIndices.shuffled().take(2)

                // 1. Yıldırım hedeflerini ata (Animasyon gösterilsin)
                _uiState.update { it.copy(lightningTargets = targets) }

                delay(700) // Yıldırım görsel efekti sürsün

                // 2. Hedeflenen çemberleri yok et ve kazanı sıfırla
                val newSlots = _uiState.value.slots.toMutableList()
                targets.forEach { targetIndex ->
                    newSlots[targetIndex] = null
                }

                _uiState.update {
                    it.copy(
                        slots = newSlots,
                        cauldronCharge = 0f,
                        lightningTargets = emptyList(),
                        isGameOver = false // Alan açıldığı için asla Game Over kalmaz
                    )
                }
            } else {
                _uiState.update { it.copy(cauldronCharge = 0f) }
            }
            isDischarging = false
        }
    }
}
