package fr.harmony.harmonize

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmony.R
import fr.harmony.database.HarmonizationSessionModel
import fr.harmony.database.SessionRepository
import fr.harmony.harmonize.data.ApiErrorException
import fr.harmony.harmonize.domain.HarmonizeRepository
import fr.harmony.socket.SocketManager
import io.socket.engineio.parser.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject
import androidx.core.net.toUri

enum class HarmonizeStep {
    SEND_IMAGE,
    COMPUTE_PALETTE,
    SIMPLIFY_PALETTE,
    IMAGE_SEGMENTATION,
    RECEIVE_LAYERS,
    PALETTE_HARMONIZATION,
    DONE
}

@HiltViewModel
class ModelHarmonize @Inject constructor(
    application: Application,
    private val socketManager: SocketManager,
    private val sessionRepository: SessionRepository,
    private val harmonizeRepository: HarmonizeRepository
) : ViewModel() {
    private val contentResolver = application.contentResolver
    private val context = application

    // StateFlow pour exposer le state
    private val _state = MutableStateFlow(StateHarmonize())
    val state: StateFlow<StateHarmonize> = _state.asStateFlow()

    // SharedFlow pour émettre des events ponctuels
    private val _events = MutableSharedFlow<EventHarmonize>()
    val events: SharedFlow<EventHarmonize> = _events.asSharedFlow()

    // SharedFlow pour émettre des événements de navigation
    private val _navigation = MutableSharedFlow<NavigationEventHarmonize>()
    val navigation: SharedFlow<NavigationEventHarmonize> = _navigation.asSharedFlow()


    fun updateSessionOnDataBase(id: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                val originalBitmap = _state.value.imageBitmap
                val targetWidth = 200
                val aspectRatio = originalBitmap.height.toFloat() / originalBitmap.width
                val targetHeight = (targetWidth * aspectRatio).toInt()
                val newThumbnail = Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)
                val session = HarmonizationSessionModel(
                    palette = _state.value.palette,
                    harmonizedPalette = _state.value.harmonizedPalette,
                    selectedPattern = _state.value.selectedPattern,
                )
                sessionRepository.updateSession(id,newThumbnail,session)
            }
        }
    }

    fun saveSessionOnDataBase(){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                val sourceUri = _state.value.imageUri
                val fileName = Regex("temp_image_(\\d+\\.jpg)$").find(sourceUri.toString())?.groupValues?.get(1) ?: "default.jpg"
                val destinationFile = File(context.filesDir, fileName)
                context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                    destinationFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }


                val originalBitmap = _state.value.imageBitmap
                val targetWidth = 200
                val aspectRatio = originalBitmap.height.toFloat() / originalBitmap.width
                val targetHeight = (targetWidth * aspectRatio).toInt()
                val thumbnail = Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)
                val previewFilename = "thumb_${fileName}"
                val previewFile = File(context.filesDir,  previewFilename)
                previewFile.outputStream().use { out ->
                    thumbnail.compress(Bitmap.CompressFormat.JPEG, 80, out)
                }

                val session = HarmonizationSessionModel(
                    width = _state.value.imageWidth,
                    height = _state.value.imageHeight,
                    widthPreview = thumbnail.width,
                    heightPreview = thumbnail.height,
                    originalPath = destinationFile.absolutePath,
                    previewPath = previewFile.absolutePath,
                    palette = _state.value.palette,
                    weights = _state.value.weights,
                    harmonizedPalette = _state.value.harmonizedPalette,
                    selectedPattern = _state.value.selectedPattern,
                )
                sessionRepository.save(session)
            }
        }
    }

    fun cleanup() {
        println("Harmonisation : Clean des ressources")

        // « off » pour chacun des events
        socketManager.off("message")
        socketManager.off("layer_weights")
        socketManager.off("server_response")
        socketManager.off("error")
        socketManager.off("convex_hull")
        socketManager.off("thinking")
        socketManager.off("harmonized")
        socketManager.off(io.socket.client.Socket.EVENT_DISCONNECT)
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
    }

    /** Initialise avec l’URI que le caller doit passer au ViewModel */
    fun initWith(uri: Uri) {
        _state.value = StateHarmonize(imageUri = uri, step = HarmonizeStep.SEND_IMAGE)

        viewModelScope.launch {
            val result = socketManager.connect()
            println("Harmonisation : Résultat de la connexion : $result")
            if (!result) {
                viewModelScope.launch {
                    _events.emit(EventHarmonize.ShowSnackbar(context.getString(R.string.SOCKET_CONNECTION_ERROR)))
                    _navigation.emit(NavigationEventHarmonize.NavigateToImport)
                }
                return@launch
            }

            // Si la connexion est réussie, on envoie l'image au serveur
            sendImageToSocket()

            // On écoute les événements du socket
            socketManager.on("layer_weights", ::handleImageLayers)
            socketManager.on("error", ::handleErrorMessage)
            socketManager.on("convex_hull", ::handleConvexHull)
            socketManager.on("harmonized", ::handleHarmonizedPalette)
            socketManager.on(io.socket.client.Socket.EVENT_DISCONNECT) {
                println("Harmonisation : Socket déconnecté: ${it.joinToString()}")
                viewModelScope.launch {
                    _events.emit(EventHarmonize.ShowSnackbar(context.getString(R.string.SOCKET_DISCONNECTED)))
                    _navigation.emit(NavigationEventHarmonize.NavigateToImport)
                }
            }
        }
    }

    fun initFromDataBase(id : Long) {
        viewModelScope.launch {
            val session = sessionRepository.loadById(id)
            if (session != null) {
                reconstructImageThanksHarmonizationSession(session)
            } else {
                _events.emit(EventHarmonize.ShowSnackbar(context.getString(R.string.SESSION_NOT_FOUND)))
                _navigation.emit(NavigationEventHarmonize.NavigateToImport)
            }
        }
    }

    /** Gestion des intentions */
    fun onIntent(intent: IntentHarmonize) {
        when (intent) {
            IntentHarmonize.ToggleShareMenu -> toggleShareMenu()
            is IntentHarmonize.SliderChanged -> changeSlider(intent.value)
            is IntentHarmonize.PatternSelected -> selectPattern(intent.pattern)
            IntentHarmonize.ShareAppClicked    -> shareApp()
            IntentHarmonize.ShareExternalClicked -> shareExternal()
            IntentHarmonize.DownloadImage -> onSaveImageClicked()
        }
    }

    private fun toggleShareMenu() {
        _state.update { it.copy(shareVisible = !it.shareVisible) }
    }

    private fun changeSlider(value: Float) {
        _state.update { it.copy(sliderPosition = value) }
        generateReconstructedImage()
    }

    private fun selectPattern(pattern: String) {
        _state.update { it.copy(selectedPattern = pattern) }
        generateReconstructedImage()
    }

    fun onShareOffsetChanged(offset: Int) {
        _state.update { it.copy(shareOffset = offset) }
    }

    private fun onSaveImageClicked() {
        val bmp = _state.value.imageBitmap ?: return
        viewModelScope.launch {
            _events.emit(EventHarmonize.RequestSaveImage(bmp))
        }
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.cacheDir, "original_image.jpg")
            file.outputStream().use { output ->
                val image = BitmapFactory.decodeStream(inputStream)
                image.compress(Bitmap.CompressFormat.JPEG, 80, output)
            }
            file
        } catch (e: Exception) {
            null
        }
    }

    private fun shareApp() {
        _state.update { it.copy(sharingState = "loading") }

        viewModelScope.launch {
            try {
                val uri = _state.value.imageUri
                val bitmap = _state.value.imageBitmap

                // On convertit l'Uri en File (image originale) et on sauvegarde le Bitmap dans un fichier temporaire
                val originalFile = uriToFile(uri) ?: throw Exception("ORIGINAL_FILE_NULL")
                val harmonizedFile = File(context.cacheDir, "harmonized_image.jpg").apply {
                    outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 80, it) }
                }

                // Upload des images
                val result = harmonizeRepository.uploadImages(
                    original = originalFile,
                    harmonized = harmonizedFile
                )

                result.fold(
                    onSuccess = {
                        _state.update { it.copy(sharingState = "done") }
                    },
                    onFailure = { it ->
                        // En cas d'erreur, on affiche un message d'erreur et on reset l'état
                        Log.e("shareApp", "Failed to upload images: ${it.message}")
                        _state.update { it.copy(sharingState = "none") }
                        _events.emit(EventHarmonize.ShowSnackbar(context.getString(R.string.SHARE_ERROR)))
                    }
                )

                // On supprime les fichiers temporaires
                if (!originalFile.delete()) Log.e("shareApp", "Failed to delete the original image from cache.")
                if (!harmonizedFile.delete()) Log.e("shareApp", "Failed to delete the harmonized image from cache.")
            } catch (e: Exception) {
                _state.update { it.copy(sharingState = "EXCEPTION") }
            }
        }
    }

    private fun shareExternal() {
        viewModelScope.launch {
            val bitmap = _state.value.imageBitmap
            val file = File(context.cacheDir, "shared_image.png").apply {
                outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            }

            // On crée un URI pour le fichier temporaire
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            _events.emit(EventHarmonize.LaunchShareIntent(intent))
        }
    }

    private fun parseVerticesToColorList(verticesJson: JSONArray): List<Int> {
        val colorList = mutableListOf<Int>()

        for (i in 0 until verticesJson.length()) {
            val colorArray = verticesJson.getJSONArray(i)

            val rRaw = colorArray.getDouble(0)
            val gRaw = colorArray.getDouble(1)
            val bRaw = colorArray.getDouble(2)

            // Détection automatique : si toutes les valeurs sont ≤ 1.0, on suppose qu'elles sont normalisées
            val isNormalized = rRaw <= 1.0 && gRaw <= 1.0 && bRaw <= 1.0

            val r = if (isNormalized) (rRaw * 255).toInt() else rRaw.toInt()
            val g = if (isNormalized) (gRaw * 255).toInt() else gRaw.toInt()
            val b = if (isNormalized) (bRaw * 255).toInt() else bRaw.toInt()

            val color = Color.rgb(r.coerceIn(0, 255), g.coerceIn(0, 255), b.coerceIn(0, 255))
            colorList.add(color)
        }

        return colorList
    }

    private fun handleConvexHull(args: Array<Any>) {
        val json = args.firstOrNull() as? JSONObject ?: return
        val vertices = json.getJSONArray("vertices")
        val couleurs = parseVerticesToColorList(vertices)

        _state.update { st ->
            val nextStep = when (st.step) {
                HarmonizeStep.COMPUTE_PALETTE -> HarmonizeStep.SIMPLIFY_PALETTE
                else -> HarmonizeStep.IMAGE_SEGMENTATION
            }
            st.copy(
                step = nextStep,
                palette = couleurs
            )
        }

        if (_state.value.step == HarmonizeStep.IMAGE_SEGMENTATION) {
            socketManager.emit("harmonize", JSONObject().apply {
                put("palette", vertices)
            })
        }
    }

    fun reconstructImageThanksHarmonizationSession(
        session: HarmonizationSessionModel
    ) {
        _state .update {
            it.copy(
                imageUri = session.originalPath.toUri(),
                imageWidth = session.width,
                imageHeight = session.height,
                palette = session.palette,
                weights = session.weights,
                harmonizedPalette = session.harmonizedPalette,
                selectedPattern = session.selectedPattern,
                step = HarmonizeStep.DONE,

            )
        }
        generateReconstructedImage()
    }

    private fun handleImageLayers(args: Array<Any>) {
        val json = args.firstOrNull() as? JSONObject ?: return
        val layerWeightsJson = json.getJSONArray("weights")
        val layerWeights = List(layerWeightsJson.length()) { i ->
            layerWeightsJson.getDouble(i).toFloat()
        }
        val width = json.getInt("width")
        val height = json.getInt("height")

        _state.update { st ->
            st.copy(
                imageWidth = width,
                imageHeight = height,
                step = HarmonizeStep.RECEIVE_LAYERS,
                weights = st.weights + listOf(layerWeights)
            )
        }

        if (_state.value.palette.size == _state.value.weights.size && _state.value.harmonizedPalette.isNotEmpty()) {
            _state.update { it.copy(step = HarmonizeStep.DONE) }
            generateReconstructedImage()
        }

        println("Harmonisation : Couche ${json.getInt("id")} ajoutée, total couches = ${_state.value.weights.size} / ${_state.value.palette.size}")
    }

    private fun handleHarmonizedPalette(args: Array<Any>) {
        if (_state.value.palette.size == _state.value.weights.size) {
            _state.update { it.copy(step = HarmonizeStep.DONE) }
            generateReconstructedImage()
        }
        val json = args.firstOrNull() as? JSONObject ?: return
        println("Harmonisation : Palette harmonisée reçue")

        // Pour chaque clé de l'objet JSON, on crée une liste de couleurs
        json.keys().forEach { key ->
            val entry = json.getJSONObject(key.toString())
            val paletteArray = entry.getJSONArray("palette")

            val couleurs = parseVerticesToColorList(paletteArray)
            _state.update { st ->
                st.copy(
                    harmonizedPalette = st.harmonizedPalette + (key.toString() to couleurs)
                )
            }
        }
    }

    private fun handleErrorMessage(args: Array<Any>) {
        println("Harmonisation : Erreur du socket : ${args.joinToString()}")
        viewModelScope.launch {
            _events.emit(EventHarmonize.ShowSnackbar(context.getString(R.string.SOCKET_ERROR)))
            _navigation.emit(NavigationEventHarmonize.NavigateToImport)
        }
    }

    /**
     * Envoie l'image actuelle au serveur via un socket.
     *
     */
    private fun sendImageToSocket() {
        // On convertit l'image en base64
        val imageUri = _state.value.imageUri
        val base64Image = convertImageToBase64(imageUri)

        // Si l'image est null, on affiche un message d'erreur et on retourne à l'écran d'import
        if (base64Image == null) {
            viewModelScope.launch {
                _events.emit(EventHarmonize.ShowSnackbar(context.getString(R.string.IMAGE_CONVERSION_ERROR)))
                _navigation.emit(NavigationEventHarmonize.NavigateToImport)
            }
            return
        }

        // On envoie l'image au socket
        val data = mapOf("image_data" to base64Image)

        // On stringifie le JSON
        val jsonString = JSONObject(data)
        socketManager.emit("upload_image", jsonString)
        _state.update { it.copy(step = HarmonizeStep.COMPUTE_PALETTE) }
    }

    /**
     * Convertit une image en une chaîne encodée en Base64.
     *
     * @param uri L'URI de l'image à convertir.
     * @return Une chaîne encodée en Base64 représentant l'image, ou `null` en cas d'erreur.
     *
     * En cas d'erreur, la fonction retourne `null` et affiche la pile d'exceptions.
     */
    private fun convertImageToBase64(uri: Uri?): String? {
        if (uri == null) return null

        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val buffer = ByteArrayOutputStream()
                val data = ByteArray(1024)

                var bytesRead: Int
                while (inputStream.read(data).also { bytesRead = it } != -1) {
                    buffer.write(data, 0, bytesRead)
                }

                val imageBytes = buffer.toByteArray()
                "data:image/png;base64," + Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Génère une image reconstruite à partir des couches et de la palette harmonisée.
     *
     * Cette fonction effectue les étapes suivantes :
     * 1. Convertit les couches de poids en matrices 2D.
     * 2. Construit une palette harmonisée en fonction du motif sélectionné et du ratio d'harmonisation.
     * 3. Reconstruit un bitmap à partir de la palette et des matrices de poids.
     * 4. Met à jour l'état avec le bitmap généré.
     */
    private fun generateReconstructedImage() {
        val st = _state.value
        val w = st.imageWidth
        val h = st.imageHeight

        // 1) Convertir chaque couche de List<Float> en Array<FloatArray>
        val matrices = st.weights.map { linearWeights ->
            linearWeights.toMatrix(w, h)
        }

        // 2) On construit la palette harmonisée (avec ratio)
        var palette = st.palette
        if (_state.value.selectedPattern.isNotEmpty() && st.harmonizedPalette[_state.value.selectedPattern] != null) {
            st.harmonizedPalette[_state.value.selectedPattern]?.let { palette = it }

            // On modifie la force de l'harmonisation (mélange palette originale et harmonisée)
            val strength = st.sliderPosition
            val originalPalette = st.palette
            palette = originalPalette.mapIndexed { index, color ->
                val harmonizedColor = st.harmonizedPalette[_state.value.selectedPattern]!![index]
                blendColors(color, harmonizedColor, strength)
            }
        }

        // 3) On reconstruit le bitmap
        val resultBmp = reconstructBitmap(
            palette = palette,
            layerWeights = matrices,
            width = w,
            height = h
        )

        // 3) Mettre à jour le state
        _state.update { it.copy(imageBitmap = resultBmp) }
    }
}