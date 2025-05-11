package fr.harmony

import fr.harmony.explore.data.SharedImage

sealed class IntentExplore {
    data object LoadImages : IntentExplore()
    data class LikeImage(val isOriginalVote: Boolean) : IntentExplore()
    data class ShowSlider(val image: SharedImage) : IntentExplore()
    data object HideSlider : IntentExplore()
    data object NavigateToHome : IntentExplore()
}