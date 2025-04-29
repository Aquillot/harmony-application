package fr.harmony.login.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmony.login.data.ApiErrorException
import fr.harmony.login.domain.LoginUseCase
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
class ModelLogin @Inject constructor (
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    // Flux d'Actions émises par la logique métier
    private val _actions = MutableSharedFlow<ActionLogin>(replay = 0)   // MutableSharedFlow est un flux qui permet d'émettre des valeurs à plusieurs abonnés
                                                                        // replay = 0 signifie que le flux ne garde pas en mémoire les valeurs émises
    val actions: SharedFlow<ActionLogin> = _actions.asSharedFlow()      // asSharedFlow() permet de rendre le flux immuable pour les abonnés
                                                                        // en d'autre terme de le rendre "read-only"

    // Flux d'Etat émis par la logique métier
    private val _state = MutableStateFlow<StateLogin>(StateLogin.Initial)   // MutableStateFlow est un flux qui permet de garder en mémoire la dernière valeur émise
                                                                            // et de l'émettre à tous les abonnés
    val state: StateFlow<StateLogin> = _state.asStateFlow()                 // asStateFlow() permet de rendre le flux immuable pour les abonnés
                                                                            // en d'autre terme de le rendre "read-only"

    init {
        // On observe les actions pour produire un nouvel Etat
        // viewModelScope est un scope de coroutine qui est lié au cycle de vie du ViewModel
        // et qui permet de lancer des coroutines qui seront annulées lorsque le ViewModel est détruit (ici quand l'utilisateur quitte l'écran de login)
        viewModelScope.launch {
            // On observe les actions émises par la logique métier, on les réduit et on les émets dans le flux d'état
            actions.collect { action ->
                reduce(action)
            }
        }
    }

    // Gestion des Intentions de l'utilisateur
    fun handleIntent(intent: IntentLogin) {
        // On va gérer les intentions de l'utilisateur
        when (intent) {
            // Quand l'utilisateur veut se connecter, on va appeler la méthode login
            is IntentLogin.Login -> doLogin(intent.email, intent.password)
        }
    }

    // Appel métier et émission d'Actions
    private fun doLogin(email: String, password: String) {
        // On va lancer une coroutine pour effectuer l'appel réseau
        viewModelScope.launch {
            // On va émettre une action de chargement pour indiquer que l'on est en train de se connecter
            _actions.emit(ActionLogin.Loading)
            val result = loginUseCase.login(email, password)
            if (result.isSuccess) {
                val loginResult = result.getOrThrow()
                _actions.emit(
                    ActionLogin.Success(
                        token = loginResult.token,
                        userId = loginResult.user_id
                    )
                )
                println("Token: ${loginResult.token}, User ID: ${loginResult.user_id}")
            } else {
                val code = (result.exceptionOrNull() as? ApiErrorException)?.errorCode ?: "UNKNOWN_ERROR"
                _actions.emit(ActionLogin.Failure(code))
            }
        }
    }

    // Reduction: Action + ancien Etat -> nouvel Etat
    private fun reduce(action: ActionLogin) {
        // On va réduire l'action en un nouvel état
        val newState = when (action) {
            is ActionLogin.Loading -> StateLogin.Loading // On est en train de se connecter
            is ActionLogin.Success -> StateLogin.Success(action.token) // On est connecté
            is ActionLogin.Failure -> StateLogin.Error(action.errorCode) // On a échoué
        }
        // On va émettre le nouvel état dans le flux d'état
        _state.value = newState
    }
}
