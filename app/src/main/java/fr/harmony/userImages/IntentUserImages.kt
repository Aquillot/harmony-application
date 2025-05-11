package fr.harmony.userImages

import fr.harmony.userImages.data.SharedImage


sealed class IntentUserImages {
    data object LoadImages : IntentUserImages()
    data class ShowPopup(val image: SharedImage) : IntentUserImages()
    data object HidePopup : IntentUserImages()
    data object DeleteImage : IntentUserImages()
    data object NavigateToExplore : IntentUserImages()
}