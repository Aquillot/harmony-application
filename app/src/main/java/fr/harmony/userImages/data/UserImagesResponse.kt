package fr.harmony.userImages.data

data class User(
    val id: Int,
    val username: String
)

data class SharedImage(
    val image_id: Int,
    val user: User,
    val original_image_url: String,
    val harmonized_image_url: String,
    val original_votes: Int,
    val harmonized_votes: Int,
    val user_vote: String?, // null ou "original" ou "harmonized"
    val created_at: String,
)

data class DeleteResponse(
    val message: String,
)

data class ErrorResponse(
    val error: String,
    val error_code: String = null.toString()
)

class ApiErrorException(val errorCode: String) : Exception(errorCode)