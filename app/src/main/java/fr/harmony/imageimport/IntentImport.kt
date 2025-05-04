package fr.harmony.imageimport

import android.net.Uri

sealed class IntentImport {
    data object OpenGallery : IntentImport()
    data object OpenCamera : IntentImport()
    data object OpenDocument : IntentImport()
    data class Selected(val uri: Uri) : IntentImport()
}
