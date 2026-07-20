package de.gyrosbande.dice.ui.roll

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import de.gyrosbande.dice.domain.Category
import de.gyrosbande.dice.domain.Drink
import de.gyrosbande.dice.domain.RollPhase
import de.gyrosbande.dice.ui.DiceFace

/**
 * The shared roll UI: phase heading, dice, mode switch, roll input and the
 * numbered drink list. What happens after a finished roll is up to the
 * caller via [resultActions] (quick roll: roll again; round: next player).
 * UI copy is German on purpose (target audience: the Gyrosbande).
 */
@Composable
fun ColumnScope.RollPanel(
    controller: RollController,
    onRollVirtual: () -> Unit,
    resultActions: @Composable ColumnScope.(RollPhase.Finished) -> Unit,
) {
    val state = controller.state
    val phase = state.phase
    val feedback = rememberDiceFeedback()
    val rollWithFeedback = {
        feedback.playRoll()
        onRollVirtual()
    }

    // Shaking the phone rolls too - like a dice cup.
    ShakeToRoll(
        enabled = state.mode == RollMode.VIRTUAL && !state.isRolling && phase !is RollPhase.Finished,
        onShake = rollWithFeedback,
    )

    // Phase heading
    val (title, subtitle) = when (phase) {
        is RollPhase.CategoryRoll -> "Wurf 1: Kategorie" to "Ein Würfel entscheidet, welche Kategorie dran ist."
        is RollPhase.DrinkRoll -> "Wurf 2: ${phase.category.name}" to
            if (phase.diceCount == 2) "${phase.category.drinks.size} Drinks, also zwei Würfel. Die Summe zählt!"
            else "${phase.category.drinks.size} Drinks, ein Würfel reicht."
        is RollPhase.Finished -> "Das wird bestellt! 🍻" to null
    }
    Text(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
    subtitle?.let {
        Spacer(Modifier.height(4.dp))
        Text(
            it,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
    Spacer(Modifier.height(24.dp))

    // Dice display
    val diceCount = when (phase) {
        is RollPhase.CategoryRoll -> 1
        is RollPhase.DrinkRoll -> phase.diceCount
        is RollPhase.Finished -> phase.outcome.drinkRolls.size
    }
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(diceCount) { i ->
            DiceFace(value = state.shownDice.getOrNull(i))
        }
    }
    Spacer(Modifier.height(24.dp))

    when (phase) {
        is RollPhase.Finished -> {
            ResultCard(phase)
            Spacer(Modifier.height(8.dp))
            NotAvailableSection(controller)
            Spacer(Modifier.height(8.dp))
            resultActions(phase)
        }
        else -> {
            // Mode switch plus sound toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(1f)) {
                    RollMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = state.mode == mode,
                            onClick = { controller.setMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index, RollMode.entries.size),
                        ) {
                            Text(if (mode == RollMode.VIRTUAL) "App würfelt" else "Echte Würfel")
                        }
                    }
                }
                TextButton(onClick = feedback::toggleSound) {
                    Text(if (feedback.soundEnabled) "🔊" else "🔇")
                }
            }
            Spacer(Modifier.height(16.dp))

            if (state.mode == RollMode.VIRTUAL) {
                Button(
                    onClick = rollWithFeedback,
                    enabled = !state.isRolling,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                ) {
                    Text(
                        if (state.isRolling) "Würfelt…" else "Würfeln! 🎲",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Oder das Handy schütteln wie einen Würfelbecher.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                val needed = diceCount - state.pendingManual.size
                Text(
                    if (diceCount == 2) "Würfel ${state.pendingManual.size + 1} von 2 eintippen"
                    else "Gewürfelte Augenzahl eintippen",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..6).forEach { n ->
                        OutlinedButton(
                            onClick = { controller.enterManualDie(n) },
                            enabled = needed > 0,
                            modifier = Modifier.size(48.dp),
                            contentPadding = PaddingValues(0.dp),
                        ) {
                            Text("$n", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            // During the drink roll, show the numbered list - so you can
            // see what you're hoping for (or what's looming).
            if (phase is RollPhase.DrinkRoll) {
                Spacer(Modifier.height(24.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        phase.category.drinks.forEachIndexed { i, drink ->
                            Row {
                                Text(
                                    "${i + 1}",
                                    modifier = Modifier.padding(end = 12.dp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(drink.name, modifier = Modifier.weight(1f))
                                Text(drink.priceFormatted, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * San Remo is out of the rolled drink: offer the house rule (reroll the
 * drink within the same category) or picking a replacement by hand.
 */
@Composable
private fun ColumnScope.NotAvailableSection(controller: RollController) {
    var expanded by remember { mutableStateOf(false) }
    var showPicker by remember { mutableStateOf(false) }

    TextButton(onClick = { expanded = !expanded }) {
        Text("Getränk nicht da? 🙃")
    }
    if (expanded) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = {
                    expanded = false
                    controller.rerollDrink()
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("Neu würfeln 🎲")
            }
            OutlinedButton(
                onClick = { showPicker = true },
                modifier = Modifier.weight(1f),
            ) {
                Text("Selbst wählen 📋")
            }
        }
        Text(
            "Neu gewürfelt wird in derselben Kategorie.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    if (showPicker) {
        SubstitutePickerDialog(
            categories = controller.categories,
            onPick = { drink ->
                controller.substitute(drink)
                showPicker = false
                expanded = false
            },
            onDismiss = { showPicker = false },
        )
    }
}

/** Menu picker for a replacement drink; also used by the round summary. */
@Composable
internal fun SubstitutePickerDialog(
    categories: List<Category>,
    onPick: (Drink) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        },
        title = { Text("Ersatz wählen") },
        text = {
            LazyColumn(modifier = Modifier.height(420.dp)) {
                categories.forEach { category ->
                    item(key = "header-${category.diceNumber}") {
                        Text(
                            category.name,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                        )
                    }
                    items(category.drinks, key = { "${category.diceNumber}-${it.name}-${it.sizeLabel}" }) { drink ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPick(drink) }
                                .padding(vertical = 10.dp),
                        ) {
                            Text(drink.name, modifier = Modifier.weight(1f))
                            Text(
                                drink.priceFormatted,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun ResultCard(phase: RollPhase.Finished) {
    val outcome = phase.outcome
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                outcome.drink.name,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
            outcome.drink.sizeLabel?.let {
                Text(it, color = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                outcome.drink.priceFormatted,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Spacer(Modifier.height(8.dp))
            val rollInfo = "${outcome.category.name} · Wurf ${outcome.categoryRoll} + ${outcome.drinkRolls.joinToString("+")}"
            Text(
                if (outcome.substituted) "$rollInfo · von Hand ersetzt" else rollInfo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}
