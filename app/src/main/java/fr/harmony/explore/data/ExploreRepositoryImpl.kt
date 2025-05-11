package fr.harmony.explore.data

import fr.harmony.explore.domain.ExploreRepository
import fr.harmony.harmonize.data.ApiErrorException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ExploreRepositoryImpl @Inject constructor(
    private val api: ExploreApi,
    private val moshi: com.squareup.moshi.Moshi
) : ExploreRepository {

    override suspend fun getImages(): Result<List<SharedImage>> {
        return try {
            val response = api.getImages()
            println("Response: $response")
            Result.success(response)
        } catch (e: Exception) {
            println("Error: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun vote(
        imageId: Int,
        isOriginalVote: Boolean
    ): Result<VoteResponse> {
        return try {
            val response = api.vote(imageId, RegisterVote(isOriginalVote))
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
            println("Error: ${e.message}")
            Result.failure(ApiErrorException("UNKNOWN_ERROR"))
        }
    }
}