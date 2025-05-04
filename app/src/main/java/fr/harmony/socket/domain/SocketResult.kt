package fr.harmony.socket.domain

sealed class SocketResult {
    data class Success(val id: Int) : SocketResult()
    data class HttpError(val code: Int) : SocketResult()
    data object NetworkError : SocketResult()
    data object UnknownError : SocketResult()
}