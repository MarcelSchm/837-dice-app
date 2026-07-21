package de.gyrosbande.dice.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.gyrosbande.dice.R
import de.gyrosbande.dice.domain.FestivalStatus
import de.gyrosbande.dice.ui.home.HomeViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onStartRound: () -> Unit,
    onQuickRoll: () -> Unit,
    onPlayers: () -> Unit,
    onHistory: () -> Unit,
    onMenu: () -> Unit,
) {
    val status by viewModel.festivalStatus.collectAsState()
    val storedStart by viewModel.festivalStartEpochDay.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.logo_gyrosbande),
            contentDescription = "837 Gyrosbande Logo",
            modifier = Modifier.size(180.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text("837 Dice", style = MaterialTheme.typography.displaySmall)
        Text(
            "Das Schnapswürfeln der Gyrosbande",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(20.dp))
        FestivalBanner(status = status, onClick = { showDatePicker = true })

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onStartRound,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
        ) {
            Text("Neue Runde 🎲", style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onQuickRoll,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text("Schnell würfeln", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onPlayers,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
            ) {
                Text("Spieler", style = MaterialTheme.typography.titleSmall)
            }
            OutlinedButton(
                onClick = onHistory,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
            ) {
                Text("Historie", style = MaterialTheme.typography.titleSmall)
            }
            OutlinedButton(
                onClick = onMenu,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
            ) {
                Text("Karte", style = MaterialTheme.typography.titleSmall)
            }
        }
    }

    if (showDatePicker) {
        FestivalDatePicker(
            initialEpochDay = storedStart,
            onPick = { epochDay ->
                viewModel.setFestivalStart(epochDay)
                showDatePicker = false
            },
            onClear = {
                viewModel.clearFestivalStart()
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }
}

/** The Open Flair countdown - or an invitation to set the date. */
@Composable
private fun FestivalBanner(status: FestivalStatus?, onClick: () -> Unit) {
    val (line, sub) = when (status) {
        null -> "Open Flair eintragen 🎪" to "Tippen und den nächsten Festival-Termin setzen."
        is FestivalStatus.Upcoming -> {
            val label = if (status.days == 1) "Noch 1 Tag" else "Noch ${status.days} Tage"
            "$label bis Open Flair 🎪" to "Die Gyrosbande zählt runter."
        }
        is FestivalStatus.Running ->
            "Open Flair läuft! 🍻" to "Tag ${status.day} von ${status.totalDays}. Prost!"
        FestivalStatus.Past ->
            "Open Flair ist vorbei 😢" to "Tippen und den nächsten Termin setzen."
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (status is FestivalStatus.Running) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val onColor = if (status is FestivalStatus.Running) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
            Text(line, style = MaterialTheme.typography.titleMedium, color = onColor)
            Text(
                sub,
                style = MaterialTheme.typography.bodySmall,
                color = onColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FestivalDatePicker(
    initialEpochDay: Long?,
    onPick: (Long) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    val initialMillis = (initialEpochDay ?: LocalDate.now().toEpochDay()) * MILLIS_PER_DAY
    val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let { millis ->
                        // The picker works in UTC midnight; go straight to epoch day.
                        onPick(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay())
                    }
                },
                enabled = state.selectedDateMillis != null,
            ) {
                Text("Setzen")
            }
        },
        dismissButton = {
            Row {
                if (initialEpochDay != null) {
                    TextButton(onClick = onClear) { Text("Entfernen") }
                }
                TextButton(onClick = onDismiss) { Text("Abbrechen") }
            }
        },
    ) {
        DatePicker(state = state, title = {
            Text(
                "Erster Festival-Tag",
                Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
            )
        })
    }
}

private const val MILLIS_PER_DAY = 86_400_000L
