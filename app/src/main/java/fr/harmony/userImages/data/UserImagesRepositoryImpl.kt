package fr.harmony.userImages.data

import android.util.Log
import fr.harmony.userImages.domain.UserImagesRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class UserImagesRepositoryImpl @Inject constructor(
    private val api: UserImagesApi,
    private val moshi: com.squareup.moshi.Moshi
) : UserImagesRepository {

    override suspend fun getUserImages(): Result<List<SharedImage>> {
        return try {
            val response = api.getUserImages()
            Result.success(response)
        } catch (e: Exception) {
            Log.e("UserImagesRepositoryImpl", "Error: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun delete(
        imageId: Int
    ): Result<DeleteResponse> {
        return try {
            val response = api.delete(imageId)
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
            Log.e("UserImagesRepositoryImpl", "Error: ${e.message}")
            Result.failure(ApiErrorException("UNKNOWN_ERROR"))
        }
    }
}