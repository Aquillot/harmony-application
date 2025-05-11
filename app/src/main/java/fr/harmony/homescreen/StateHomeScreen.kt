package fr.harmony.homescreen


sealed class StateHomeScreen {
    data object Refreshing : StateHomeScreen()
    data object EndRefreshing : StateHomeScreen()
}