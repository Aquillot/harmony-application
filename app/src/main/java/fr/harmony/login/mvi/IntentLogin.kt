package fr.harmony.login.mvi

// Class qui représente les intentions possibles de l'écran de login par l'utilisateur
sealed class IntentLogin {
    data class Login(val email: String, val password: String) : IntentLogin()
}