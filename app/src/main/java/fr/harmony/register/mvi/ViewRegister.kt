package fr.harmony.register.mvi

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
import fr.harmony.components.authentication.BaseTextField
import fr.harmony.components.authentication.EmailTextField
import fr.harmony.components.authentication.PasswordTextField
import fr.harmony.ui.theme.AppTheme
import fr.harmony.R
import fr.harmony.components.TopBar
import fr.harmony.components.authentication.FormBottomBar

// hiltViewModel() permet d'injecter le ViewModelRegister dans la fonction
// ViewModelRegister est le ViewModel qui gère la logique métier de l'écran de register
// onRegisterSuccess est une fonction de rappel qui sera appelée lorsque la connexion réussit
@Composable
fun RegisterScreen(
    vm: ModelRegister = hiltViewModel(),
    onRegisterSuccess: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val state by vm.state.collectAsState()

    // Attributs de la vue
    var email    by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }


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
            TopBar(
                returnAction = {
                    // TODO : Action de retour
                }
            )


            // Affichage selon l'Etat
            when (state) {
                is StateRegister.Success -> {
                    // On peut naviguer vers l'écran d'accueil et passer le token
                    onRegisterSuccess((state as StateRegister.Success).token)
                }

                is StateRegister.Initial, is StateRegister.Error, is StateRegister.Loading -> FormRegister(
                    // On affiche le formulaire de Register car l'utilisateur n'est pas connecté
                    email, username, password, confirmPassword,
                    onEmailChange = { email = it },
                    onUsernameChange = { username = it },
                    onPasswordChange = { password = it },
                    onConfirmPasswordChange = { confirmPassword = it },
                    errorCode = (state as? StateRegister.Error)?.errorCode,
                    loading = state is StateRegister.Loading,
                    onSubmit = {
                        vm.handleIntent(
                            IntentRegister.Register(
                                email,
                                username,
                                password,
                                confirmPassword
                            )
                        )
                    },
                    onNavigateToLogin = onNavigateToLogin
                )
            }

            // BottomBar
            Spacer(modifier = Modifier.weight(1f))
            FormBottomBar(
                text = stringResource(id = R.string.GO_TO_LOGIN),
                onClick = { onNavigateToLogin() }
            )
        }
    }
}

@Composable
fun FormRegister(
    email: String,
    username: String,
    password: String,
    confirmPassword: String,
    onEmailChange: (String) -> Unit,
    onUsernameChange : (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    loading: Boolean = false,
    errorCode: String? = null,
    onSubmit: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val (usernameFocus, passwordFocus, confirmPasswordFocus) = remember { FocusRequester.createRefs() }

    // États pour la validation
    var emailError by remember { mutableStateOf(false) }
    var usernameLengthError by remember { mutableStateOf(false) }
    var usernameContentError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var confirmPasswordError by remember { mutableStateOf(false) }

    val validationMessageErrors = mapOf(
        "INVALID_EMAIL" to stringResource(id = R.string.INVALID_EMAIL),
        "INVALID_USERNAME_LENGTH" to stringResource(id = R.string.INVALID_USERNAME_LENGTH),
        "INVALID_USERNAME_CONTENT" to stringResource(id = R.string.INVALID_USERNAME_CONTENT),
        "INVALID_PASSWORD" to stringResource(id = R.string.INVALID_PASSWORD),
        "INVALID_CONFIRM_PASSWORD" to stringResource(id = R.string.INVALID_CONFIRM_PASSWORD),
    )

    val messageError = when (errorCode) {
        "MISSING_DATA" -> stringResource(id = R.string.MISSING_DATA)
        "EMAIL_ALREADY_USED" -> stringResource(id = R.string.EMAIL_ALREADY_USED)
        "DATABASE_ERROR" -> stringResource(id = R.string.DATABASE_ERROR)
        else -> stringResource(id = R.string.UNKNOWN_ERROR)
    }

    // Validation de l'email avec une regex basique
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Validation de la longueur du nom d'utilisateur : entre 3 et 20 caractères
    fun isValidUsername(username: String): Boolean {
        return username.length in 3..20
    }

    // Validation du contenu du nom d'utilisateur : pas d'espaces, pas de caractères spéciaux saufs _
    fun isValidUsernameContent(username: String): Boolean {
        return username.all { it.isLetterOrDigit() || it == '_' || it == '-' }
    }

    // Validation de la longueur du mot de passe : > 6 caractères
    fun isValidPassword(password: String): Boolean {
        return password.length > 6
    }

    fun isValidConfirmPassword(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    fun updateErrorState() {
        emailError = email.isBlank() || !isValidEmail(email)
        usernameLengthError = username.isBlank() || !isValidUsername(username)
        usernameContentError = username.isBlank() || !isValidUsernameContent(username)
        passwordError = password.isBlank() || !isValidPassword(password)
        confirmPasswordError = confirmPassword.isBlank() || !isValidConfirmPassword(password, confirmPassword)
    }

    // Validation de l'email et du mot de passe
    fun validateAndSubmit() {
        updateErrorState()

        if (emailError) {
            Toast.makeText(context, validationMessageErrors["INVALID_EMAIL"], Toast.LENGTH_SHORT).show()
        } else if (usernameLengthError) {
            Toast.makeText(context, validationMessageErrors["INVALID_USERNAME_LENGTH"], Toast.LENGTH_SHORT).show()
        } else if (usernameContentError) {
            Toast.makeText(context, validationMessageErrors["INVALID_USERNAME_CONTENT"], Toast.LENGTH_SHORT).show()
        } else if (passwordError) {
            Toast.makeText(context, validationMessageErrors["INVALID_PASSWORD"], Toast.LENGTH_SHORT).show()
        } else if (confirmPasswordError) {
            Toast.makeText(context, validationMessageErrors["INVALID_CONFIRM_PASSWORD"], Toast.LENGTH_SHORT).show()
        } else {
            focusManager.clearFocus()
            onSubmit()
        }
    }

    fun onEmailFocusChanged(focusState: FocusState) {
        // L'erreur ne sera vérifiée que lorsque l'utilisateur perd le focus
        emailError = !focusState.isFocused && (email.isBlank() || !isValidEmail(email))
    }

    fun onUsernameFocusChanged(focusState: FocusState) {
        // L'erreur ne sera vérifiée que lorsque l'utilisateur perd le focus
        usernameLengthError = !focusState.isFocused && (username.isBlank() || !isValidUsername(username))
        usernameContentError = !focusState.isFocused && (username.isBlank() || !isValidUsernameContent(username))
    }

    fun onPasswordFocusChanged(focusState: FocusState) {
        // L'erreur ne sera vérifiée que lorsque l'utilisateur perd le focus
        passwordError = !focusState.isFocused && (password.isBlank() || !isValidPassword(password))
    }

    fun onConfirmPasswordFocusChanged(focusState: FocusState) {
        // L'erreur ne sera vérifiée que lorsque l'utilisateur perd le focus
        confirmPasswordError = !focusState.isFocused && (confirmPassword.isBlank() || !isValidConfirmPassword(password, confirmPassword))
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
            text = stringResource(id = R.string.REGISTER_TITLE),
            style = AppTheme.harmonyTypography.title,
            color = AppTheme.harmonyColors.textColor,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        EmailTextField(
            email = email,
            onEmailChange = onEmailChange,
            onNext = { usernameFocus.requestFocus() },
            isError = emailError,
            modifier = Modifier.onFocusChanged { onEmailFocusChanged(it) },
        )
        BaseTextField(
            value = username,
            onValueChange = onUsernameChange,
            focusRequester = usernameFocus,
            onNext = { passwordFocus.requestFocus() },
            label = stringResource(id = R.string.USERNAME_LABEL),
            isError = usernameLengthError || usernameContentError,
            modifier = Modifier.onFocusChanged { onUsernameFocusChanged(it) },
        )
        PasswordTextField(
            password = password,
            onPasswordChange = onPasswordChange,
            focusRequester = passwordFocus,
            onNext = { confirmPasswordFocus.requestFocus() },
            isError = passwordError,
            modifier = Modifier
                .padding(top = 12.dp)
                .onFocusChanged { onPasswordFocusChanged(it) }
        )
        PasswordTextField(
            password = confirmPassword,
            onPasswordChange = onConfirmPasswordChange,
            label = stringResource(id = R.string.CONFIRM_PASSWORD_LABEL),
            focusRequester = confirmPasswordFocus,
            onDone = {
                focusManager.clearFocus()
                validateAndSubmit()
            },
            isError = confirmPasswordError,
            modifier = Modifier.onFocusChanged { onConfirmPasswordFocusChanged(it) }
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
            enabled = !loading && email.isNotBlank() && username.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(top = 14.dp)
        ) {
            Text(stringResource(id = R.string.REGISTER_BUTTON))
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