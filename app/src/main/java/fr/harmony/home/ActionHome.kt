package fr.harmony.home


sealed class ActionHome {
    data object Loading : ActionHome()
    data class Success(val images: List<GalleryImage>) : ActionHome()
    data class Failure(val errorCode: String) : ActionHome()
    data object Empty : ActionHome()
}