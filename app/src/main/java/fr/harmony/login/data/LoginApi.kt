package fr.harmony.login.data

import retrofit2.http.Body
import retrofit2.http.POST

// Représentation du JSON d’entrée
data class LoginRequest(
    val email: String,
    val password: String
)

data class ErrorResponse(
    val error: String,
    val error_code: String = null.toString()
)


 // Exception dont le message / la propriété errorCode sera directement renvoyée à la vue.
class ApiErrorException(val errorCode: String) : Exception(errorCode)


// Représentation du JSON de sortie
data class LoginResponse(
    val message: String,    // "Connexion réussie"
    val token: String,      // ton JWT
    val user_id: Int        // id retourné
)

// Interface qui permet de faire la connexion à l'API donc est pris en paramètre par LoginRepositoryImpl
interface LoginApi {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}
