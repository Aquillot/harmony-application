package fr.harmony.database

data class HarmonizationSessionModel(
    val id: Long = 0,
    val originalPath: String = "",
    val previewPath: String = "",
    val width: Int = 0,
    val height: Int = 0,
    val widthPreview: Int = 0,
    val heightPreview: Int = 0,
    val palette: List<Int> = emptyList(),
    val weights: List<List<Float>> = emptyList(),
    val harmonizedPalette: Map<String, List<Int>> = emptyMap(),
    val selectedPattern : String = ""
)

data class HarmonizationSessionModelPreview(
    val id: Long = 0,
    val originalPath: String = "",
    val previewPath: String = "",
    val widthPreview: Int = 0,
    val heightPreview: Int = 0
)