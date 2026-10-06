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
        var slots: List<ElementTier?> = List(GameState.TOTAL_SLOTS) { null }
        // 2048 başlangıcı: 2 adet rastgele 2 veya 4 taşı
        slots = engine.spawnRandomTile(slots)
        slots = engine.spawnRandomTile(slots)

        _uiState.value = GameState(
            slots = slots,
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

        val result = engine.processMove(state.slots, direction)
        // Eğer tahtada hiçbir taş hareket etmedi veya birleşmediyse hamle geçersizdir (2048 kuralı)
        if (!result.hasMoved) return

        isBusy = true
        viewModelScope.launch {
            // Hamle yapıldıysa yeni rastgele taş girer
            val updatedSlots = engine.spawnRandomTile(result.newSlots)
            val newScore = state.score + result.pointsEarned
            scorePreferences.saveBestScore(newScore)

            val chargeBonus = result.mergedCount * 0.20f
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

            // Güç kazanı dolduğunda 2 taş temizleme jokeri
            if (newCharge >= 1.0f) {
                delay(300)
                triggerCauldronDischarge()
            } else {
                delay(100)
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
