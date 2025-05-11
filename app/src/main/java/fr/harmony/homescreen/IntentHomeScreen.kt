package fr.harmony.homescreen

sealed class IntentHomeScreen {
    data class DeleteImage(val id:Long): IntentHomeScreen()
    data object EndRefresh : IntentHomeScreen()
    data object NavigateToImport : IntentHomeScreen()
    data class NavigateToHarmonize(val id: Long, val uri: String) : IntentHomeScreen()
}