package fr.harmony.login.data

import fr.harmony.login.domain.LoginRepository
import javax.inject.Inject

// class qui permet de faire la connexion à l'API donc prends en paramètre LoginApi
class LoginRepositoryImpl @Inject constructor(
    private val api: LoginApi
) : LoginRepository {
    override suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val resp = api.login(LoginRequest(email, password))
            if (resp.message == "Connexion réussie")
                Result.success(resp)
            else Result.failure(RuntimeException(resp.message ?: "Unknown error"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
