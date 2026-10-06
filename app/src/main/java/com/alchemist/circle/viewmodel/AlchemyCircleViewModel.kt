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

    private var isAnimating = false

    init {
        startNewGame()
    }

    fun startNewGame() {
        val initialSlots: MutableList<ElementTier?> = MutableList(GameState.SLOT_COUNT) { null }
        // Başlangıçta tam 2 adet taş: Biri en tepede (0), diğeri saat 3 yönünde (2)
        initialSlots[0] = ElementTier.WATER
        initialSlots[2] = ElementTier.WATER

        _uiState.value = GameState(
            slots = initialSlots,
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
        if (state.isGameOver || isAnimating) return

        isAnimating = true
        viewModelScope.launch {
            // Adım 1: Kullanıcı butona bastığında veya kaydırdığında
            // Çember fiziksel olarak gözünüzün önünde 36 derece DÖNER!
            val deltaAngle = if (direction == MoveDirection.CLOCKWISE) 36f else -36f
            val targetAngle = state.rotationAngleDegrees + deltaAngle
            _uiState.update { it.copy(rotationAngleDegrees = targetAngle) }

            // Animasyon süresini bekle (akıcı geçiş)
            delay(220)

            // Adım 2: Çember tam yerine oturduğunda taşların yeni pozisyonunu ve birleşmeleri hesapla:
            val result = engine.processMove(state.slots, direction)

            // Adım 3: Sabit portaldan (en tepeden) yeni taş oyuna girer:
            val updatedSlots = engine.spawnNewElementAtPortal(result.newSlots, direction)
            val newScore = state.score + result.pointsEarned
            scorePreferences.saveBestScore(newScore)

            val chargeBonus = result.mergedCount * 0.25f
            val newCharge = (state.cauldronCharge + chargeBonus).coerceAtMost(1.0f)
            val gameOver = engine.isGameOver(updatedSlots)

            _uiState.update {
                it.copy(
                    slots = updatedSlots,
                    score = newScore,
                    bestScore = maxOf(newScore, it.bestScore),
                    cauldronCharge = newCharge,
                    isGameOver = gameOver
                )
            }

            // Güç kazanı %100 dolduysa yıldırım fırlat
            if (newCharge >= 1.0f) {
                triggerCauldronDischarge()
            } else {
                isAnimating = false
            }
        }
    }

    private fun triggerCauldronDischarge() {
        viewModelScope.launch {
            delay(200)
            val currentSlots = _uiState.value.slots
            val filledIndices = currentSlots.indices.filter { currentSlots[it] != null }

            if (filledIndices.isNotEmpty()) {
                val targets = filledIndices.shuffled().take(2)
                _uiState.update { it.copy(lightningTargets = targets) }

                delay(650) // Yıldırım patlama efekti

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
            isAnimating = false
        }
    }
}
