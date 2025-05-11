package fr.harmony.profile.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmony.api.TokenManager
import fr.harmony.profile.data.ApiErrorException
import fr.harmony.profile.domain.ProfileUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// HiltViewModel est une annotation de Dagger Hilt qui permet d'injecter des dépendances dans un ViewModel
// Hilt est une bibliothèque de Dagger qui simplifie l'injection de dépendances dans les applications Android
// Inject est une annotation de Dagger qui permet d'injecter des dépendances dans une classe
@HiltViewModel
class ModelProfile @Inject constructor (
    private val profileUseCase: ProfileUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    // Flux d'Actions émises par la logique métier
    private val _actions = MutableSharedFlow<ActionProfile>(replay = 0)   // MutableSharedFlow est un flux qui permet d'émettre des valeurs à plusieurs abonnés
                                                                        // replay = 0 signifie que le flux ne garde pas en mémoire les valeurs émises
    val actions: SharedFlow<ActionProfile> = _actions.asSharedFlow()      // asSharedFlow() permet de rendre le flux immuable pour les abonnés
                                                                        // en d'autre terme de le rendre "read-only"

    // Flux d'Etat émis par la logique métier
    private val _state = MutableStateFlow<StateProfile>(StateProfile.Initial)   // MutableStateFlow est un flux qui permet de garder en mémoire la dernière valeur émise
                                                                            // et de l'émettre à tous les abonnés
    val state: StateFlow<StateProfile> = _state.asStateFlow()                 // asStateFlow() permet de rendre le flux immuable pour les abonnés
                                                                            // en d'autre terme de le rendre "read-only"

    init {
        // On observe les actions pour produire un nouvel Etat
        // viewModelScope est un scope de coroutine qui est lié au cycle de vie du ViewModel
        // et qui permet de lancer des coroutines qui seront annulées lorsque le ViewModel est détruit (ici quand l'utilisateur quitte l'écran de profile)
        viewModelScope.launch {
            // On observe les actions émises par la logique métier, on les réduit et on les émets dans le flux d'état
            actions.collect { action ->
                reduce(action)
            }
        }
    }

    // Gestion des Intentions de l'utilisateur
    fun handleIntent(intent: IntentProfile) {
        // On va gérer les intentions de l'utilisateur
        when (intent) {
            // Quand l'utilisateur veut se connecter, on va appeler la méthode profile
            is IntentProfile.GetProfile -> getProfile()
        }
    }

    private fun getProfile() {
        // On va lancer une coroutine pour effectuer l'appel réseau

        viewModelScope.launch {
            // On va émettre une action de chargement pour indiquer que l'on est en train de se connecter
            _actions.emit(ActionProfile.Loading)
            if (tokenManager.getTokenSync() == null) {
                _actions.emit(ActionProfile.Failure("NO_TOKEN"))
            }
            else{
                val result = profileUseCase.getProfile()
                if (result.isSuccess) {
                    val profileResult = result.getOrThrow()
                    _actions.emit(
                        ActionProfile.Success(
                            id = profileResult.id,
                            email = profileResult.email,
                            username = profileResult.username,
                        )
                    )
                } else {
                    print("Error: ${(result.exceptionOrNull() as? ApiErrorException)?.errorCode}")
                    val code = (result.exceptionOrNull() as? ApiErrorException)?.errorCode ?: "UNKNOWN_ERROR"
                    _actions.emit(ActionProfile.Failure(code))
                }
            }
        }
    }

    // Reduction: Action + ancien Etat -> nouvel Etat
    private fun reduce(action: ActionProfile) {
        // On va réduire l'action en un nouvel état
        val newState = when (action) {
            is ActionProfile.Loading -> StateProfile.Loading // On est en train de se connecter
            is ActionProfile.Success -> StateProfile.Success(
                action.email,
                action.username,
                action.id
            ) // On est connecté
            is ActionProfile.Failure -> StateProfile.Error(action.errorCode) // On a échoué
        }
        // On va émettre le nouvel état dans le flux d'état
        _state.value = newState
    }
}
