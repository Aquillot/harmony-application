package fr.harmony.explore

sealed class NavigationEventExplore {
    data object NavigateToHome : NavigationEventExplore()
    data object NavigateToUserImages : NavigationEventExplore()
}