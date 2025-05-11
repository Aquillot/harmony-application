package fr.harmony.explore

sealed class EventExplore {
    data class ShowError(val message: String): EventExplore()
}