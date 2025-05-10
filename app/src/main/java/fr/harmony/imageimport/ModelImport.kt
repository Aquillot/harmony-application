package fr.harmony.imageimport

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class ModelImport @Inject constructor() : ViewModel() {

    // État : l'image actuellement sélectionnée (null si rien)
    private val _selectedImageUri = MutableStateFlow<Uri?>(null)

    // Événement unique : déclencher une navigation une fois l’image sélectionnée
    private val _navigation = MutableSharedFlow<NavigationEventImport>()
    val navigation: SharedFlow<NavigationEventImport> = _navigation.asSharedFlow()

    fun onIntent(intent: IntentImport) {
        when (intent) {
            is IntentImport.OpenGallery -> emitNavigation(NavigationEventImport.OpenGallery)
            is IntentImport.OpenCamera -> emitNavigation(NavigationEventImport.OpenCamera)
            is IntentImport.OpenDocument -> emitNavigation(NavigationEventImport.OpenDocument)
            is IntentImport.Selected -> handleImageSelected(intent.uri)
        }
    }

    private fun handleImageSelected(uri: Uri) {
        _selectedImageUri.value = uri
        // Navigation vers l'écran suivant
        emitNavigation(NavigationEventImport.NavigateHarmonize(uri))
    }

    private fun emitNavigation(event: NavigationEventImport) {
        viewModelScope.launch {
            _navigation.emit(event)
        }
    }

    /**
     * Copie une image depuis un URI vers un dossier temporaire et retourne l'URI du fichier temporaire.
     *
     * @param context Le contexte Android utilisé pour accéder au ContentResolver et au cache.
     * @param uri L'URI de l'image à copier.
     * @return L'URI du fichier temporaire créé, ou `null` en cas d'erreur.
     */
    fun copyImageToTemporaryFolder(context: Context, uri: Uri): Uri? {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")

        return try {
            val outputStream = FileOutputStream(tempFile)
            inputStream?.copyTo(outputStream)
            outputStream.close()

            Uri.fromFile(tempFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            inputStream?.close()
        }
    }

}