package fr.harmony.login.mvi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel


// hiltViewModel() permet d'injecter le ViewModelLogin dans la fonction
// ViewModelLogin est le ViewModel qui gère la logique métier de l'écran de login
// onLoginSuccess est une fonction de rappel qui sera appelée lorsque la connexion réussit
@Composable
fun LoginScreen(vm: ModelLogin = hiltViewModel(), onLoginSuccess: (String) -> Unit) {
    // Récupération de l'état du ViewModel en utilisant collectAsState qui permet de réagir aux changements d'état
    val state by vm.state.collectAsState()
    // Attributs de la vue
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Affichage selon l'Etat
    when (state) {
        is StateLogin.Success -> {
            // On peut naviguer vers l'écran d'accueil et passer le token
            onLoginSuccess((state as StateLogin.Success).token)
        }
        is StateLogin.Initial -> FormLogin(
            // On affiche le formulaire de login car l'utilisateur n'est pas connecté
            email, password,
            onEmailChange    = { email = it },
            onPasswordChange = { password = it },
            onSubmit         = { vm.handleIntent(IntentLogin.Login(email, password)) }
        )
        is StateLogin.Loading -> CircularProgressIndicator() // On affiche un écran de chargement
        is StateLogin.Error-> ErrorScreen(
            // On affiche un message d'erreur dans un popup et le formulaire de login
            email, password,
            onEmailChange    = { email = it },
            onPasswordChange = { password = it },
            onSubmit         = { vm.handleIntent(IntentLogin.Login(email, password))},
                (state as StateLogin.Error).message
        )
    }
}

@Composable
fun FormLogin(
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Mot de passe") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onSubmit) {
            Text("Se connecter")
        }
    }
}

@Composable
fun ErrorScreen(email: String,
                password: String,
                onEmailChange: (String) -> Unit,
                onPasswordChange: (String) -> Unit,
                onSubmit: () -> Unit,
                messageError: String) {

    Spacer(modifier = Modifier.height(16.dp))
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Mot de passe") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = messageError,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onSubmit) {
            Text("Se connecter")
        }

    }

}