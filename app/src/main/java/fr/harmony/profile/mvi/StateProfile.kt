package fr.harmony.profile.mvi

// Class qui représente les états possibles de l'écran de profile pour l'interface utilisateur
// modifiés par uniquement la logique métier
sealed class StateProfile {
    data object Initial : StateProfile()
    data object Loading : StateProfile()
    data class Success(val email: String, val username: String, val userId: Int) : StateProfile()
    data class Error(val errorCode: String) : StateProfile()
}
