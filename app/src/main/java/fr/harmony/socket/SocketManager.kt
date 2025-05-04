package fr.harmony.socket

import fr.harmony.socket.domain.SocketRepository
import fr.harmony.socket.domain.SocketResult
import io.socket.client.IO
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject

@Singleton
class SocketManager @Inject constructor(
    private val idProvider: SocketRepository
) {
    private var socket: io.socket.client.Socket? = null

    @OptIn(InternalCoroutinesApi::class)
    suspend fun connect(): Boolean {
        if (socket?.connected() == true) {
            return true
        }

        val id = when (val result = idProvider.getSocketPort()) {
            is SocketResult.Success -> result.id
            else -> return false
        }

        val socketUrl = "https://harmony.jhune.dev"
        println("Tentative de connexion à $socketUrl/$id")

        val opts = IO.Options.builder()
            .setPath("/$id/socket.io")
            .setReconnection(true)
            .setTransports(arrayOf("websocket"))
            .build()

        socket = IO.socket(socketUrl, opts)
        socket?.connect()

        return suspendCancellableCoroutine { cont ->
            val callback = object {
                fun cleanup() {
                    socket?.off(io.socket.client.Socket.EVENT_CONNECT)
                    socket?.off(io.socket.client.Socket.EVENT_CONNECT_ERROR)
                }
            }

            socket?.on(io.socket.client.Socket.EVENT_CONNECT) {
                println("Connexion réussie au socket")
                callback.cleanup()
                cont.tryResume(true)?.let { token ->
                    cont.completeResume(token)
                }
            }

            socket?.on(io.socket.client.Socket.EVENT_CONNECT_ERROR) { args ->
                println("Erreur de connexion au socket : ${args.joinToString()}")
                callback.cleanup()
                cont.tryResume(false)?.let { token ->
                    cont.completeResume(token)
                }
            }
        }

    }

    fun emit(event: String, data: Any) {
        socket?.emit(event, data)
    }

    fun on(event: String, callback: (args: Array<Any>) -> Unit) {
        socket?.on(event) { args -> callback(args) }
    }

    fun off(event: String) {
        socket?.off(event)
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
    }
}