package fr.harmony.register.data


import retrofit2.http.Body
import retrofit2.http.POST

// Représentation du JSON d’entrée
data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String
)

data class ErrorResponse(
    val error: String,
    val error_code: String = null.toString()
)


 // Exception dont le message / la propriété errorCode sera directement renvoyée à la vue.
class ApiErrorException(val errorCode: String) : Exception(errorCode)


// Représentation du JSON de sortie
data class RegisterResponse(
    val message: String,    // "Connexion réussie"
    val user_id: Int        // id retourné
)

// Interface qui permet de faire la connexion à l'API donc est pris en paramètre par RegisterRepositoryImpl
interface RegisterApi {
    @POST("add_user")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse
}
