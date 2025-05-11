package fr.harmony.userImages.data

import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

interface UserImagesApi {
    @GET("images/user")
    suspend fun getUserImages(): List<SharedImage>

    @DELETE("images/{imageId}")
    suspend fun delete(@Path("imageId") imageId: Int): DeleteResponse
}