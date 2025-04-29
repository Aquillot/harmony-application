package fr.harmony.login.mvi

// Class qui représente les états possibles de l'écran de login pour l'interface utilisateur
// modifiés par uniquement la logique métier
sealed class StateLogin {
    object Initial : StateLogin()
    object Loading : StateLogin()
    data class Success(val token : String) : StateLogin()
    data class Error(val errorCode: String) : StateLogin()
}
