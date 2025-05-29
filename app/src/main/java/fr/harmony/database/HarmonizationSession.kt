package fr.harmony.database

import io.objectbox.annotation.*

@Entity
data class HarmonizationSession(
    @Id var id: Long = 0,

    var originalPath: String = "", // chemin de l'image originale
    var previewPath: String = "", // chemin de l'image de preview

    var widthPreview : Int = 0, // largeur de l'image de preview
    var heightPreview : Int = 0, // hauteur de l'image de preview

    var width : Int = 0, // largeur de l'image
    var height : Int = 0, // hauteur de l'image

    // Stocker les listes en tant que JSON
    var paletteJson: String= "",
    var weightsJson: String= "",
    var harmonizedJson: String= "",
    var selectedPattern: String = "",

    var slider: Float = 1f
    )
