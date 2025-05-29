package fr.harmony.home

sealed class NavigationEventHome {
    data object NavigateToImport: NavigationEventHome()
    data class NavigateToHarmonize(val id: Long, val uri: String) : NavigationEventHome()
}