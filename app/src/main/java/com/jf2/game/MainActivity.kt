package com.jf2.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.sqrt

private enum class GameScreen { MAIN_MENU, STORY_CHARACTER_SELECT, STORY, ONLINE }

private data class Hero(val name: String, val role: String, val description: String)
private data class Enemy(val id: Int, val position: Offset, val health: Int)

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
                        onBack = { selectedHero = null; screen = GameScreen.MAIN_MENU }
                    )
                    GameScreen.STORY -> MissionOne(hero = selectedHero, onBack = { screen = GameScreen.STORY_CHARACTER_SELECT })
                    GameScreen.ONLINE -> OnlinePlaceholder(onBack = { screen = GameScreen.MAIN_MENU })
                }
            }
        }
    }
}

@Composable
private fun MainMenu(onStory: () -> Unit, onOnline: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("JF2", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onStory, Modifier.fillMaxWidth()) { Text("Modo História") }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onOnline, Modifier.fillMaxWidth()) { Text("Modo Online") }
    }
}

@Composable
private fun CharacterSelectionScreen(
    selectedHero: Hero?, onSelect: (Hero) -> Unit, onStart: () -> Unit, onBack: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("SELEÇÃO DE PERSONAGEM", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("Escolha seu herói para começar a campanha.")
        Spacer(Modifier.height(12.dp))
        heroes.forEach { hero ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text(hero.name, style = MaterialTheme.typography.titleLarge)
                    Text(hero.role)
                    Text(hero.description)
                    Button(onClick = { onSelect(hero) }) {
                        Text(if (hero == selectedHero) "Selecionado" else "Selecionar")
                    }
                }
            }
        }
        Button(onClick = onStart, enabled = selectedHero != null, Modifier.fillMaxWidth()) {
            Text(if (selectedHero == null) "Escolha um personagem" else "Começar campanha")
        }
        OutlinedButton(onClick = onBack, Modifier.fillMaxWidth()) { Text("Voltar") }
    }
}

@Composable
private fun MissionOne(hero: Hero?, onBack: () -> Unit) {
    var player by remember { mutableStateOf(Offset(500f, 500f)) }
    var aim by remember { mutableStateOf(Offset(1f, 0f)) }
    var enemies by remember {
        mutableStateOf(
            listOf(
                Enemy(1, Offset(260f, 300f), 3),
                Enemy(2, Offset(760f, 300f), 3),
                Enemy(3, Offset(300f, 700f), 3),
                Enemy(4, Offset(780f, 700f), 3)
            )
        )
    }
    var health by remember { mutableStateOf(100) }
    var score by remember { mutableStateOf(0) }
    var fireCooldown by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }
    var won by remember { mutableStateOf(false) }

    LaunchedEffect(gameOver, won) {
        while (!gameOver && !won) {
            delay(80)
            val next = enemies.mapNotNull { enemy ->
                val dx = player.x - enemy.position.x
                val dy = player.y - enemy.position.y
                val distance = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                val step = 2.0f
                val nextPos = Offset(enemy.position.x + dx / distance * step, enemy.position.y + dy / distance * step)
                if (distance < 38f) {
                    health = (health - 1).coerceAtLeast(0)
                    enemy.copy(position = nextPos)
                } else enemy.copy(position = nextPos)
            }
            enemies = next
            if (health <= 0) gameOver = true
            if (enemies.isEmpty()) won = true
        }
    }

    Box(Modifier.fillMaxSize().background(Color(0xFF20252B))) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Color(0xFF303840))
            drawCircle(Color(0xFF4CAF50), 28f, player)
            enemies.forEach { enemy ->
                drawCircle(Color(0xFFE53935), 24f, enemy.position)
                drawCircle(Color.White, 4f, enemy.position)
            }
            val gunEnd = Offset(player.x + aim.x * 55f, player.y + aim.y * 55f)
            drawLine(Color.White, player, gunEnd, strokeWidth = 10f)
        }

        Column(Modifier.align(Alignment.TopStart).padding(16.dp)) {
            Text("MISSÃO 1 — INVASÃO", color = Color.White, style = MaterialTheme.typography.titleLarge)
            Text("${hero?.name ?: "Herói"}  •  Vida: $health  •  Inimigos: ${enemies.size}  •  Abates: $score", color = Color.White)
        }

        Box(
            Modifier.align(Alignment.BottomStart).padding(24.dp).pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    val length = sqrt(dragAmount.x * dragAmount.x + dragAmount.y * dragAmount.y).coerceAtLeast(1f)
                    val move = Offset(dragAmount.x / length * 12f, dragAmount.y / length * 12f)
                    player = Offset((player.x + move.x).coerceIn(40f, 960f), (player.y + move.y).coerceIn(120f, 1600f))
                }
            }
        ) {
            Card { Text("◉\nARRASTE\nPARA MOVER", Modifier.padding(18.dp)) }
        }

        Column(Modifier.align(Alignment.BottomEnd).padding(24.dp), horizontalAlignment = Alignment.End) {
            Button(onClick = {
                aim = Offset(0f, -1f)
            }) { Text("MIRAR ↑") }
            Row {
                Button(onClick = { aim = Offset(-1f, 0f) }) { Text("←") }
                Button(onClick = { aim = Offset(1f, 0f) }) { Text("→") }
            }
            Button(onClick = { aim = Offset(0f, 1f) }) { Text("MIRAR ↓") }
            Button(enabled = !fireCooldown && !gameOver && !won, onClick = {
                if (enemies.isNotEmpty()) {
                    fireCooldown = true
                    val target = enemies.minByOrNull { enemy ->
                        val dx = enemy.position.x - player.x
                        val dy = enemy.position.y - player.y
                        dx * dx + dy * dy
                    }
                    if (target != null) {
                        val dx = target.position.x - player.x
                        val dy = target.position.y - player.y
                        val dot = dx * aim.x + dy * aim.y
                        if (dot > 0f) {
                            val distance = sqrt(dx * dx + dy * dy)
                            if (distance < 650f) {
                                val updated = target.copy(health = target.health - 1)
                                enemies = if (updated.health <= 0) {
                                    score += 1
                                    enemies.filterNot { it.id == target.id }
                                } else enemies.map { if (it.id == target.id) updated else it }
                            }
                        }
                    }
                }
            }) { Text(if (fireCooldown) "ATIRANDO..." else "ATIRAR") }
        }

        if (gameOver || won) {
            Card(Modifier.align(Alignment.Center).padding(24.dp)) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (won) "MISSÃO CONCLUÍDA!" else "VOCÊ FOI DERROTADO", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text(if (won) "Todos os inimigos foram derrotados." else "Tente novamente com outro personagem.")
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onBack) { Text("Voltar para seleção") }
                }
            }
        }
    }

    LaunchedEffect(fireCooldown) {
        if (fireCooldown) {
            delay(350)
            fireCooldown = false
        }
    }
}

@Composable
private fun OnlinePlaceholder(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Modo Online")
        Text("Salas e bots — próxima etapa")
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onBack) { Text("Voltar") }
    }
}
