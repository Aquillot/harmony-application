package fr.harmony.harmonize

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap

/**
 * Reconstruit un Bitmap à partir d’une palette et de matrices de poids.
 */
fun reconstructBitmap(
    palette: List<Int>,
    layerWeights: List<Array<FloatArray>>,
    width: Int,
    height: Int
): Bitmap {
    require(palette.size == layerWeights.size) {
        "Palette size must match number of layers"
    }
    val bmp = createBitmap(width, height)
    for (y in 0 until height) for (x in 0 until width) {
        var aAcc = 0f; var rAcc = 0f; var gAcc = 0f; var bAcc = 0f
        palette.forEachIndexed { i, color ->
            val w = layerWeights[i][y][x]
            aAcc += Color.alpha(color) * w
            rAcc += Color.red(color)   * w
            gAcc += Color.green(color) * w
            bAcc += Color.blue(color)  * w
        }
        val final = Color.argb(
            aAcc.coerceIn(0f,255f).toInt(),
            rAcc.coerceIn(0f,255f).toInt(),
            gAcc.coerceIn(0f,255f).toInt(),
            bAcc.coerceIn(0f,255f).toInt()
        )
        bmp.setPixel(x, y, final)
    }
    return bmp
}

/**
 * Convertit une liste de valeurs flottantes en une matrice 2D.
 *
 * @receiver La liste de valeurs flottantes à convertir.
 * @param width La largeur de la matrice (nombre de colonnes).
 * @param height La hauteur de la matrice (nombre de lignes).
 * @return Une matrice 2D sous forme d'Array<FloatArray>.
 * @throws IllegalArgumentException Si la taille de la liste ne correspond pas à width * height.
 */
fun List<Float>.toMatrix(width: Int, height: Int): Array<FloatArray> {
    require(size == width * height)
    return Array(height) { y ->
        FloatArray(width) { x -> this[y * width + x] }
    }
}

/**
 * Mélange deux couleurs en fonction d'un ratio donné.
 *
 * @param c1 La première couleur, représentée sous forme d'entier ARGB.
 * @param c2 La deuxième couleur, représentée sous forme d'entier ARGB.
 * @param ratio Le ratio de mélange entre les deux couleurs (compris entre 0.0 et 1.0).
 *              Un ratio de 0.0 retourne la première couleur, tandis qu'un ratio de 1.0 retourne la deuxième couleur.
 * @return La couleur résultante du mélange, représentée sous forme d'entier ARGB.
 */
fun blendColors(c1: Int, c2: Int, ratio: Float): Int {
    val r = (Color.red(c1)   * (1-ratio) + Color.red(c2)   * ratio).toInt()
    val g = (Color.green(c1) * (1-ratio) + Color.green(c2) * ratio).toInt()
    val b = (Color.blue(c1)  * (1-ratio) + Color.blue(c2)  * ratio).toInt()
    val a = (Color.alpha(c1) * (1-ratio) + Color.alpha(c2) * ratio).toInt()
    return Color.argb(a,r,g,b)
}
