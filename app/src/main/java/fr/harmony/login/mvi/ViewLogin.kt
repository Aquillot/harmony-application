package fr.harmony.login.mvi

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.harmony.components.authentication.EmailTextField
import fr.harmony.components.authentication.PasswordTextField
import fr.harmony.components.authentication.FormBottomBar
import fr.harmony.ui.theme.AppTheme
import fr.harmony.R
import fr.harmony.components.TopBar

// hiltViewModel() permet d'injecter le ViewModelLogin dans la fonction
// ViewModelLogin est le ViewModel qui gère la logique métier de l'écran de login
// onLoginSuccess est une fonction de rappel qui sera appelée lorsque la connexion réussit
@Composable
fun LoginScreen(
    vm: ModelLogin = hiltViewModel(),
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
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
            TopBar(
                returnAction = {
                    // TODO : Action de retour
                }
            )


            // Affichage selon l'Etat
            when (state) {
                is StateLogin.Success -> {
                    // On peut naviguer vers l'écran d'accueil et passer le token
                    onLoginSuccess((state as StateLogin.Success).token)
                }

                is StateLogin.Initial, is StateLogin.Error, is StateLogin.Loading -> FormLogin(
                    // On affiche le formulaire de login car l'utilisateur n'est pas connecté
                    email, password,
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    errorCode = (state as? StateLogin.Error)?.errorCode,
                    loading = state is StateLogin.Loading,
                    onSubmit = { vm.handleIntent(IntentLogin.Login(email, password)) },
                    onNavigateToRegister = onNavigateToRegister,
                )
            }

            // BottomBar
            Spacer(modifier = Modifier.weight(1f))
            FormBottomBar(
                text = stringResource(id = R.string.GO_TO_REGISTER),
                onClick = { onNavigateToRegister() }
            )
        }
    }
}

@Composable
fun FormLogin(
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    loading: Boolean = false,
    onSubmit: () -> Unit,
    onNavigateToRegister: () -> Unit,
    errorCode: String? = null
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val passwordFocus = remember { FocusRequester() }

    // États pour la validation
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    val validationMessageErrors = mapOf(
        "INVALID_EMAIL" to stringResource(id = R.string.INVALID_EMAIL),
        "INVALID_PASSWORD" to stringResource(id = R.string.INVALID_PASSWORD),
    )

    val messageError = when (errorCode) {
        "INVALID_CREDENTIALS" -> stringResource(id = R.string.INVALID_CREDENTIALS)
        "ERROR_LOGIN_400" -> stringResource(id = R.string.ERROR_LOGIN_400)
        "UNKNOWN_ERROR" -> stringResource(id = R.string.UNKNOWN_ERROR)
        else -> stringResource(id = R.string.UNKNOWN_ERROR)
    }

    // Validation de l'email avec une regex basique
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Validation de l'email et du mot de passe
    fun validateAndSubmit() {
        emailError = email.isBlank() || !isValidEmail(email)
        passwordError = password.length < 6

        if (emailError) {
            Toast.makeText(context, validationMessageErrors["INVALID_EMAIL"], Toast.LENGTH_SHORT).show()
        } else if (passwordError) {
            Toast.makeText(context, validationMessageErrors["INVALID_PASSWORD"], Toast.LENGTH_SHORT).show()
        } else {
            focusManager.clearFocus()
            onSubmit()
        }
    }

    fun onPasswordFocusChanged(focusState: FocusState) {
        // L'erreur ne sera vérifiée que lorsque l'utilisateur perd le focus
        passwordError = !focusState.isFocused && (password.isBlank() || password.length < 6)
    }

    fun onEmailFocusChanged(focusState: FocusState) {
        // L'erreur ne sera vérifiée que lorsque l'utilisateur perd le focus
        emailError = !focusState.isFocused && (email.isBlank() || !isValidEmail(email))
    }

    LaunchedEffect(errorCode) {
        errorCode?.let {
            Toast.makeText(context, messageError, Toast.LENGTH_SHORT).show()
        }
    }

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

        EmailTextField(
            email = email,
            onEmailChange = onEmailChange,
            onNext = { passwordFocus.requestFocus() },
            isError = emailError,
            modifier = Modifier.onFocusChanged { onEmailFocusChanged(it) },
        )
        PasswordTextField(
            password = password,
            focusRequester = passwordFocus,
            onPasswordChange = onPasswordChange,
            onDone = {
                validateAndSubmit()
            },
            isError = passwordError,
            modifier = Modifier.onFocusChanged { onPasswordFocusChanged(it) },
        )

        Button(
            onClick = { validateAndSubmit() },
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.harmonyColors.lightCard,
                contentColor = AppTheme.harmonyColors.darkTextColor,
                disabledContainerColor = AppTheme.harmonyColors.disabledTextColor,
                disabledContentColor = AppTheme.harmonyColors.darkTextColor,
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !loading && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(top = 14.dp)
        ) {
            Text(stringResource(id = R.string.LOGIN_BUTTON))
        }

        // Affichage du loader si le form a été soumis
        if (loading) {
            LoadingView()
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