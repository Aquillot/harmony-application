package fr.harmony.profile.data

import retrofit2.http.GET

data class ErrorResponse(
    val error: String,
    val error_code: String = null.toString()
)


 // Exception dont le message / la propriété errorCode sera directement renvoyée à la vue.
class ApiErrorException(val errorCode: String) : Exception(errorCode)


// Représentation du JSON de sortie
data class ProfileResponse(
    val id: Int,    // "id" de l'utilisateur
    val email: String,      // email de l'utilisateur
    val username: String        // username de l'utilisateur
)

// Interface qui permet de faire la connexion à l'API donc est pris en paramètre par ProfileRepositoryImpl
interface ProfileApi {
    @GET("profile")
    suspend fun getProfile(): ProfileResponse
}
