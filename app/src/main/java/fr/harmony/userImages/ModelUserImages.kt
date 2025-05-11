package fr.harmony.userImages

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmony.R
import fr.harmony.userImages.data.SharedImage
import fr.harmony.userImages.domain.UserImagesRepository
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
class ModelUserImages @Inject constructor(
    application: Application,
    private val repository: UserImagesRepository
) : ViewModel() {
    private val context = application

    // StateFlow pour exposer le state
    private val _state = MutableStateFlow(StateUserImages())
    val state: StateFlow<StateUserImages> = _state.asStateFlow()

    // SharedFlow pour émettre des events ponctuels
    private val _events = MutableSharedFlow<EventUserImages>()
    val events: SharedFlow<EventUserImages> = _events.asSharedFlow()

    // SharedFlow pour émettre des événements de navigation
    private val _navigation = MutableSharedFlow<NavigationEventUserImages>()
    val navigation: SharedFlow<NavigationEventUserImages> = _navigation.asSharedFlow()

    fun handleIntent(intent: IntentUserImages) {
        when (intent) {
            is IntentUserImages.LoadImages -> loadImages()
            is IntentUserImages.ShowPopup -> updatePopupImage(intent.image).also {
                updatePopupVisibility(true)
            }
            is IntentUserImages.HidePopup -> updatePopupVisibility(false)
            is IntentUserImages.DeleteImage -> deleteImage()
            is IntentUserImages.NavigateToExplore -> {
                viewModelScope.launch {
                    _navigation.emit(NavigationEventUserImages.NavigateToExplore)
                }
            }
        }
    }

    private fun loadImages() {
        viewModelScope.launch {
            // Upload des images
            val result = repository.getUserImages()

            result.fold(
                onSuccess = { response ->
                    _state.update { it.copy(images = response) }
                },
                onFailure = { exception ->
                    _state.update { it.copy(images = emptyList(), error = exception.message) }
                    _events.emit(EventUserImages.ShowError(context.getString(R.string.EXPLORE_IMAGE_ERRORED)))
                    _navigation.emit(NavigationEventUserImages.NavigateToExplore)
                }
            )
        }
    }

    private fun deleteImage() {
        viewModelScope.launch {
            val imageId = state.value.popupImage?.image_id
            if (imageId == null) {
                _events.emit(EventUserImages.ShowError(context.getString(R.string.DELETE_IMAGE_ERRORED)))
                return@launch
            }

            // Suppression de l'image
            val result = repository.delete(imageId)
            result.fold(
                onSuccess = {
                    loadImages()
                    updatePopupVisibility(false)
                },
                onFailure = { exception ->
                    _state.update { it.copy(error = exception.message) }
                    _events.emit(EventUserImages.ShowError(context.getString(R.string.DELETE_IMAGE_ERRORED)))
                }
            )
        }
    }

    private fun updatePopupVisibility(visible: Boolean) {
        _state.update { it.copy(popupVisible = visible) }
    }

    private fun updatePopupImage(image: SharedImage) {
        _state.update { it.copy(popupImage = image) }
    }
}