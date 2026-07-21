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
import de.gyrosbande.dice.domain.Stats
import de.gyrosbande.dice.domain.StatsPresentation

/** The hall of fame: one card per fun fact (hidden while there is no data). */
@Composable
fun StatsContent(stats: Stats) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StatsPresentation.funFacts(stats).forEach { fact ->
            FunFactCard(
                emoji = fact.emoji,
                title = fact.title,
                holder = fact.holder,
                detail = fact.detail,
            )
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("📊 Bilanz", style = MaterialTheme.typography.titleMedium)
                Text(
                    StatsPresentation.tally(stats),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    StatsPresentation.revenueLine(stats),
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
