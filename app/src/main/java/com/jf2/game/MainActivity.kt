package com.jf2.game

import android.content.Intent
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
import androidx.compose.material3.TextButton
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
private enum class OnlineScreen { LOBBY, CREATE_ROOM, JOIN_ROOM }
private enum class BotDifficulty(val label: String, val level: Int) {
    NOVATO("Novato", 1),
    AMADOR("Amador", 2),
    MESTRE("Mestre", 3),
    VETERANO("Veterano", 4)
}

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
                    GameScreen.ONLINE -> OnlineLobby(onBack = { screen = GameScreen.MAIN_MENU })
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
    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
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
private fun OnlineLobby(onBack: () -> Unit) {
    var onlineScreen by remember { mutableStateOf(OnlineScreen.LOBBY) }
    var roomCode by remember { mutableStateOf("") }
    var joinedRoom by remember { mutableStateOf(false) }
    var botCount by remember { mutableStateOf(0) }
    var botDifficulty by remember { mutableStateOf(BotDifficulty.NOVATO) }
    var message by remember { mutableStateOf("Crie uma sala ou entre com um código.") }

    when (onlineScreen) {
        OnlineScreen.LOBBY -> Column(
            Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("MODO ONLINE", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(message)
            Spacer(Modifier.height(20.dp))
            Button(onClick = { onlineScreen = OnlineScreen.CREATE_ROOM }, Modifier.fillMaxWidth()) {
                Text("Criar sala")
            }
            Spacer(Modifier.height(10.dp))
            Button(onClick = { onlineScreen = OnlineScreen.JOIN_ROOM }, Modifier.fillMaxWidth()) {
                Text("Entrar em sala")
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = onBack, Modifier.fillMaxWidth()) { Text("Voltar") }
        }

        OnlineScreen.CREATE_ROOM -> CreateRoomScreen(
            roomCode = roomCode,
            botCount = botCount,
            difficulty = botDifficulty,
            onAddBot = { if (botCount < 7) botCount++ },
            onRemoveBot = { if (botCount > 0) botCount-- },
            onDifficulty = { botDifficulty = it },
            onCreate = {
                roomCode = generateRoomCode()
                joinedRoom = true
                message = "Sala criada: $roomCode"
            },
            onInvite = {
                val shareText = "Venha jogar JF2 comigo! Código da sala: $roomCode"
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                // This screen is UI-only until a real multiplayer server is connected.
            },
            joined = joinedRoom,
            onBack = { onlineScreen = OnlineScreen.LOBBY }
        )

        OnlineScreen.JOIN_ROOM -> JoinRoomScreen(
            onJoin = { code ->
                roomCode = code.uppercase().trim()
                joinedRoom = roomCode.length == 6
                message = if (joinedRoom) "Você entrou na sala $roomCode" else "O código precisa ter 6 caracteres."
            },
            onBack = { onlineScreen = OnlineScreen.LOBBY }
        )
    }
}

@Composable
private fun CreateRoomScreen(
    roomCode: String,
    botCount: Int,
    difficulty: BotDifficulty,
    onAddBot: () -> Unit,
    onRemoveBot: () -> Unit,
    onDifficulty: (BotDifficulty) -> Unit,
    onCreate: () -> Unit,
    onInvite: () -> Unit,
    joined: Boolean,
    onBack: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("CRIAR SALA", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        if (!joined) {
            Button(onClick = onCreate, Modifier.fillMaxWidth()) { Text("Criar sala") }
        } else {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("CÓDIGO DA SALA", style = MaterialTheme.typography.labelLarge)
                    Text(roomCode, style = MaterialTheme.typography.displaySmall)
                    Button(onClick = onInvite, Modifier.fillMaxWidth()) { Text("Convidar amigos") }
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Bots: $botCount / 7", style = MaterialTheme.typography.titleMedium)
        Row {
            OutlinedButton(onClick = onRemoveBot, enabled = botCount > 0) { Text("−") }
            Spacer(Modifier.padding(4.dp))
            Button(onClick = onAddBot, enabled = botCount < 7) { Text("+ Bot") }
        }
        Spacer(Modifier.height(14.dp))
        Text("Dificuldade dos bots: ${difficulty.label}")
        BotDifficulty.values().forEach { level ->
            TextButton(onClick = { onDifficulty(level) }) {
                Text(if (level == difficulty) "✓ ${level.label}" else level.label)
            }
        }
        Text("Novato < Amador < Mestre < Veterano", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onBack, Modifier.fillMaxWidth()) { Text("Voltar") }
    }
}

@Composable
private fun JoinRoomScreen(onJoin: (String) -> Unit, onBack: () -> Unit) {
    var code by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("ENTRAR EM SALA", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Text("Digite o código de 6 caracteres que seu amigo enviou.")
        Spacer(Modifier.height(16.dp))
        androidx.compose.material3.OutlinedTextField(
            value = code,
            onValueChange = { code = it.take(6).uppercase() },
            label = { Text("Código da sala") },
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = { onJoin(code) }, enabled = code.length == 6) { Text("Entrar") }
        OutlinedButton(onClick = onBack) { Text("Voltar") }
    }
}

private fun generateRoomCode(): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    return (1..6).map { chars.random() }.joinToString("")
}

@Composable
private fun MissionOne(hero: Hero?, onBack: () -> Unit) {
    var player by remember { mutableStateOf(Offset(500f, 500f)) }
    var aim by remember { mutableStateOf(Offset(1f, 0f)) }
    var enemies by remember { mutableStateOf(listOf(Enemy(1, Offset(260f, 300f), 3), Enemy(2, Offset(760f, 300f), 3), Enemy(3, Offset(300f, 700f), 3), Enemy(4, Offset(780f, 700f), 3))) }
    var health by remember { mutableStateOf(100) }
    var score by remember { mutableStateOf(0) }
    var fireCooldown by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }
    var won by remember { mutableStateOf(false) }

    LaunchedEffect(gameOver, won) {
        while (!gameOver && !won) {
            delay(80)
            val next = enemies.map { enemy ->
                val dx = player.x - enemy.position.x
                val dy = player.y - enemy.position.y
                val distance = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                enemy.copy(position = Offset(enemy.position.x + dx / distance * 2f, enemy.position.y + dy / distance * 2f))
            }
            enemies = next
            if (enemies.any { sqrt((it.position.x - player.x) * (it.position.x - player.x) + (it.position.y - player.y) * (it.position.y - player.y)) < 38f }) health = (health - 1).coerceAtLeast(0)
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
            drawLine(Color.White, player, Offset(player.x + aim.x * 55f, player.y + aim.y * 55f), strokeWidth = 10f)
        }
        Column(Modifier.align(Alignment.TopStart).padding(16.dp)) {
            Text("MISSÃO 1 — INVASÃO", color = Color.White, style = MaterialTheme.typography.titleLarge)
            Text("${hero?.name ?: "Herói"} • Vida: $health • Inimigos: ${enemies.size} • Abates: $score", color = Color.White)
        }
        Box(Modifier.align(Alignment.BottomStart).padding(24.dp).pointerInput(Unit) {
            detectDragGestures { _, dragAmount ->
                val length = sqrt(dragAmount.x * dragAmount.x + dragAmount.y * dragAmount.y).coerceAtLeast(1f)
                player = Offset((player.x + dragAmount.x / length * 12f).coerceIn(40f, 960f), (player.y + dragAmount.y / length * 12f).coerceIn(120f, 1600f))
            }
        }) { Card { Text("◉\nARRASTE\nPARA MOVER", Modifier.padding(18.dp)) } }
        Column(Modifier.align(Alignment.BottomEnd).padding(24.dp), horizontalAlignment = Alignment.End) {
            Button(onClick = { aim = Offset(0f, -1f) }) { Text("MIRAR ↑") }
            Row { Button(onClick = { aim = Offset(-1f, 0f) }) { Text("←") }; Button(onClick = { aim = Offset(1f, 0f) }) { Text("→") } }
            Button(onClick = { aim = Offset(0f, 1f) }) { Text("MIRAR ↓") }
            Button(enabled = !fireCooldown && !gameOver && !won, onClick = {
                fireCooldown = true
                enemies.minByOrNull { (it.position.x - player.x) * (it.position.x - player.x) + (it.position.y - player.y) * (it.position.y - player.y) }?.let { target ->
                    val dx = target.position.x - player.x
                    val dy = target.position.y - player.y
                    if (dx * aim.x + dy * aim.y > 0f && sqrt(dx * dx + dy * dy) < 650f) {
                        val updated = target.copy(health = target.health - 1)
                        enemies = if (updated.health <= 0) { score++; enemies.filterNot { it.id == target.id } } else enemies.map { if (it.id == target.id) updated else it }
                    }
                }
            }) { Text(if (fireCooldown) "ATIRANDO..." else "ATIRAR") }
        }
        if (gameOver || won) Card(Modifier.align(Alignment.Center).padding(24.dp)) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (won) "MISSÃO CONCLUÍDA!" else "VOCÊ FOI DERROTADO", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onBack) { Text("Voltar para seleção") }
            }
        }
    }
    LaunchedEffect(fireCooldown) { if (fireCooldown) { delay(350); fireCooldown = false } }
}
