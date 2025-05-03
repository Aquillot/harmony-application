package fr.harmony.register.mvi

// Class qui représente les états possibles de l'écran de login pour l'interface utilisateur
// modifiés par uniquement la logique métier
sealed class StateRegister {
    object Initial : StateRegister()
    object Loading : StateRegister()
    object Success : StateRegister()
    data class Error(val errorCode: String) : StateRegister()
}
