package de.gyrosbande.dice.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.gyrosbande.dice.R

@Composable
fun HomeScreen(
    onStartRound: () -> Unit,
    onQuickRoll: () -> Unit,
    onPlayers: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.logo_gyrosbande),
            contentDescription = "837 Gyrosbande Logo",
            modifier = Modifier.size(200.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text("837 Dice", style = MaterialTheme.typography.displaySmall)
        Text(
            "Das Schnapswürfeln der Gyrosbande",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = onStartRound,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
        ) {
            Text("Neue Runde 🎲", style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onQuickRoll,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text("Schnell würfeln", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onPlayers,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text("Spieler", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "Historie & Kartenpflege folgen bald.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
