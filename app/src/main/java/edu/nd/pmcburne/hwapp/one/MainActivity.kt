package edu.nd.pmcburne.hwapp.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.nd.pmcburne.hwapp.one.ui.theme.HWStarterRepoTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HWStarterRepoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GameScreen()
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(vm: GameViewModel = viewModel()) {
    val selectedDate by vm.selectedDate.collectAsStateWithLifecycle()
    val isMen by vm.isMen.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val games by vm.games.collectAsStateWithLifecycle()
    val errorMessage by vm.errorMessage.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    val displayFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "College Basketball", fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = { showDatePicker = true }) {
                Text(text = "Date:  ${selectedDate.format(displayFormatter)}")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Men's",
                    fontWeight = if (isMen) FontWeight.Bold else FontWeight.Normal,
                    color = if (isMen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(checked = !isMen, onCheckedChange = { vm.toggleGender() })
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Women's",
                    fontWeight = if (!isMen) FontWeight.Bold else FontWeight.Normal,
                    color = if (!isMen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { vm.refresh() }) { Text("Refresh") }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Spacer(modifier = Modifier.height(4.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }

            if (!isLoading && games.isEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(text = "No games found for this date.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(games, key = { it.id }) { game -> GameCard(game = game, isMen = isMen) }
                }
            }

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = selectedDate
                        .atStartOfDay(ZoneId.of("UTC"))
                        .toInstant()
                        .toEpochMilli()
                )
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val picked = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.of("UTC"))
                                    .toLocalDate()
                                vm.setDate(picked)
                            }
                            showDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
        }
    }
}

@Composable
fun GameCard(game: GameEntity, isMen: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Status(game = game)
                Text(
                    text = statusInfo(game, isMen),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            TeamRow(
                label = "AWAY",
                name = game.awayTeam,
                score = game.awayScore,
                isWinner = game.awayWinner,
                showScore = game.gameState != "pre"
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            TeamRow(
                label = "HOME",
                name = game.homeTeam,
                score = game.homeScore,
                isWinner = game.homeWinner,
                showScore = game.gameState != "pre"
            )
        }
    }
}

@Composable
fun TeamRow(label: String, name: String, score: String, isWinner: Boolean, showScore: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(40.dp)
            )
            Text(
                text = name,
                fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
                fontSize = 16.sp
            )
            if (isWinner) {
                Text(
                    text = "    WIN",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
            }
        }
        if (showScore) {
            Text(
                text = score,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.End
            )
        }
    }
}
@Composable
fun Status(game: GameEntity) {
    val (text, color) = when (game.gameState) {
        "pre" -> "Upcoming" to MaterialTheme.colorScheme.tertiary
        "live" -> "Live" to MaterialTheme.colorScheme.error
        "final" -> "Final" to MaterialTheme.colorScheme.primary
        else -> game.gameState to MaterialTheme.colorScheme.onSurface
    }
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .background(color, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}
fun statusInfo(game: GameEntity, isMen: Boolean): String {
    return when (game.gameState) {
        "pre" -> game.startTime
        "live" -> {
            val periodLabel = period(game.currentPeriod, isMen)
            "$periodLabel — ${game.contestClock}"
        }
        "final" -> {
            val message = game.finalMessage.ifBlank { "Final" }
            val winner = when {
                game.homeWinner -> game.homeTeam
                game.awayWinner -> game.awayTeam
                else -> null
            }
            if (winner != null) "$message — $winner wins" else message
        }
        else -> ""
    }
}

fun period(rawPeriod: String, isMen: Boolean): String {
    val period = rawPeriod.trim()
    if (period.isBlank()) {
        return ""
    }
    val lowercase = period.lowercase()
    if ("half" in lowercase || "qtr" in lowercase || "quarter" in lowercase || "ot" in lowercase || "final" in lowercase) {
        return period
    }
    return try {
        val num = period.replace(Regex("[^0-9]"), "").toInt()
        if (isMen) {
            when (num) {
                1 -> "1st Half"
                2 -> "2nd Half"
                else -> "OT${num - 2}"
            }
        } else {
            when (num) {
                1 -> "1st Qtr"
                2 -> "2nd Qtr"
                3 -> "3rd Qtr"
                4 -> "4th Qtr"
                else -> "OT${num - 4}"
            }
        }
    } catch (_: Exception) {
        period
    }
}