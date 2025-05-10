package fr.harmony.harmonize.data

import fr.harmony.harmonize.domain.HarmonizeRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import com.squareup.moshi.Moshi
import javax.inject.Inject
import retrofit2.HttpException
import java.io.IOException

class HarmonizeRepositoryImpl @Inject constructor(
    private val api: HarmonizeApi,
    private val moshi: Moshi
) : HarmonizeRepository {

    override suspend fun uploadImages(
        original: java.io.File,
        harmonized: java.io.File
    ): Result<HarmonizeResponse> {
        return try {
            val originalPart = original.asMultipart("original")
            val harmonizedPart = harmonized.asMultipart("harmonized")

            val response = api.uploadImages(originalPart, harmonizedPart)
            Result.success(response)

        } catch (e: HttpException) {
            val errorJson = e.response()?.errorBody()?.string().orEmpty()
            val errorResp = try {
                moshi.adapter(ErrorResponse::class.java).fromJson(errorJson)
            } catch (_: Exception) {
                ErrorResponse(error = "", error_code = "null")
            }

            val code = errorResp?.error_code
                .takeUnless { it == "null" }
                ?: ("uploadError" + e.code().toString())

            Result.failure(ApiErrorException(code))

        } catch (e: IOException) {
            Result.failure(ApiErrorException("NETWORK_ERROR"))
        } catch (e: Exception) {
            Result.failure(ApiErrorException("UNKNOWN_ERROR"))
        }
    }

    private fun java.io.File.asMultipart(name: String): MultipartBody.Part {
        val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), this)
        return MultipartBody.Part.createFormData(name, this.name, requestFile)
    }
}