package fr.harmony.register.data

import fr.harmony.register.domain.RegisterRepository
import javax.inject.Inject

// class qui permet de faire la connexion à l'API donc prends en paramètre RegisterApi
class RegisterRepositoryImpl @Inject constructor(
    private val api: RegisterApi,
    private val moshi: com.squareup.moshi.Moshi
) : RegisterRepository {

    override suspend fun register(email: String,username : String, password: String): Result<RegisterResponse> {
        return try {
            val resp = api.register(RegisterRequest(email,username, password))
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
            val code = errorResp?.error_code
                .takeUnless { it == "null" }
                ?: ("errorRegister" + e.code().toString())

            // On renvoie une exception avec le code d'erreur
            Result.failure(ApiErrorException(code))
        } catch (e: java.io.IOException) {
            // IOException est levée si le réseau est inaccessible
            Result.failure(ApiErrorException("NETWORK_ERROR"))
        } catch (e: Exception) {
            // Exception levée si le JSON n'est pas bien formé
            Result.failure(ApiErrorException("UNKNOWN_ERROR"))
        }
    }

}