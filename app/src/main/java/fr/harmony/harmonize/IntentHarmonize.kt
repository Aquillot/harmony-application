package fr.harmony.harmonize

sealed class IntentHarmonize {
    data object ToggleShareMenu      : IntentHarmonize()
    data object ShareAppClicked      : IntentHarmonize()
    data object ShareExternalClicked : IntentHarmonize()
    data object DownloadImage : IntentHarmonize()
    data class SliderChanged(val value: Float) : IntentHarmonize()
    data class PatternSelected(val pattern: String) : IntentHarmonize()
}