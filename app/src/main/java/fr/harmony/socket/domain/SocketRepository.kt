package fr.harmony.socket.domain

interface SocketRepository {
    suspend fun getSocketPort(): SocketResult
}