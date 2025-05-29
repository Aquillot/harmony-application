package fr.harmony.home

sealed class IntentHome {
    data class DeleteImage(val id:Long): IntentHome()
    data object EndRefresh : IntentHome()
    data object NavigateToImport : IntentHome()
    data class NavigateToHarmonize(val id: Long, val uri: String) : IntentHome()
}