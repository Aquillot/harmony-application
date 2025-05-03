package fr.harmony.profile.domain

import fr.harmony.profile.data.ProfileResponse

// Interface qui définit les méthodes de connexion
interface ProfileRepository {
    suspend fun getProfile(): Result<ProfileResponse>
}
