package fr.harmony.homescreen

sealed class NavigationEventHome {
    data object NavigateToImport: NavigationEventHome()
    data class NavigateToHarmonize(val id: Long, val uri: String) : NavigationEventHome()
}