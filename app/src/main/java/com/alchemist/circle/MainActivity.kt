package com.alchemist.circle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alchemist.circle.data.ScorePreferences
import com.alchemist.circle.ui.AlchemyCircleScreen
import com.alchemist.circle.viewmodel.AlchemyCircleViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val scorePreferences = ScorePreferences(applicationContext)
            val viewModel: AlchemyCircleViewModel = viewModel {
                AlchemyCircleViewModel(scorePreferences)
            }
            AlchemyCircleScreen(viewModel = viewModel)
        }
    }
}
