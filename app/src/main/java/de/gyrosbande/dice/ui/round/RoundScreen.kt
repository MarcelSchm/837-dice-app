package de.gyrosbande.dice.ui.round

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.CardDefaults
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
import de.gyrosbande.dice.domain.Drink
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
        val redoIndex = viewModel.redoIndex
        if (redoIndex != null) {
            RedoContent(viewModel, redoIndex)
        } else if (viewModel.isFinished) {
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

/**
 * Re-rolling for everyone who had an unavailable drink, one player after
 * another. Rolling the same sold-out drink again is refused instead of
 * being accepted.
 */
@Composable
private fun ColumnScope.RedoContent(
    viewModel: RoundViewModel,
    index: Int,
) {
    val controller = viewModel.controller ?: return
    val result = viewModel.results.getOrNull(index) ?: return

    Text(
        "🎲 ${result.player.name} würfelt neu",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
    )
    Text(
        if (viewModel.redoTotal > 1) {
            "${viewModel.redoDone} von ${viewModel.redoTotal}. Die Kategorie bleibt, " +
                "nur der Drink wird neu gewürfelt."
        } else {
            "Die Kategorie bleibt, nur der Drink wird neu gewürfelt."
        },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(16.dp))
    RollPanel(
        controller = controller,
        onRollVirtual = viewModel::rollVirtual,
    ) { finished ->
        if (viewModel.isUnavailable(finished.outcome.drink)) {
            // Bad luck - the same sold-out drink came up again.
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Das gibt's immer noch nicht 🙄",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    Text(
                        "„${finished.outcome.drink.name}“ hat San Remo gerade nicht. " +
                            "Nochmal würfeln, oder oben von Hand einen Ersatz wählen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        } else {
            Button(
                onClick = viewModel::confirmRedo,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Text("Übernehmen ✅", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    OutlinedButton(onClick = viewModel::cancelRedo, modifier = Modifier.fillMaxWidth()) {
        Text("Abbrechen")
    }
}

@Composable
private fun SummaryContent(viewModel: RoundViewModel, onDone: () -> Unit) {
    val results = viewModel.results
    var addExtraDialog by remember { mutableStateOf(false) }
    var unavailableDrink by remember { mutableStateOf<Drink?>(null) }

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
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(result.player.name, modifier = Modifier.weight(1f))
                    Text(
                        result.outcome.drink.name +
                            if (result.outcome.substituted) " ✎" else "",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(16.dp))

    // Grouped order with total (drinks plus manual extras). Tapping a drink
    // reports "they don't have that" - it affects everyone who rolled it,
    // not just one player.
    val outcomes = results.map { it.outcome }
    val extraItems = viewModel.extras.map { it.second }
    OrderCard(
        lines = OrderSummary.lines(outcomes),
        totalCents = OrderSummary.totalCents(outcomes) + extraItems.sumOf { it.totalCents },
        extras = extraItems,
        onExtraClick = viewModel::removeExtra,
        onDrinkClick = { unavailableDrink = it },
    )
    Spacer(Modifier.height(4.dp))
    Text(
        if (extraItems.isEmpty()) {
            "Haben sie einen Drink nicht? Zeile antippen."
        } else {
            "Haben sie einen Drink nicht? Zeile antippen. Extras antippen entfernt sie."
        },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
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

    // "They don't have that" - names everyone affected before re-rolling
    unavailableDrink?.let { drink ->
        val affected = viewModel.playersWith(drink)
        AlertDialog(
            onDismissRequest = { unavailableDrink = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        unavailableDrink = null
                        viewModel.markDrinkUnavailable(drink)
                    },
                ) {
                    Text(if (affected.size > 1) "Alle neu würfeln 🎲" else "Neu würfeln 🎲")
                }
            },
            dismissButton = {
                TextButton(onClick = { unavailableDrink = null }) { Text("Passt doch") }
            },
            title = { Text("„${drink.name}“ gibt's nicht?") },
            text = {
                Text(
                    if (affected.size > 1) {
                        "${affected.dropLast(1).joinToString(", ")} und ${affected.last()} " +
                            "würfeln nacheinander neu, jeweils in ihrer eigenen Kategorie."
                    } else {
                        "${affected.firstOrNull() ?: "Niemand"} würfelt in derselben " +
                            "Kategorie neu."
                    }
                )
            },
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
