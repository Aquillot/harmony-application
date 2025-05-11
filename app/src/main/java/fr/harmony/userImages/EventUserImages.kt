package fr.harmony.userImages

sealed class EventUserImages {
    data class ShowError(val message: String): EventUserImages()
}