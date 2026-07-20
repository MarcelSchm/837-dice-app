package de.gyrosbande.dice.ui.menu

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import de.gyrosbande.dice.data.EditableDrink
import de.gyrosbande.dice.domain.PriceInput

/**
 * Edit one category: rename it, reassign its pip number (swaps with the
 * category that held it) and manage its drinks. Drink order matters - it
 * is the roll order (wrap rule)!
 */
@Composable
fun CategoryEditScreen(viewModel: MenuViewModel, categoryId: Long?, onBack: () -> Unit) {
    val categories by viewModel.categories.collectAsState()
    val category = categories.find { it.id == categoryId }

    if (category == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var renameDialog by remember { mutableStateOf(false) }
    var drinkDialogFor by remember { mutableStateOf<EditableDrink?>(null) }
    var addDialog by remember { mutableStateOf(false) }
    var deleteDialogFor by remember { mutableStateOf<EditableDrink?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                category.name,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = { renameDialog = true }) { Text("Umbenennen") }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "Augenzahl beim Kategorie-Wurf:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (1..6).forEach { n ->
                FilterChip(
                    selected = category.diceNumber == n,
                    onClick = { viewModel.swapDiceNumber(category.id, n) },
                    label = { Text("$n") },
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Die Reihenfolge der Drinks ist die Würfelreihenfolge!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(category.drinks, key = { it.id }) { drink ->
                val index = category.drinks.indexOf(drink)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { drinkDialogFor = drink },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "${index + 1}",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 10.dp),
                        )
                        Column(Modifier.weight(1f)) {
                            Text(drink.name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                drink.priceFormatted +
                                    (drink.sizeLabel?.let { " · $it" } ?: ""),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(
                            onClick = { viewModel.moveDrink(drink.id, up = true) },
                            enabled = index > 0,
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Nach oben")
                        }
                        IconButton(
                            onClick = { viewModel.moveDrink(drink.id, up = false) },
                            enabled = index < category.drinks.size - 1,
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Nach unten")
                        }
                        IconButton(
                            onClick = { deleteDialogFor = drink },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "${drink.name} löschen")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Button(onClick = { addDialog = true }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.padding(4.dp))
            Text("Drink hinzufügen")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Zurück")
        }
    }

    if (renameDialog) {
        RenameDialog(
            current = category.name,
            onConfirm = { viewModel.renameCategory(category.id, it); renameDialog = false },
            onDismiss = { renameDialog = false },
        )
    }

    if (addDialog) {
        DrinkDialog(
            title = "Drink hinzufügen",
            initial = null,
            onConfirm = { name, cents, size ->
                viewModel.addDrink(category.id, name, cents, size)
                addDialog = false
            },
            onDismiss = { addDialog = false },
        )
    }

    drinkDialogFor?.let { drink ->
        DrinkDialog(
            title = "Drink bearbeiten",
            initial = drink,
            onConfirm = { name, cents, size ->
                viewModel.updateDrink(drink.id, name, cents, size)
                drinkDialogFor = null
            },
            onDismiss = { drinkDialogFor = null },
        )
    }

    deleteDialogFor?.let { drink ->
        AlertDialog(
            onDismissRequest = { deleteDialogFor = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteDrink(drink.id)
                        deleteDialogFor = null
                    },
                ) {
                    Text("Löschen", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialogFor = null }) { Text("Abbrechen") }
            },
            title = { Text("${drink.name} löschen?") },
            text = { Text("Der Drink verschwindet von der Karte. Alte Runden bleiben unberührt.") },
        )
    }
}

@Composable
private fun RenameDialog(current: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) {
                Text("Speichern")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } },
        title = { Text("Kategorie umbenennen") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
            )
        },
    )
}

@Composable
private fun DrinkDialog(
    title: String,
    initial: EditableDrink?,
    onConfirm: (name: String, priceCents: Int, sizeLabel: String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var price by remember { mutableStateOf(initial?.let { PriceInput.format(it.priceCents) } ?: "") }
    var size by remember { mutableStateOf(initial?.sizeLabel ?: "") }
    val parsedCents = PriceInput.parseCents(price)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, parsedCents ?: 0, size.takeIf { it.isNotBlank() }) },
                enabled = name.isNotBlank() && parsedCents != null,
            ) {
                Text("Speichern")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Preis in € (z. B. 2,50)") },
                    singleLine = true,
                    isError = price.isNotBlank() && parsedCents == null,
                )
                OutlinedTextField(
                    value = size,
                    onValueChange = { size = it },
                    label = { Text("Größe, optional (z. B. 2 cl)") },
                    singleLine = true,
                )
            }
        },
    )
}
