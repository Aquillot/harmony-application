package fr.harmony.database

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import fr.harmony.HarmonyApplication
import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(application: Application) {
    private val box: Box<HarmonizationSession> = HarmonyApplication.boxStore.boxFor()
    private val moshi = MoshiProvider.moshi
    private val context = application.applicationContext

    val refreshDatabase = MutableStateFlow(false)


    // Crée les adapters
    private val paletteAdapter: JsonAdapter<List<Int>> =
        moshi.adapter<List<Int>>(paletteType)
    private val weightsAdapter: JsonAdapter<List<List<Float>>> =
        moshi.adapter(weightsType)
    private val harmonizedAdapter: JsonAdapter<Map<String, List<Int>>> =
        moshi.adapter(harmonizedType)

    fun save(model: HarmonizationSessionModel) {
        val entity = HarmonizationSession().apply {
            originalPath    = model.originalPath
            width           = model.width
            height          = model.height
            widthPreview    = model.widthPreview
            heightPreview   = model.heightPreview
            previewPath      = model.previewPath
            paletteJson      = paletteAdapter.toJson(model.palette)
            weightsJson      = weightsAdapter.toJson(model.weights)
            harmonizedJson   = harmonizedAdapter.toJson(model.harmonizedPalette)
            selectedPattern  = model.selectedPattern
            slider          = model.slider
        }
        if (entity.width==0 || entity.height==0) {
            return
        }
        box.put(entity)
        Log.d("SessionRepository", "Saved session with ID: ${entity.id}")
        refreshDatabase.value = true
    }

    fun loadAll(): List<HarmonizationSessionModel> {
        return box.all.map { entity ->
            HarmonizationSessionModel(
                id = entity.id,
                originalPath = entity.originalPath,
                width = entity.width,
                height = entity.height,
                widthPreview = entity.widthPreview,
                heightPreview = entity.heightPreview,
                previewPath = entity.previewPath,
                palette = paletteAdapter.fromJson(entity.paletteJson) ?: emptyList(),
                weights = weightsAdapter.fromJson(entity.weightsJson) ?: emptyList(),
                harmonizedPalette = harmonizedAdapter.fromJson(entity.harmonizedJson) ?: emptyMap(),
                selectedPattern = entity.selectedPattern,
                slider = entity.slider
            )
        }
    }
    fun loadById(id: Long): HarmonizationSessionModel? {
        val entity = box.get(id) ?: return null
        return HarmonizationSessionModel(
            id = entity.id,
            originalPath = entity.originalPath,
            width = entity.width,
            height = entity.height,
            widthPreview = entity.widthPreview,
            heightPreview = entity.heightPreview,
            previewPath = entity.previewPath,
            palette = paletteAdapter.fromJson(entity.paletteJson) ?: emptyList(),
            weights = weightsAdapter.fromJson(entity.weightsJson) ?: emptyList(),
            harmonizedPalette = harmonizedAdapter.fromJson(entity.harmonizedJson) ?: emptyMap(),
            selectedPattern = entity.selectedPattern,
            slider = entity.slider
        )
    }

    fun updateSession(id: Long, newThumbnail : Bitmap, model: HarmonizationSessionModel) {
        val entity = box.get(id) ?: return
        println(entity.previewPath)
        val previewFile = File(entity.previewPath)
        previewFile.outputStream().use { out ->
            newThumbnail.compress(Bitmap.CompressFormat.JPEG, 80, out)
        }
        entity.slider = model.slider
        entity.paletteJson = paletteAdapter.toJson(model.palette)
        entity.selectedPattern = model.selectedPattern
        box.put(entity)
    }

    fun loadAllPreview(): List<HarmonizationSessionModelPreview> {
        return box.all.map { entity ->
            HarmonizationSessionModelPreview(
                id = entity.id,
                originalPath = entity.originalPath,
                previewPath = entity.previewPath,
                widthPreview = entity.widthPreview,
                heightPreview = entity.heightPreview
            )
        }
    }

    fun delete(id: Long) {
        box.remove(id)
    }

    suspend fun deleteSession(id: Long) {
        withContext(Dispatchers.IO) {
            val session = loadById(id) ?: return@withContext
            File(session.originalPath).takeIf(File::exists)?.delete()
            File(session.previewPath).takeIf(File::exists)?.delete()
            delete(id)
        }
    }

    fun loadPreviewPage(offset: Int, limit: Int): List<HarmonizationSessionModelPreview> {
        val allEntities: List<HarmonizationSession> = box.query()
            .orderDesc(HarmonizationSession_.id)
            .build()
            .find()

        return allEntities
            .drop(offset)
            .take(limit)
            .map { entity ->
                HarmonizationSessionModelPreview(
                    id           = entity.id,
                    originalPath  = entity.originalPath,
                    previewPath  = entity.previewPath,
                    widthPreview = entity.widthPreview,
                    heightPreview= entity.heightPreview
                )
            }
    }




}
