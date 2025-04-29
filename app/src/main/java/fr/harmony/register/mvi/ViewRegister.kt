package fr.harmony.register.mvi

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmony.components.authentication.BaseTextField
import fr.harmony.components.authentication.EmailTextField
import fr.harmony.components.authentication.PasswordTextField
import fr.harmony.ui.theme.AppTheme
import fr.harmony.R

// hiltViewModel() permet d'injecter le ViewModelRegister dans la fonction
// ViewModelRegister est le ViewModel qui gère la logique métier de l'écran de register
// onRegisterSuccess est une fonction de rappel qui sera appelée lorsque la connexion réussit
@Composable
fun RegisterScreen(vm: ModelRegister = hiltViewModel(), onRegisterSuccess: (String) -> Unit, onNavigateToLogin: () -> Unit) {
    // Récupération de l'état du ViewModel en utilisant collectAsState qui permet de réagir aux changements d'état
    val state by vm.state.collectAsState()
    // Attributs de la vue
    var email    by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp, 0.dp),
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
                        contentDescription = "Retour",
                        tint = AppTheme.harmonyColors.textColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }


            // Affichage selon l'Etat
            when (state) {
                is StateRegister.Success -> {
                    // On peut naviguer vers l'écran d'accueil et passer le token
                    onRegisterSuccess((state as StateRegister.Success).token)
                }

                is StateRegister.Initial -> FormRegister(
                    // On affiche le formulaire de Register car l'utilisateur n'est pas connecté
                    email, username, password,
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onUsernameChange = { username = it },
                    onSubmit = {
                        vm.handleIntent(
                            IntentRegister.Register(
                                email,
                                username,
                                password
                            )
                        )
                    },
                    onNavigateToLogin = onNavigateToLogin
                )

                is StateRegister.Loading -> CircularProgressIndicator() // On affiche un écran de chargement
                is StateRegister.Error -> ErrorScreen(
                    // On affiche un message d'erreur dans un toast et le formulaire de register
                    email, username, password,
                    onEmailChange = { email = it },
                    onUsernameChange = { username = it },
                    onPasswordChange = { password = it },
                    onSubmit = {
                        vm.handleIntent(
                            IntentRegister.Register(
                                email,
                                username,
                                password
                            )
                        )
                    },
                    onNavigateToLogin = onNavigateToLogin,
                    (state as StateRegister.Error).message
                )
            }

            // BottomBar
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                onClick = { onNavigateToLogin() },
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
                    text = stringResource(id = R.string.GO_TO_LOGIN),
                    style = AppTheme.harmonyTypography.smallLine,
                    color = AppTheme.harmonyColors.textColor
                )
            }
        }
    }
}

@Composable
fun FormRegister(
    email: String,
    username: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onUsernameChange : (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    // Container du formulaire
    Column(
        modifier = Modifier.padding(6.dp, 11.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Titre du formulaire
        Text(
            text = stringResource(id = R.string.REGISTER_TITLE),
            style = AppTheme.harmonyTypography.title,
            color = AppTheme.harmonyColors.textColor,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        EmailTextField(email = email, onEmailChange = onEmailChange)
        BaseTextField(value = username, onValueChange = onUsernameChange, label = "Nom d'utilisateur")
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
            Text(stringResource(id = R.string.REGISTER_BUTTON))
        }
    }
}

@Composable
fun ErrorScreen(email: String,
                username: String,
                password: String,
                onEmailChange: (String) -> Unit,
                onUsernameChange: (String) -> Unit,
                onPasswordChange: (String) -> Unit,
                onSubmit: () -> Unit,
                onNavigateToLogin: () -> Unit,
                messageError: String) {

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
                text = "S'inscrire",
                style = MaterialTheme.typography.titleLarge,
                color = AppTheme.harmonyColors.textColor,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            EmailTextField(email = email, onEmailChange = onEmailChange)
            BaseTextField(value = username, onValueChange = onUsernameChange, label = "Nom d'utilisateur")
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
                Text("S'inscrire")
            }
        }
    }
        // Affichage du message d'erreur dans le toast
        Toast.makeText(
            LocalContext.current,
            if (messageError == "MISSING_DATA")
                stringResource(id = R.string.MISSING_DATA)
            else if (messageError == "EMAIL_ALREADY_USED")
                stringResource(id = R.string.EMAIL_ALREADY_USED)
            else if (messageError == "DATABASE_ERROR")
                stringResource(id = R.string.DATABASE_ERROR)
            else
                stringResource(id = R.string.UNKNOWN_ERROR),
            Toast.LENGTH_SHORT
        ).show()
}