package de.gyrosbande.dice.ui.history

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.gyrosbande.dice.domain.HistoryRound
import de.gyrosbande.dice.domain.OrderSummary
import de.gyrosbande.dice.domain.StatsCalculator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dayFormat = SimpleDateFormat("EEEE, d. MMMM yyyy", Locale.GERMANY)
private val timeFormat = SimpleDateFormat("HH:mm", Locale.GERMANY)

/**
 * History with two tabs: the round list (with export/import) and the hall
 * of fame. The year filter applies to both.
 */
@Composable
fun HistoryScreen(viewModel: HistoryViewModel, onOpenRound: (String) -> Unit, onBack: () -> Unit) {
    val allRounds by viewModel.rounds.collectAsState()
    val rounds = viewModel.filtered(allRounds)
    var tab by remember { mutableIntStateOf(0) }
    var roundToDelete by remember { mutableStateOf<HistoryRound?>(null) }
    val context = LocalContext.current

    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let(viewModel::exportTo) }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(viewModel::importFrom) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text("Historie", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))

        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Runden") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Hall of Fame") })
        }
        Spacer(Modifier.height(12.dp))

        // Year filter (only shown once there is more than one year)
        val years = allRounds.map { it.year }.distinct().sortedDescending()
        if (years.size > 1) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = viewModel.selectedYear == null,
                    onClick = { viewModel.selectedYear = null },
                    label = { Text("Alle") },
                )
                years.forEach { year ->
                    FilterChip(
                        selected = viewModel.selectedYear == year,
                        onClick = { viewModel.selectedYear = year },
                        label = { Text("$year") },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        Column(Modifier.weight(1f)) {
            if (tab == 0) {
                RoundsTab(
                    rounds = rounds,
                    onOpenRound = onOpenRound,
                    onShare = {
                        viewModel.exportForSharing { uri ->
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/json"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(
                                Intent.createChooser(intent, "Historie teilen")
                            )
                        }
                    },
                    onSave = { saveLauncher.launch(viewModel.exportFileName()) },
                    onImport = { importLauncher.launch(arrayOf("*/*")) },
                    onDeleteRequest = { roundToDelete = it },
                )
            } else {
                StatsTab(
                    rounds = rounds,
                    onShareImage = {
                        val subtitle = viewModel.selectedYear?.let { "Open Flair $it" }
                            ?: "Alle Festivals"
                        viewModel.shareStatsImage(rounds, subtitle) { uri ->
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/png"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(
                                Intent.createChooser(intent, "Hall of Fame teilen")
                            )
                        }
                    },
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Zurück")
        }
    }

    roundToDelete?.let { round ->
        AlertDialog(
            onDismissRequest = { roundToDelete = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRound(round.uuid)
                        roundToDelete = null
                    },
                ) {
                    Text("Löschen", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { roundToDelete = null }) { Text("Abbrechen") }
            },
            title = { Text("Runde löschen?") },
            text = {
                Text(
                    "${timeFormat.format(Date(round.startedAt))} Uhr, " +
                        "${round.results.map { it.playerName }.distinct().joinToString(", ")}. " +
                        "Das lässt sich nicht rückgängig machen. Beim Import einer " +
                        "älteren Export-Datei kann die Runde allerdings zurückkommen."
                )
            },
        )
    }

    viewModel.importReport?.let { report ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDialogs,
            confirmButton = {
                TextButton(onClick = viewModel::dismissDialogs) { Text("OK") }
            },
            title = { Text("Import fertig ✅") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${report.importedRounds} Runden importiert")
                    Text("${report.skippedRounds} übersprungen (schon vorhanden)")
                    if (report.newPlayers.isNotEmpty()) {
                        Text("Neue Spieler: ${report.newPlayers.joinToString(", ")}")
                    }
                }
            },
        )
    }
    viewModel.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDialogs,
            confirmButton = {
                TextButton(onClick = viewModel::dismissDialogs) { Text("OK") }
            },
            title = { Text("Das hat nicht geklappt 😕") },
            text = { Text(message) },
        )
    }
}

@Composable
private fun RoundsTab(
    rounds: List<HistoryRound>,
    onOpenRound: (String) -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit,
    onImport: () -> Unit,
    onDeleteRequest: (HistoryRound) -> Unit,
) {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onShare, modifier = Modifier.weight(1f)) {
                Text("Teilen 📤")
            }
            OutlinedButton(onClick = onSave, modifier = Modifier.weight(1f)) {
                Text("Sichern 💾")
            }
            OutlinedButton(onClick = onImport, modifier = Modifier.weight(1f)) {
                Text("Import 📥")
            }
        }
        Spacer(Modifier.height(12.dp))

        if (rounds.isEmpty()) {
            Text(
                "Noch keine Runden gespeichert. Spielt eine Runde, dann füllt sich hier alles.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
            )
            return
        }

        val byDay = rounds.groupBy { dayFormat.format(Date(it.startedAt)) }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            byDay.forEach { (day, dayRounds) ->
                item(key = "header-$day") {
                    Text(
                        day,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                items(dayRounds, key = { it.uuid }) { round ->
                    RoundListItem(
                        round = round,
                        onClick = { onOpenRound(round.uuid) },
                        onDelete = { onDeleteRequest(round) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RoundListItem(round: HistoryRound, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "${timeFormat.format(Date(round.startedAt))} Uhr, ${round.results.size} Würfe",
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    round.results.map { it.playerName }.distinct().joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                OrderSummary.formatCents(round.totalCents),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Runde löschen",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StatsTab(rounds: List<HistoryRound>, onShareImage: () -> Unit) {
    if (rounds.isEmpty()) {
        Text(
            "Noch keine Daten. Die Hall of Fame füllt sich mit jeder Runde.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
        )
        return
    }
    Column {
        OutlinedButton(onClick = onShareImage, modifier = Modifier.fillMaxWidth()) {
            Text("Als Bild teilen 📤")
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn {
            item { StatsContent(StatsCalculator.calculate(rounds)) }
        }
    }
}
