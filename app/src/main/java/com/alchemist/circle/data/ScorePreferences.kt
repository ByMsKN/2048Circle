package com.alchemist.circle.data

import android.content.Context
import android.content.SharedPreferences

class ScorePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("alchemy_circle_prefs", Context.MODE_PRIVATE)

    fun getBestScore(): Int = prefs.getInt(KEY_BEST_SCORE, 0)

    fun saveBestScore(score: Int) {
        if (score > getBestScore()) {
            prefs.edit().putInt(KEY_BEST_SCORE, score).apply()
        }
    }

    companion object {
        private const val KEY_BEST_SCORE = "best_score"
    }
}
