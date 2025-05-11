package fr.harmony.homescreen

sealed class IntentHomeScreen {
    data class DeleteImage(val id:Long): IntentHomeScreen()
    data object EndRefresh : IntentHomeScreen()
}