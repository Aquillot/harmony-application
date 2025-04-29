package fr.harmony.register.mvi

// Class qui représente les actions possibles de l'écran de register pour la logique métier
sealed class ActionRegister {
    object Loading : ActionRegister()
    data class Success(val token: String, val userId: Int) : ActionRegister()
    data class Failure(val error: String) : ActionRegister()
}