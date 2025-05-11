package fr.harmony.userImages.domain

import fr.harmony.userImages.data.DeleteResponse
import fr.harmony.userImages.data.SharedImage

interface UserImagesRepository {
    suspend fun getUserImages(): Result<List<SharedImage>>
    suspend fun delete(imageId: Int): Result<DeleteResponse>
}