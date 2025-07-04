package fr.harmony.login.data

import fr.harmony.login.domain.LoginRepository
import javax.inject.Inject

// class qui permet de faire la connexion à l'API donc prends en paramètre LoginApi
class LoginRepositoryImpl @Inject constructor(
    private val api: LoginApi,
    private val moshi: com.squareup.moshi.Moshi
) : LoginRepository {

    override suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val resp = api.login(LoginRequest(email, password))
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
                ?: ("errorLogin" + e.code().toString())

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