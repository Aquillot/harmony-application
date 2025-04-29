package fr.harmony.api

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject


class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()
            .newBuilder()

        val token = tokenManager.getToken()
        if (!token.isNullOrEmpty()){
            request.addHeader("Authorization", "Bearer $token")
        }

        val response = chain.proceed(request.build())

        return response
    }
}
