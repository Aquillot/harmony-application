package fr.harmony.explore.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ExploreApi {
    @GET("images")
    suspend fun getImages(): List<SharedImage>

    @POST("images/{imageId}/vote")
    suspend fun vote(@Path("imageId") imageId: Int, @Body request: RegisterVote): VoteResponse
}