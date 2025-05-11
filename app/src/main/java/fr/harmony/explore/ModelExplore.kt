package fr.harmony.explore

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmony.IntentExplore
import fr.harmony.R
import fr.harmony.explore.data.SharedImage
import fr.harmony.explore.domain.ExploreRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModelExplore @Inject constructor(
    application: Application,
    private val repository: ExploreRepository
) : ViewModel() {
    private val context = application

    // StateFlow pour exposer le state
    private val _state = MutableStateFlow(StateExplore())
    val state: StateFlow<StateExplore> = _state.asStateFlow()

    // SharedFlow pour émettre des events ponctuels
    private val _events = MutableSharedFlow<EventExplore>()
    val events: SharedFlow<EventExplore> = _events.asSharedFlow()

    // SharedFlow pour émettre des événements de navigation
    private val _navigation = MutableSharedFlow<NavigationEventExplore>()
    val navigation: SharedFlow<NavigationEventExplore> = _navigation.asSharedFlow()

    fun handleIntent(intent: IntentExplore) {
        when (intent) {
            is IntentExplore.LoadImages -> loadImages()
            is IntentExplore.LikeImage -> likeImage(intent.isOriginalVote)
            is IntentExplore.ShowSlider -> updateSliderImages(intent.image).also {
                updateSliderVisibility(true)
            }
            is IntentExplore.HideSlider -> updateSliderVisibility(false)
            is IntentExplore.NavigateToHome -> {
                viewModelScope.launch {
                    _navigation.emit(NavigationEventExplore.NavigateToHome)
                }
            }
        }
    }

    private fun loadImages() {
        viewModelScope.launch {
            // Upload des images
            val result = repository.getImages()
            println("Result: $result")

            result.fold(
                onSuccess = { response ->
                    _state.update { it.copy(images = response) }
                },
                onFailure = { exception ->
                    _state.update { it.copy(images = emptyList(), error = exception.message) }
                    _events.emit(EventExplore.ShowError(context.getString(R.string.EXPLORE_IMAGE_ERRORED)))
                    _navigation.emit(NavigationEventExplore.NavigateToHome)
                }
            )
        }
    }

    private fun likeImage(isOriginalVote: Boolean) {
        viewModelScope.launch {
            // On trouve l'image qui correspond à l'URL
            if (state.value.sliderImage != null) {
                val result = repository.vote(state.value.sliderImage!!.image_id, isOriginalVote)
                if (result.isSuccess) {
                    // On crée une nouvelle image avec le vote mis à jour
                    val updatedImage = state.value.sliderImage!!.copy(
                        user_vote = when {
                            isOriginalVote -> "original"  // Vote pour l'original
                            else -> "harmonized"           // Vote pour l'harmonisé
                        }
                    )

                    // Mettre à jour l'état avec la nouvelle image
                    _state.value = _state.value.copy(
                        images = _state.value.images.map { if (it == state.value.sliderImage) updatedImage else it }
                    )
                } else {
                    // En cas d'erreur, on émet un événement pour afficher un message d'erreur
                    _events.emit(EventExplore.ShowError(context.getString(R.string.EXPLORE_VOTE_ERRORED)))
                }
            }
        }
    }

    private fun updateSliderVisibility(visible: Boolean) {
        _state.update { it.copy(sliderVisible = visible) }
    }

    private fun updateSliderImages(image: SharedImage) {
        _state.update { it.copy(sliderImage = image) }
    }
}