package fr.harmony.profile.mvi

// Class qui représente les actions possibles de l'écran de profile pour la logique métier
sealed class ActionProfile {
    object Loading : ActionProfile()
    data class Success(val id:Int, val email:String, val username:String ) : ActionProfile()
    data class Failure(val errorCode: String) : ActionProfile()
}