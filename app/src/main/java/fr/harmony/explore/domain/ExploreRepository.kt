package fr.harmony.explore.domain

import fr.harmony.explore.data.SharedImage
import fr.harmony.explore.data.VoteResponse

interface ExploreRepository {
    suspend fun getImages(): Result<List<SharedImage>>
    suspend fun vote(imageId: Int, isOriginalVote: Boolean): Result<VoteResponse>
}