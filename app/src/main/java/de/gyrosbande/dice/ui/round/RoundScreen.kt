package de.gyrosbande.dice.ui.round

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.gyrosbande.dice.domain.OrderSummary
import de.gyrosbande.dice.domain.PlayerOutcome
import de.gyrosbande.dice.ui.roll.RollPanel

/**
 * The round flow: active players roll one after another; at the end the
 * grouped order (with total) is shown for ordering at the counter.
 */
@Composable
fun RoundScreen(viewModel: RoundViewModel, onGoToPlayers: () -> Unit, onDone: () -> Unit) {
    val controller = viewModel.controller

    if (viewModel.loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (controller == null) {
        // No active players yet
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                "Keine aktiven Spieler 🤷",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Leg zuerst die Gyrosbande an – wer einen Haken hat, spielt mit.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onGoToPlayers, modifier = Modifier.fillMaxWidth()) {
                Text("Spieler verwalten")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                Text("Zurück")
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (viewModel.isFinished) {
            SummaryContent(viewModel.results, onDone)
        } else {
            val player = viewModel.currentPlayer
            if (player != null) {
                Text(
                    "🎲 ${player.name} ist dran",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    "Spieler ${viewModel.results.size + 1} von ${viewModel.players.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
            }
            RollPanel(
                controller = controller,
                onRollVirtual = viewModel::rollVirtual,
            ) {
                val isLast = viewModel.results.size == viewModel.players.size - 1
                Button(
                    onClick = viewModel::confirmResult,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                ) {
                    Text(
                        if (isLast) "Zur Bestellung 🧾" else "Weiter – nächster Spieler",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryContent(results: List<PlayerOutcome>, onDone: () -> Unit) {
    Text("Die Bestellung 🧾", style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(4.dp))
    Text(
        "Einmal vorlesen beim San Remo:",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(16.dp))

    // Who rolled what
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            results.forEach { result ->
                Row {
                    Text(result.player.name, modifier = Modifier.weight(1f))
                    Text(
                        result.outcome.drink.name,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(16.dp))

    // Grouped order with total
    val outcomes = results.map { it.outcome }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            OrderSummary.lines(outcomes).forEach { line ->
                Row {
                    Text(
                        "${line.quantity}×",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(end = 12.dp),
                    )
                    Text(
                        line.drink.name + (line.drink.sizeLabel?.let { " ($it)" } ?: ""),
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    Text(line.totalFormatted, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimary)
            Row {
                Text(
                    "Gesamt",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    OrderSummary.totalFormatted(outcomes),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
    Spacer(Modifier.height(24.dp))
    Button(
        onClick = onDone,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
    ) {
        Text("Runde abschließen ✅", style = MaterialTheme.typography.titleMedium)
    }
}
