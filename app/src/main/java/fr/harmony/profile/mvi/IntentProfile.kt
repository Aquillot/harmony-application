package fr.harmony.profile.mvi

// Class qui représente les intentions possibles de l'écran de profile par l'utilisateur
sealed class IntentProfile {
    object GetProfile : IntentProfile()
}