package fr.harmony.profile.mvi

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmony.User

// hiltViewModel() permet d'injecter le ModelProfile dans la fonction
// ViewModelProfile est le ViewModel qui gère la logique métier de l'écran de login
// onGetTokenSuccess est une fonction de rappel qui sera appelée lorsque la connexion réussit
@Composable
fun ProfileScreen(
    model: ModelProfile = hiltViewModel(),
    onGetTokenSuccess: (User) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    // On déclenche l’intention qu’une seule fois
    LaunchedEffect(Unit) {
        model.handleIntent(IntentProfile.GetProfile)
    }

    val state by model.state.collectAsState()


    when (state) {
        is StateProfile.Initial -> {
            // État initial : on affiche un écran de chargement
            LoadingView() //TODO Modifier le style
        }
        is StateProfile.Loading -> {
            // État de chargement : on affiche un écran de chargement
            LoadingView()
        }
        is StateProfile.Success -> {
            LaunchedEffect(Unit) {
                // État de succès : on affiche le profil de l'utilisateur
                val user = User(
                    username = (state as StateProfile.Success).username,
                    email = (state as StateProfile.Success).email,
                    userId = (state as StateProfile.Success).userId
                )
                onGetTokenSuccess(user)
            }
        }
        is StateProfile.Error -> {
            // État d'erreur : on affiche un message d'erreur
            LaunchedEffect(Unit) {
                onNavigateToLogin()
            }
        }
    }
}


@Composable
fun LoadingView() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}