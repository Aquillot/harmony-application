package fr.harmony.imageimport

import android.net.Uri

sealed class NavigationEventImport {
    data object OpenGallery : NavigationEventImport()
    data object OpenCamera : NavigationEventImport()
    data object OpenDocument : NavigationEventImport()
    data class NavigateHarmonize(val uri: Uri) : NavigationEventImport()
}
