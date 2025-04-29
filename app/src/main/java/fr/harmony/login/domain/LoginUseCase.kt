package fr.harmony.login.domain

import javax.inject.Inject

// LoginUseCase est une classe qui permet de faire la connexion à l'API donc elle utilise le repository
class LoginUseCase @Inject constructor(
    private val repo: LoginRepository
) {
    suspend fun login(email: String, password: String) =
        repo.login(email, password)
}
