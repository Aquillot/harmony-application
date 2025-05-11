package fr.harmony.homescreen


sealed class ActionHomeScreen {
    data object Loading : ActionHomeScreen()
    data class Success(val images: List<GalleryImage>) : ActionHomeScreen()
    data class Failure(val errorCode: String) : ActionHomeScreen()
    data object Empty : ActionHomeScreen()
}