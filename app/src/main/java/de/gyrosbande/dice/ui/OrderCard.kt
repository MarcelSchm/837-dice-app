package de.gyrosbande.dice.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.gyrosbande.dice.domain.OrderLine
import de.gyrosbande.dice.domain.OrderSummary

/**
 * The gold order card: grouped drinks with quantities and the total.
 * Used at the end of a round and in the history detail.
 */
@Composable
fun OrderCard(lines: List<OrderLine>, totalCents: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            lines.forEach { line ->
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
                    OrderSummary.formatCents(totalCents),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}
