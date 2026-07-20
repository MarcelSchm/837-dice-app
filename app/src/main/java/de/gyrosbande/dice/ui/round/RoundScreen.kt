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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.gyrosbande.dice.domain.ExtraItem
import de.gyrosbande.dice.domain.OrderSummary
import de.gyrosbande.dice.domain.PriceInput
import de.gyrosbande.dice.ui.OrderCard
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
                "Leg zuerst die Gyrosbande an. Wer einen Haken hat, spielt mit.",
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
            SummaryContent(viewModel, onDone)
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
                        if (isLast) "Zur Bestellung 🧾" else "Nächster Spieler",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryContent(viewModel: RoundViewModel, onDone: () -> Unit) {
    val results = viewModel.results
    var addExtraDialog by remember { mutableStateOf(false) }

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

    // Grouped order with total (drinks plus manual extras)
    val outcomes = results.map { it.outcome }
    val extraItems = viewModel.extras.map { it.second }
    OrderCard(
        lines = OrderSummary.lines(outcomes),
        totalCents = OrderSummary.totalCents(outcomes) + extraItems.sumOf { it.totalCents },
        extras = extraItems,
        onExtraClick = viewModel::removeExtra,
    )
    if (extraItems.isNotEmpty()) {
        Spacer(Modifier.height(4.dp))
        Text(
            "Extras antippen zum Entfernen.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Spacer(Modifier.height(12.dp))
    OutlinedButton(onClick = { addExtraDialog = true }, modifier = Modifier.fillMaxWidth()) {
        Text("Essen oder Getränk dazu 🥙")
    }
    Spacer(Modifier.height(12.dp))

    if (addExtraDialog) {
        AddExtraDialog(
            onConfirm = { extra ->
                viewModel.addExtra(extra)
                addExtraDialog = false
            },
            onDismiss = { addExtraDialog = false },
        )
    }
    Button(
        onClick = onDone,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
    ) {
        Text("Runde abschließen ✅", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun AddExtraDialog(onConfirm: (ExtraItem) -> Unit, onDismiss: () -> Unit) {
    var label by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf(1) }
    val parsedCents = PriceInput.parseCents(price)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(ExtraItem(label.trim(), parsedCents ?: 0, quantity))
                },
                enabled = label.isNotBlank() && parsedCents != null,
            ) {
                Text("Hinzufügen")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } },
        title = { Text("Essen oder Getränk dazu") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Was? (z. B. Menü 837)") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Einzelpreis in € (z. B. 8,50)") },
                    singleLine = true,
                    isError = price.isNotBlank() && parsedCents == null,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Anzahl:", modifier = Modifier.weight(1f))
                    OutlinedButton(onClick = { if (quantity > 1) quantity-- }) { Text("−") }
                    Text(
                        "$quantity",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    OutlinedButton(onClick = { if (quantity < 20) quantity++ }) { Text("+") }
                }
            }
        },
    )
}
