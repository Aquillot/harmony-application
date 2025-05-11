package fr.harmony.profile.data

import android.util.Log
import com.squareup.moshi.Moshi
import fr.harmony.profile.domain.ProfileRepository
import javax.inject.Inject

// class qui permet de faire la connexion à l'API donc prends en paramètre ProfileApi
class ProfileRepositoryImpl @Inject constructor(
    private val api: ProfileApi,
    private val moshi: Moshi
) : ProfileRepository {

    override suspend fun getProfile(): Result<ProfileResponse> {
        return try {
            val resp = api.getProfile()
            Result.success(resp)
        } catch (e: retrofit2.HttpException) {
            // On essaie de parser le JSON d'erreur
            val errorJson = e.response()?.errorBody()?.string().orEmpty()

            // Au cas où le JSON n'est pas bien formé, on renvoie une erreur
            val errorResp = try {
                moshi.adapter(ErrorResponse::class.java).fromJson(errorJson)
            } catch (_: Exception) {
                ErrorResponse(error = "", error_code = "null")
            }

            // Si errorCode vaut "null", on prend le code HTTP, sinon on garde la valeur du JSON
            val code = errorResp?.error_code.takeUnless { it == "null" }?: ("errorProfile" + e.code().toString())
            Log.e("ProfileRepositoryImpl", "Error Code: $code")
            // On renvoie une exception avec le code d'erreur
            Result.failure(ApiErrorException(code))
        } catch (e: java.io.IOException) {
            // IOException est levée si le réseau est inaccessible
            Result.failure(ApiErrorException("NETWORK_ERROR"))
        } catch (e: Exception) {
            // Exception levée si le JSON n'est pas bien formé
            Log.e("ProfileRepositoryImpl", "Error: ${e.message}")
            Result.failure(ApiErrorException("UNKNOWN_ERROR"))
        }
    }

}