package de.gyrosbande.dice.ui.history

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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import de.gyrosbande.dice.ui.OrderCard
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

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                round.results.forEach { result ->
                    Column {
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
                        val substitutedHint = if (result.substituted) ", von Hand ersetzt" else ""
                        Text(
                            "${result.drinkName} (${result.categoryName}, " +
                                "Wurf ${result.categoryRoll} + ${result.drinkRolls.joinToString("+")}" +
                                "$substitutedHint)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        val drinks = round.results.map { it.drink }
        OrderCard(
            lines = OrderSummary.linesOfDrinks(drinks),
            totalCents = OrderSummary.totalCentsOfDrinks(drinks),
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
}
