package de.gyrosbande.dice.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.gyrosbande.dice.domain.ExtraItem
import de.gyrosbande.dice.domain.OrderLine
import de.gyrosbande.dice.domain.OrderSummary

/**
 * The gold order card: grouped drinks with quantities, optional manually
 * added extras (food, beer ...) and the total. Used at the end of a round
 * and in the history detail. When [onExtraClick] is set, tapping an extra
 * row triggers it (used for removing extras while the round is open).
 */
@Composable
fun OrderCard(
    lines: List<OrderLine>,
    totalCents: Int,
    modifier: Modifier = Modifier,
    extras: List<ExtraItem> = emptyList(),
    onExtraClick: ((Int) -> Unit)? = null,
    /** Set to make drink lines tappable ("they don't have that"). */
    onDrinkClick: ((de.gyrosbande.dice.domain.Drink) -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            lines.forEach { line ->
                OrderRow(
                    quantity = line.quantity,
                    label = line.drink.name + (line.drink.sizeLabel?.let { " ($it)" } ?: ""),
                    amount = line.totalFormatted,
                    modifier = if (onDrinkClick != null) {
                        Modifier.clickable { onDrinkClick(line.drink) }
                    } else {
                        Modifier
                    },
                )
            }
            extras.forEachIndexed { index, extra ->
                OrderRow(
                    quantity = extra.quantity,
                    label = extra.label,
                    amount = OrderSummary.formatCents(extra.totalCents),
                    italic = true,
                    modifier = if (onExtraClick != null) {
                        Modifier.clickable { onExtraClick(index) }
                    } else {
                        Modifier
                    },
                )
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

@Composable
private fun OrderRow(
    quantity: Int,
    label: String,
    amount: String,
    italic: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Row(modifier) {
        Text(
            "$quantity×",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(end = 12.dp),
        )
        Text(
            label,
            color = MaterialTheme.colorScheme.onPrimary,
            fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
            modifier = Modifier.weight(1f),
        )
        Text(amount, color = MaterialTheme.colorScheme.onPrimary)
    }
}
