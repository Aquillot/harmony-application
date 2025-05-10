package fr.harmony.harmonize.data

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface HarmonizeApi {

    @Multipart
    @POST("images/upload")
    suspend fun uploadImages(
        @Part original: MultipartBody.Part,
        @Part harmonized: MultipartBody.Part
    ): HarmonizeResponse
}