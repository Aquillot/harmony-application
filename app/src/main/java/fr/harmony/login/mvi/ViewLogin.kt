package fr.harmony.login.mvi

import androidx.compose.foundation.BorderStroke
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmony.components.authentication.EmailTextField
import fr.harmony.components.authentication.PasswordTextField
import fr.harmony.ui.theme.AppTheme
import fr.harmony.R

// hiltViewModel() permet d'injecter le ViewModelLogin dans la fonction
// ViewModelLogin est le ViewModel qui gère la logique métier de l'écran de login
// onLoginSuccess est une fonction de rappel qui sera appelée lorsque la connexion réussit
@Composable
fun LoginScreen(vm: ModelLogin = hiltViewModel(), onLoginSuccess: (String) -> Unit,onNavigateToRegister : () -> Unit) {
    // Récupération de l'état du ViewModel en utilisant collectAsState qui permet de réagir aux changements d'état
    val state by vm.state.collectAsState()
    // Attributs de la vue
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(11.dp, 40.dp, 11.dp, 11.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // TopBar : Bouton de retour
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp, 0.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedIconButton(
                    onClick = { /* TODO: Gérer le retour */ },
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        containerColor = AppTheme.harmonyColors.darkCard,
                        contentColor = AppTheme.harmonyColors.subtleTextColor,
                    ),
                    border = BorderStroke(
                        1.dp,
                        AppTheme.harmonyColors.darkCardStroke
                    ),
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_left),
                        contentDescription = stringResource(id = R.string.BACK_BUTTON),
                        tint = AppTheme.harmonyColors.textColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Affichage selon l'Etat
            when (state) {
                is StateLogin.Success -> {
                    // On peut naviguer vers l'écran d'accueil et passer le token
                    onLoginSuccess((state as StateLogin.Success).token)
                }

                is StateLogin.Initial -> FormLogin(
                    // On affiche le formulaire de login car l'utilisateur n'est pas connecté
                    email, password,
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onSubmit = { vm.handleIntent(IntentLogin.Login(email, password)) },
                onNavigateToRegister = onNavigateToRegister
            )

                is StateLogin.Loading -> CircularProgressIndicator() // On affiche un écran de chargement
                is StateLogin.Error -> ErrorScreen(
                    // On affiche un message d'erreur dans un toast et le formulaire de login
                    email, password,
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onSubmit = { vm.handleIntent(IntentLogin.Login(email, password)) },
                    onNavigateToRegister = onNavigateToRegister,
                (state as StateLogin.Error).errorCode
                )
            }

            // BottomBar
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                onClick = { onNavigateToRegister() },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .navigationBarsPadding(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Transparent,
                ),
            ) {
                Text(
                    text = stringResource(id = R.string.GO_TO_REGISTER),
                    style = AppTheme.harmonyTypography.smallLine,
                    color = AppTheme.harmonyColors.textColor
                )
            }
        }
    }
}

@Composable
fun FormLogin(
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onNavigateToRegister : () -> Unit
) {

    // Container du formulaire
    Column(
        modifier = Modifier.padding(6.dp, 11.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Titre du formulaire
        Text(
            text = stringResource(id = R.string.LOGIN_TITLE),
            style = AppTheme.harmonyTypography.title,
            color = AppTheme.harmonyColors.textColor,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        EmailTextField(email = email, onEmailChange = onEmailChange)
        PasswordTextField(password = password, onPasswordChange = onPasswordChange)

        Button(
            onClick = onSubmit,
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.harmonyColors.lightCard,
                contentColor = AppTheme.harmonyColors.darkTextColor
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(top = 14.dp)
        ) {
            Text(stringResource(id = R.string.LOGIN_BUTTON))
        }
    }
}

@Composable
fun ErrorScreen(
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onNavigateToRegister : () -> Unit,
    messageError: String
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(11.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Container du formulaire
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp, 11.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Titre du formulaire
            Text(
                text = "Se connecter",
                style = MaterialTheme.typography.titleLarge,
                color = AppTheme.harmonyColors.textColor,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            EmailTextField(email = email, onEmailChange = onEmailChange)
            PasswordTextField(password = password, onPasswordChange = onPasswordChange)

            Button(
                onClick = onSubmit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.harmonyColors.lightCard,
                    contentColor = AppTheme.harmonyColors.darkTextColor
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(top = 14.dp)
            ) {
                Text("Se connecter")
            }
            Button(
                onClick = onNavigateToRegister,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.harmonyColors.lightCard,
                    contentColor = AppTheme.harmonyColors.darkTextColor
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(top = 14.dp)
            ) {
                Text("Créer un compte")
            }
        }
        // Affichage du message d'erreur dans le toast
        Toast.makeText(
            LocalContext.current,
            if (messageError == "INVALID_CREDENTIALS")
                stringResource(id = R.string.INVALID_CREDENTIALS)
            else if (messageError == "ERROR_LOGIN_400")
                stringResource(id = R.string.ERROR_LOGIN_400)
            else
                stringResource(id = R.string.UNKNOWN_ERROR),
            Toast.LENGTH_SHORT
        ).show()
    }
}