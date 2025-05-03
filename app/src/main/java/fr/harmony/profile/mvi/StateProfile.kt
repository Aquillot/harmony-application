package fr.harmony.profile.mvi

// Class qui représente les états possibles de l'écran de profile pour l'interface utilisateur
// modifiés par uniquement la logique métier
sealed class StateProfile {
    object Initial : StateProfile()
    object Loading : StateProfile()
    data class Success(val email : String,val username : String) : StateProfile()
    data class Error(val errorCode: String) : StateProfile()
}
