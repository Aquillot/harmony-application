package fr.harmony.register.mvi

// Class qui représente les intentions possibles de l'écran de register par l'utilisateur
sealed class IntentRegister {
    data class Register(val email: String,val username:String, val password: String, val confirmPassword: String) : IntentRegister()
}