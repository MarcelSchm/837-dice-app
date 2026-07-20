package de.gyrosbande.dice.ui.players

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.gyrosbande.dice.domain.PlayerName

/**
 * Manage the Gyrosbande: add/remove players; the checkbox ("spielt mit")
 * decides who takes part in the next round.
 */
@Composable
fun PlayersScreen(viewModel: PlayersViewModel, onBack: () -> Unit) {
    val players by viewModel.players.collectAsState()
    var newName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text("Spieler", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Haken = spielt bei der nächsten Runde mit.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))

        // Names are always capitalized, and every player needs a distinct
        // name - we have two Marcels, they become "Marcel S" and "Marcel H".
        val trimmedName = PlayerName.normalize(newName)
        val isDuplicate = PlayerName.isTaken(newName, players.map { it.name })

        Row(verticalAlignment = Alignment.Top) {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it.replaceFirstChar(Char::uppercaseChar) },
                label = { Text("Name") },
                singleLine = true,
                isError = isDuplicate,
                supportingText = if (isDuplicate) {
                    { Text("„$trimmedName“ gibt es schon. Mach z. B. „$trimmedName S“ daraus.") }
                } else {
                    null
                },
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = {
                    viewModel.add(newName)
                    newName = ""
                },
                enabled = trimmedName.isNotEmpty() && !isDuplicate,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Spieler hinzufügen")
            }
        }
        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(players, key = { it.id }) { player ->
                Card {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = player.isActive,
                            onCheckedChange = { viewModel.setActive(player, it) },
                        )
                        Text(
                            player.name,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { viewModel.remove(player) }) {
                            Icon(Icons.Default.Delete, contentDescription = "${player.name} löschen")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Zurück")
        }
    }
}
