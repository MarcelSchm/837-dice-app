package de.gyrosbande.dice.ui.history

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.gyrosbande.dice.domain.OrderSummary
import de.gyrosbande.dice.domain.Player
import de.gyrosbande.dice.ui.OrderCard
import de.gyrosbande.dice.ui.roll.SubstitutePickerDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateTimeFormat = SimpleDateFormat("EEEE, d. MMMM yyyy 'um' HH:mm 'Uhr'", Locale.GERMANY)

/** One past round: who rolled what, plus the order that was placed. */
@Composable
fun HistoryDetailScreen(viewModel: HistoryViewModel, uuid: String?, onBack: () -> Unit) {
    val rounds by viewModel.rounds.collectAsState()
    val round = rounds.find { it.uuid == uuid }

    if (round == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var showAddPlayer by remember { mutableStateOf(false) }
    var addTarget by remember { mutableStateOf<Player?>(null) }
    var showDrinkPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Text("Runde im Detail", style = MaterialTheme.typography.headlineMedium)
        Text(
            dateTimeFormat.format(Date(round.startedAt)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))

        var confirmRemove by remember { mutableStateOf<Int?>(null) }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                round.results.forEachIndexed { index, result ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Row {
                                Text(
                                    result.playerName,
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    result.drink.priceFormatted,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            // Dice are unknown for results entered by hand.
                            val rollHint = if (result.drinkRolls.isEmpty()) {
                                ""
                            } else {
                                ", Wurf ${result.categoryRoll} + ${result.drinkRolls.joinToString("+")}"
                            }
                            val substitutedHint = if (result.substituted) ", von Hand" else ""
                            Text(
                                "${result.drinkName} (${result.categoryName}" +
                                    "$rollHint$substitutedHint)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        // The last result can't go - an empty round would be
                        // pointless; delete the whole round instead.
                        if (round.results.size > 1) {
                            IconButton(onClick = { confirmRemove = index }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "${result.playerName} aus der Runde nehmen",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                viewModel.prepareAdd(round)
                showAddPlayer = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Spieler nachtragen ➕")
        }
        Text(
            "Für alle, die erst später dazugestoßen sind.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))

        confirmRemove?.let { index ->
            val result = round.results[index]
            AlertDialog(
                onDismissRequest = { confirmRemove = null },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteResult(round.uuid, index)
                            confirmRemove = null
                        },
                    ) {
                        Text("Entfernen", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmRemove = null }) { Text("Abbrechen") }
                },
                title = { Text("${result.playerName} entfernen?") },
                text = {
                    Text(
                        "${result.drinkName} für ${result.playerName} wird aus dieser " +
                            "Runde gestrichen. Die Rundensumme passt sich an."
                    )
                },
            )
        }

        val drinks = round.results.map { it.drink }
        OrderCard(
            lines = OrderSummary.linesOfDrinks(drinks),
            totalCents = round.totalCents,
            extras = round.extras,
        )
        Spacer(Modifier.height(24.dp))

        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Zurück")
        }
        Spacer(Modifier.height(8.dp))
        var confirmDelete by remember { mutableStateOf(false) }
        OutlinedButton(
            onClick = { confirmDelete = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
            ),
        ) {
            Text("Runde löschen 🗑️")
        }

        if (confirmDelete) {
            AlertDialog(
                onDismissRequest = { confirmDelete = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            confirmDelete = false
                            viewModel.deleteRound(round.uuid)
                            onBack()
                        },
                    ) {
                        Text("Löschen", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmDelete = false }) { Text("Abbrechen") }
                },
                title = { Text("Runde löschen?") },
                text = {
                    Text(
                        "Das lässt sich nicht rückgängig machen. Beim Import einer " +
                            "älteren Export-Datei kann die Runde allerdings zurückkommen."
                    )
                },
            )
        }
    }

    // Step 1: who is being added?
    if (showAddPlayer) {
        val candidates = viewModel.addCandidates
        AlertDialog(
            onDismissRequest = { showAddPlayer = false },
            confirmButton = {
                TextButton(onClick = { showAddPlayer = false }) { Text("Abbrechen") }
            },
            title = { Text("Wer kam später dazu?") },
            text = {
                if (candidates.isEmpty()) {
                    Text("Alle bekannten Spieler sind schon in dieser Runde.")
                } else {
                    Column {
                        candidates.forEach { player ->
                            Text(
                                player.name,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        addTarget = player
                                        showAddPlayer = false
                                    }
                                    .padding(vertical = 12.dp),
                            )
                        }
                    }
                }
            },
        )
    }

    // Step 2: did they still have to roll, or do we know what they drank?
    addTarget?.let { player ->
        if (!showDrinkPicker) {
            AlertDialog(
                onDismissRequest = { addTarget = null },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.addRolledPlayer(round.uuid, player)
                            addTarget = null
                        },
                    ) {
                        Text("Jetzt würfeln 🎲")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDrinkPicker = true }) { Text("Getränk wählen 📋") }
                },
                title = { Text("${player.name} nachtragen") },
                text = {
                    Text(
                        "Muss ${player.name} noch würfeln? Dann würfelt die App jetzt. " +
                            "Steht schon fest, was es war, wähl das Getränk direkt aus."
                    )
                },
            )
        } else {
            SubstitutePickerDialog(
                categories = viewModel.menuCategories,
                onPick = { drink ->
                    viewModel.addPickedPlayer(round.uuid, player, drink)
                    showDrinkPicker = false
                    addTarget = null
                },
                onDismiss = {
                    showDrinkPicker = false
                    addTarget = null
                },
            )
        }
    }
}
