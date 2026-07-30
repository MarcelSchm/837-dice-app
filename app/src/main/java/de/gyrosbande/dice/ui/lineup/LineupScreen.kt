package de.gyrosbande.dice.ui.lineup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.gyrosbande.dice.domain.Player
import de.gyrosbande.dice.domain.PlayerName

/**
 * Sets up tonight's line-up before the round starts: who is at the table and
 * in which order. Pre-filled with the last round's seating, because the same
 * crowd usually sits down the same way - but a day at the festival is never
 * quite like the one before.
 */
@Composable
fun LineupScreen(
    viewModel: LineupViewModel,
    onStart: (List<Long>) -> Unit,
    onBack: () -> Unit,
) {
    if (viewModel.loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val lineup = viewModel.lineup
    val bench = viewModel.bench
    var newName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text("Aufstellung", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Wer sitzt heute mit am Tisch – und in welcher Reihenfolge?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "header-table") {
                SectionTitle("Am Tisch (${lineup.size})")
            }

            if (lineup.isEmpty()) {
                item(key = "empty-table") {
                    Text(
                        "Noch niemand am Tisch. Tippt euch unten dazu.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            itemsIndexed(lineup) { index, player ->
                SeatRow(
                    position = index + 1,
                    player = player,
                    isFirst = index == 0,
                    isLast = index == lineup.lastIndex,
                    onUp = { viewModel.move(index, up = true) },
                    onDown = { viewModel.move(index, up = false) },
                    onRemove = { viewModel.removeFromLineup(player) },
                )
            }

            if (bench.isNotEmpty()) {
                item(key = "header-bench") {
                    Spacer(Modifier.height(8.dp))
                    SectionTitle("Heute nicht dabei")
                }
                items(bench, key = { "bench-${it.id}" }) { player ->
                    BenchRow(player = player, onAdd = { viewModel.addToLineup(player) })
                }
            }

            item(key = "new-player") {
                Spacer(Modifier.height(8.dp))
                SectionTitle("Jemand Neues?")
                NewPlayerRow(
                    name = newName,
                    existingNames = viewModel.allPlayers.map { it.name },
                    onNameChange = { newName = it },
                    onAdd = {
                        viewModel.addNewPlayer(newName)
                        newName = ""
                    },
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { onStart(lineup.map { it.id }) },
            enabled = lineup.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Text("Los geht's 🎲", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Zurück")
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun SeatRow(
    position: Int,
    player: Player,
    isFirst: Boolean,
    isLast: Boolean,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "$position.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                player.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
            )
            IconButton(onClick = onUp, enabled = !isFirst) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "${player.name} nach vorne")
            }
            IconButton(onClick = onDown, enabled = !isLast) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "${player.name} nach hinten")
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "${player.name} sitzt heute nicht mit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun BenchRow(player: Player, onAdd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                player.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "${player.name} dazusetzen")
            }
        }
    }
}

@Composable
private fun NewPlayerRow(
    name: String,
    existingNames: List<String>,
    onNameChange: (String) -> Unit,
    onAdd: () -> Unit,
) {
    // Same rules as the players screen: capitalized, and no two people with
    // the same name - otherwise the order summary is ambiguous.
    val trimmed = PlayerName.normalize(name)
    val isDuplicate = PlayerName.isTaken(name, existingNames)

    Row(verticalAlignment = Alignment.Top) {
        OutlinedTextField(
            value = name,
            onValueChange = { onNameChange(it.replaceFirstChar(Char::uppercaseChar)) },
            label = { Text("Name") },
            singleLine = true,
            isError = isDuplicate,
            supportingText = if (isDuplicate) {
                { Text("„$trimmed“ gibt es schon. Mach z. B. „$trimmed S“ daraus.") }
            } else {
                null
            },
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = onAdd,
            enabled = trimmed.isNotEmpty() && !isDuplicate,
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = "Neuen Spieler dazusetzen")
        }
    }
}
