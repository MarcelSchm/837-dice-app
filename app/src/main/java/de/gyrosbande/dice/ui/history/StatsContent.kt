package de.gyrosbande.dice.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.gyrosbande.dice.domain.OrderSummary
import de.gyrosbande.dice.domain.Stats

/** The hall of fame: one card per fun fact (hidden while there is no data). */
@Composable
fun StatsContent(stats: Stats) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        stats.proseccoKing?.let {
            FunFactCard(
                emoji = "🍾",
                title = "Prosecco-König:in",
                holder = it.names.joinToString(" & "),
                detail = "${it.count}× die Flasche Prosecco erwischt",
            )
        }
        stats.topSpender?.let {
            FunFactCard(
                emoji = "💸",
                title = "Spendierhosen",
                holder = it.names.joinToString(" & "),
                detail = "insgesamt ${OrderSummary.formatCents(it.cents)} verwürfelt",
            )
        }
        stats.favoriteDrink?.let {
            FunFactCard(
                emoji = "🥃",
                title = "Stammgetränk der Bande",
                holder = it.drinkName + (it.sizeLabel?.let { size -> " ($size)" } ?: ""),
                detail = "${it.count}× gewürfelt",
            )
        }
        stats.categoryMagnet?.let {
            FunFactCard(
                emoji = "🎯",
                title = "Kategorien-Magnet",
                holder = it.names.joinToString(" & "),
                detail = "${it.count}× in ${it.categoryName} gelandet",
            )
        }
        stats.doublesChamp?.let {
            FunFactCard(
                emoji = "🎲",
                title = "Pasch-Profi",
                holder = it.names.joinToString(" & "),
                detail = if (it.count == 1) "1 Pasch beim Drink-Wurf" else "${it.count} Päsche beim Drink-Wurf",
            )
        }
        stats.wrapVictim?.let {
            FunFactCard(
                emoji = "🔄",
                title = "Wrap-Opfer",
                holder = it.names.joinToString(" & "),
                detail = "${it.count}× unten durch und oben wieder reingerutscht",
            )
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("📊 Bilanz", style = MaterialTheme.typography.titleMedium)
                val roundsLabel = if (stats.roundCount == 1) "Runde" else "Runden"
                val rollsLabel = if (stats.rollCount == 1) "Wurf" else "Würfe"
                Text(
                    "${stats.roundCount} $roundsLabel, ${stats.rollCount} $rollsLabel",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "Ihr habt dem San Remo schon ${stats.totalFormatted} beschert 🇬🇷",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FunFactCard(emoji: String, title: String, holder: String, detail: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(emoji, style = MaterialTheme.typography.headlineMedium)
            Column(Modifier.padding(start = 16.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    holder,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
