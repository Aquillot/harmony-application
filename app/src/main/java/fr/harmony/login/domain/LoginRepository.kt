package fr.harmony.login.domain

import fr.harmony.login.data.LoginResponse

// Interface qui définit les méthodes de connexion
interface LoginRepository {
    suspend fun login(
        email: String,
        password: String
    ): Result<LoginResponse>
}
