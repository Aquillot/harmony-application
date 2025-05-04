package fr.harmony.harmonize

import android.graphics.Bitmap
import android.net.Uri
import androidx.core.graphics.createBitmap

data class StateHarmonize(
    val imageUri: Uri = Uri.EMPTY,
    val sliderPosition: Float = 1f,
    val selectedPattern: String = "",
    val shareVisible: Boolean = false,
    val shareOffset: Int = 0,
    val step: HarmonizeStep = HarmonizeStep.SEND_IMAGE,
    val palette: List<Int> = emptyList(),
    val weights: List<List<Float>> = emptyList(),
    val harmonizedPalette: Map<String, List<Int>> = emptyMap(),
    val imageWidth: Int = 0,
    val imageHeight: Int = 0,
    val imageBitmap: Bitmap = createBitmap(1, 1),
)