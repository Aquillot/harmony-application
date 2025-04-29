package fr.harmony.login.mvi

// Class qui représente les actions possibles de l'écran de login pour la logique métier
sealed class ActionLogin {
    object Loading : ActionLogin()
    data class Success(val token: String, val userId: Int) : ActionLogin()
    data class Failure(val errorCode: String) : ActionLogin()
}