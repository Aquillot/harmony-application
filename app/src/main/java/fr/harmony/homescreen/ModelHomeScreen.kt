package fr.harmony.homescreen

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmony.api.TokenManager
import fr.harmony.database.HarmonizationSessionModelPreview
import fr.harmony.database.SessionRepository
import fr.harmony.imageimport.IntentImport
import fr.harmony.login.domain.LoginUseCase
import fr.harmony.login.mvi.ActionLogin
import fr.harmony.login.mvi.StateLogin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.Thread.sleep
import javax.inject.Inject

data class GalleryImage(val id :Long,val originalUri :Uri, val uri: Uri, val span: Int, val aspectRatio: Float, val height: Int = 0)


@HiltViewModel
class ModelHomeScreen @Inject constructor(
    application: Application,
    private val repo: SessionRepository
) : ViewModel() {


    private val _state = MutableStateFlow<StateHomeScreen>(StateHomeScreen.EndRefreshing)
    val state : StateFlow<StateHomeScreen> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // On observe les actions émises par la logique métier, on les réduit et on les émets dans le flux d'état
            repo.refreshDatabase.collect { refresh ->
                if (refresh) {
                    // On recharge la base de données
                    Log.d("ModelHomeScreen", "Refreshing database")
                    repo.refreshDatabase.value = false
                    _state.value = StateHomeScreen.Refreshing
                    println("Refreshing database état : ${_state.value}")
                }
            }
        }
    }

    fun onIntent(intent: IntentHomeScreen) {
        println("Intent reçu : $intent")
        when (intent) {
            is IntentHomeScreen.DeleteImage -> deleteImage(intent.id)
            is IntentHomeScreen.EndRefresh -> { _state.value = StateHomeScreen.EndRefreshing }
        }
    }


    private fun deleteImage(id: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                println("Deleting image: $id")
                repo.deleteSession(id)
            }
            _state.value = StateHomeScreen.Refreshing
            Log.d("ModelHomeScreen", "Image deleted: $id")
        }
    }

    // Application context for FileProvider
    private val context: Context = application.applicationContext

    val pager: Flow<PagingData<GalleryImage>> =
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    prefetchDistance = 5, // Nombre d'éléments à précharger avant la position actuelle
                    maxSize = 50, // Taille maximale de la mémoire cache
                    enablePlaceholders = false
                ),
                pagingSourceFactory = { SessionPagingSource(repo) }
            ).flow
                .map { pagingData ->
                    pagingData.map { session ->
                        // Convert each preview model to GalleryImage lazily
                        val file = File(session.previewPath)
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            file
                        )
                        val ar = session.widthPreview.toFloat() / session.heightPreview
                        val span = if (ar > 1.6f) 2 else 1
                        GalleryImage(session.id, session.originalPath.toUri(), uri, span, ar)
                    }
                }
                .cachedIn(viewModelScope)
        }