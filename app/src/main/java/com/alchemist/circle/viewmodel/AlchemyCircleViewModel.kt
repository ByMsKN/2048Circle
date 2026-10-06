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

    private var isBusy = false

    init {
        startNewGame()
    }

    fun startNewGame() {
        val initialSlots: MutableList<ElementTier?> = MutableList(GameState.SLOT_COUNT) { null }
        // 8 yuvada temiz başlangıç: Saat 12 (0) ve Saat 3 (2) yönünde iki adet "2" taşı
        initialSlots[0] = ElementTier.T_2
        initialSlots[2] = ElementTier.T_2

        _uiState.value = GameState(
            slots = initialSlots,
            score = 0,
            bestScore = scorePreferences.getBestScore(),
            cauldronCharge = 0f,
            lightningTargets = emptyList(),
            isGameOver = false,
            won2048 = false
        )
    }

    fun makeMove(direction: MoveDirection) {
        val state = _uiState.value
        if (state.isGameOver || isBusy) return

        isBusy = true
        viewModelScope.launch {
            // 1. Taşları yön doğrultusunda 1 adım ilerlet ve birleşmeleri hesapla
            val result = engine.processMove(state.slots, direction)

            // 2. Taşlar döndükten sonra arkadaki portaldan yeni 2/4 taşı girsin
            val updatedSlots = engine.spawnAtPortal(result.newSlots, direction)
            val newScore = state.score + result.pointsEarned
            scorePreferences.saveBestScore(newScore)

            val chargeBonus = result.mergedCount * 0.25f
            val newCharge = (state.cauldronCharge + chargeBonus).coerceAtMost(1.0f)
            val gameOver = engine.isGameOver(updatedSlots)
            val hasWon = updatedSlots.any { it == ElementTier.T_2048 }

            _uiState.update {
                it.copy(
                    slots = updatedSlots,
                    score = newScore,
                    bestScore = maxOf(newScore, it.bestScore),
                    cauldronCharge = newCharge,
                    isGameOver = gameOver,
                    won2048 = it.won2048 || hasWon
                )
            }

            // Güç kazanı %100 dolduğunda 2 taş temizleme jokeri
            if (newCharge >= 1.0f) {
                delay(300)
                triggerCauldronDischarge()
            } else {
                delay(120)
                isBusy = false
            }
        }
    }

    private fun triggerCauldronDischarge() {
        viewModelScope.launch {
            val currentSlots = _uiState.value.slots
            val filledIndices = currentSlots.indices.filter { currentSlots[it] != null }

            if (filledIndices.isNotEmpty()) {
                val targets = filledIndices.shuffled().take(2)
                _uiState.update { it.copy(lightningTargets = targets) }

                delay(600) // Yıldırım patlama efekti

                val newSlots = _uiState.value.slots.toMutableList()
                targets.forEach { targetIndex ->
                    newSlots[targetIndex] = null
                }

                _uiState.update {
                    it.copy(
                        slots = newSlots,
                        cauldronCharge = 0f,
                        lightningTargets = emptyList(),
                        isGameOver = false
                    )
                }
            } else {
                _uiState.update { it.copy(cauldronCharge = 0f) }
            }
            isBusy = false
        }
    }
}
