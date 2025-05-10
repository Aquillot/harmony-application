package fr.harmony.harmonize.data

data class HarmonizeResponse(
    val message: String,   // "Images téléchargées avec succès"
    val image_id: Int
)

data class ErrorResponse(
    val error: String,
    val error_code: String = null.toString()
)

class ApiErrorException(val errorCode: String) : Exception(errorCode)