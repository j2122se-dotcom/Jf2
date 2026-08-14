package com.jf2.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

private enum class GameScreen {
    MAIN_MENU,
    STORY,
    ONLINE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var screen by remember { mutableStateOf(GameScreen.MAIN_MENU) }

            MaterialTheme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (screen) {
                        GameScreen.MAIN_MENU -> {
                            Text("JF2", style = MaterialTheme.typography.displayMedium)
                            Button(onClick = { screen = GameScreen.STORY }) {
                                Text("Modo História")
                            }
                            Button(onClick = { screen = GameScreen.ONLINE }) {
                                Text("Modo Online")
                            }
                        }

                        GameScreen.STORY -> {
                            Text("Modo História")
                            Text("Seleção de personagem — próxima etapa")
                            Button(onClick = { screen = GameScreen.MAIN_MENU }) {
                                Text("Voltar")
                            }
                        }

                        GameScreen.ONLINE -> {
                            Text("Modo Online")
                            Text("Salas e bots — próxima etapa")
                            Button(onClick = { screen = GameScreen.MAIN_MENU }) {
                                Text("Voltar")
                            }
                        }
                    }
                }
            }
        }
    }
}
