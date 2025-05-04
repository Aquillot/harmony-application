package fr.harmony.socket.data

import fr.harmony.socket.domain.SocketRepository
import fr.harmony.socket.domain.SocketResult
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class SocketRepositoryImpl @Inject constructor(
    private val api: SocketApi
) : SocketRepository {

    override suspend fun getSocketPort(): SocketResult {
        return try {
            val response = api.getSocketId()
            SocketResult.Success(response.socket_id)
        } catch (e: HttpException) {
            SocketResult.HttpError(e.code())
        } catch (e: IOException) {
            SocketResult.NetworkError
        } catch (e: Exception) {
            SocketResult.UnknownError
        }
    }
}
