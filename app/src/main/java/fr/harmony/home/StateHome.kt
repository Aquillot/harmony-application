package fr.harmony.home


sealed class StateHome {
    data object Refreshing : StateHome()
    data object EndRefreshing : StateHome()
}