package fr.harmony.register.domain

import fr.harmony.login.domain.LoginRepository
import javax.inject.Inject

// LoginUseCase est une classe qui permet de faire la connexion à l'API donc elle utilise le repository
class RegisterUseCase @Inject constructor(
    private val repo: RegisterRepository,
    private val repoLogin: LoginRepository
) {
    suspend fun login(email: String, password: String) =
        repoLogin.login(email, password)

    suspend fun register(email: String, username : String, password: String) =
        repo.register(email,username, password)
}
