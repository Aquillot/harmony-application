package fr.harmony.profile.domain

import javax.inject.Inject

// ProfileUseCase est une classe qui permet de faire la connexion à l'API donc elle utilise le repository
class ProfileUseCase @Inject constructor(
    private val repo: ProfileRepository
) {
    suspend fun getProfile() =
        repo.getProfile()
}
