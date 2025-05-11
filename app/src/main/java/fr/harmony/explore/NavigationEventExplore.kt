package fr.harmony.explore

sealed class NavigationEventExplore {
    data object NavigateToHome : NavigationEventExplore()
}