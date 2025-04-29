package fr.harmony.register.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmony.register.domain.RegisterUseCase
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
class ModelRegister @Inject constructor (
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    // Flux d'Actions émises par la logique métier
    private val _actions = MutableSharedFlow<ActionRegister>(replay = 0)
    val actions: SharedFlow<ActionRegister> = _actions.asSharedFlow()

    // Flux d'Etat émis par la logique métier
    private val _state = MutableStateFlow<StateRegister>(StateRegister.Initial)
    val state: StateFlow<StateRegister> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // On observe les actions émises par la logique métier, on les réduit et on les émets dans le flux d'état
            actions.collect { action ->
                reduce(action)
            }
        }
    }

    // Gestion des Intentions de l'utilisateur
    fun handleIntent(intent: IntentRegister) {
        // On va gérer les intentions de l'utilisateur
        when (intent) {
            // Quand l'utilisateur veut se connecter, on va appeler la méthode login
            is IntentRegister.Register -> {
                doRegister(intent.email, intent.username, intent.password)
            }
        }
    }
    private fun doRegister(email: String, username: String, password: String) {
        // On va lancer une coroutine pour effectuer l'appel réseau
        viewModelScope.launch {
            // On va émettre une action de chargement pour indiquer que l'on est en train de se connecter
            _actions.emit(ActionRegister.Loading)
            val result = registerUseCase.register(email, username, password)
            if (result.isSuccess) {
                val registerResult = result.getOrThrow()
                // Pour l'instant on va faire un login automatique après l'inscription
                //_actions.emit(ActionRegister.Success( token = registerResult.token, userId = registerResult.user_id))
                doLogin(email, password)
            } else {
                _actions.emit(ActionRegister.Failure(result.exceptionOrNull()?.message ?: "Erreur réseau"))
            }
        }
    }


    // Appel métier et émission d'Actions
    private fun doLogin(email: String, password: String) {
        // On va lancer une coroutine pour effectuer l'appel réseau
        viewModelScope.launch {
            // On va émettre une action de chargement pour indiquer que l'on est en train de se connecter
            _actions.emit(ActionRegister.Loading)
            val result = registerUseCase.login(email, password)
            if (result.isSuccess) {
                val loginResult = result.getOrThrow()
                _actions.emit(
                    ActionRegister.Success(
                        token = loginResult.token,
                        userId = loginResult.user_id
                    )
                )
                println("Token: ${loginResult.token}, User ID: ${loginResult.user_id}")
            } else {
                _actions.emit(ActionRegister.Failure(result.exceptionOrNull()?.message ?: "Erreur réseau"))
            }
        }
    }

    // Reduction: Action + ancien Etat -> nouvel Etat
    private fun reduce(action: ActionRegister) {
        // On va réduire l'action en un nouvel état
        val newState = when (action) {
            is ActionRegister.Loading -> StateRegister.Loading // On est en train de créer un compte
            is ActionRegister.Success -> StateRegister.Success(action.token) // On est connecté
            is ActionRegister.Failure -> StateRegister.Error(action.error) // On a échoué
        }
        // On va émettre le nouvel état dans le flux d'état
        _state.value = newState
    }
}
