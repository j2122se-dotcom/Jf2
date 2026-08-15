package com.jf2.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private enum class GameScreen {
    MAIN_MENU,
    STORY_CHARACTER_SELECT,
    STORY,
    ONLINE
}

private data class Hero(
    val name: String,
    val role: String,
    val description: String
)

private val heroes = listOf(
    Hero("Vanguarda", "Assalto", "Combatente equilibrado para a linha de frente."),
    Hero("Sentinela", "Defesa", "Especialista em proteger posições e aliados."),
    Hero("Batedor", "Mobilidade", "Rápido e ágil, ideal para flanquear inimigos."),
    Hero("Engenheiro", "Suporte", "Constrói equipamentos para ajudar a equipe."),
    Hero("Médico", "Suporte", "Mantém os aliados vivos durante o combate."),
    Hero("Artilheiro", "Pesado", "Grande poder de fogo, mas menor mobilidade.")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var screen by remember { mutableStateOf(GameScreen.MAIN_MENU) }
            var selectedHero by remember { mutableStateOf<Hero?>(null) }

            MaterialTheme {
                when (screen) {
                    GameScreen.MAIN_MENU -> MainMenu(
                        onStory = { screen = GameScreen.STORY_CHARACTER_SELECT },
                        onOnline = { screen = GameScreen.ONLINE }
                    )

                    GameScreen.STORY_CHARACTER_SELECT -> CharacterSelectionScreen(
                        selectedHero = selectedHero,
                        onSelect = { selectedHero = it },
                        onStart = { screen = GameScreen.STORY },
                        onBack = {
                            selectedHero = null
                            screen = GameScreen.MAIN_MENU
                        }
                    )

                    GameScreen.STORY -> StoryPlaceholder(
                        hero = selectedHero,
                        onBack = { screen = GameScreen.STORY_CHARACTER_SELECT }
                    )

                    GameScreen.ONLINE -> OnlinePlaceholder(
                        onBack = { screen = GameScreen.MAIN_MENU }
                    )
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun MainMenu(onStory: () -> Unit, onOnline: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("JF2", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onStory, modifier = Modifier.fillMaxWidth()) {
            Text("Modo História")
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onOnline, modifier = Modifier.fillMaxWidth()) {
            Text("Modo Online")
        }
    }
}

@androidx.compose.runtime.Composable
private fun CharacterSelectionScreen(
    selectedHero: Hero?,
    onSelect: (Hero) -> Unit,
    onStart: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("SELEÇÃO DE PERSONAGEM", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("Escolha seu herói para começar a campanha.")
        Spacer(Modifier.height(16.dp))

        heroes.forEach { hero ->
            HeroCard(
                hero = hero,
                selected = hero == selectedHero,
                onSelect = { onSelect(hero) }
            )
            Spacer(Modifier.height(10.dp))
        }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onStart,
            enabled = selectedHero != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (selectedHero == null) "Escolha um personagem" else "Começar campanha")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Voltar")
        }
    }
}

@androidx.compose.runtime.Composable
private fun HeroCard(hero: Hero, selected: Boolean, onSelect: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(hero.name, style = MaterialTheme.typography.titleLarge)
            Text(hero.role, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            Text(hero.description)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(onClick = onSelect) {
                    Text(if (selected) "Selecionado" else "Selecionar")
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun StoryPlaceholder(hero: Hero?, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Campanha", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("Personagem escolhido: ${hero?.name ?: "Nenhum"}")
        Spacer(Modifier.height(20.dp))
        Text("Próxima etapa: primeira missão e sistema de combate.")
        Spacer(Modifier.height(20.dp))
        OutlinedButton(onClick = onBack) {
            Text("Voltar para seleção")
        }
    }
}

@androidx.compose.runtime.Composable
private fun OnlinePlaceholder(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Modo Online")
        Spacer(Modifier.height(8.dp))
        Text("Salas e bots — próxima etapa")
        Spacer(Modifier.height(20.dp))
        OutlinedButton(onClick = onBack) {
            Text("Voltar")
        }
    }
}
