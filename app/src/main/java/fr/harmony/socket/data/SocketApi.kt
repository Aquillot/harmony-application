package fr.harmony.socket.data

import retrofit2.http.GET

data class SocketIdResponse(
    val socket_id: Int
)

interface SocketApi {
    @GET("get_socket_id")
    suspend fun getSocketId(): SocketIdResponse
}