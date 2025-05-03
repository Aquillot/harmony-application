package fr.harmony

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun HomeScreen(username : String?) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Travaux En Cours !", style = MaterialTheme.typography.headlineMedium)
        Text("Voila ton username : $username", style = MaterialTheme.typography.bodyMedium)
    }
}

