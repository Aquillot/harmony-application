package fr.harmony.register.domain

import fr.harmony.register.data.RegisterResponse

// Interface qui définit les méthodes de connexion
interface RegisterRepository {
    suspend fun register(
        email: String, username : String,
        password: String
    ): Result<RegisterResponse>
}
