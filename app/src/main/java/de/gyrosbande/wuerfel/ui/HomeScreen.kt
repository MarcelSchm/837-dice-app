package de.gyrosbande.wuerfel.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.gyrosbande.wuerfel.R

@Composable
fun HomeScreen(onStartRoll: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.logo_gyrosbande),
            contentDescription = "837 Gyrosbande Logo",
            modifier = Modifier.size(220.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text("837 Würfel", style = MaterialTheme.typography.displaySmall)
        Text(
            "Das Schnapswürfeln der Gyrosbande",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = onStartRoll,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
        ) {
            Text("Würfeln 🎲", style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "Runden, Spieler, Historie & Kartenpflege folgen bald.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
