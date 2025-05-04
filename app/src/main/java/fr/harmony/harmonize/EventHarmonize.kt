package fr.harmony.harmonize

import android.graphics.Bitmap

sealed class EventHarmonize {
    data class LaunchShareIntent(val shareIntent: android.content.Intent) : EventHarmonize()
    data class ShowSnackbar(val message: String) : EventHarmonize()
    data class RequestSaveImage(val bitmap: Bitmap) : EventHarmonize()
}
