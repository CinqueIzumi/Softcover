package nl.rhaydus.softcover.core.data.network.interceptor

import kotlinx.coroutines.runBlocking
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetApiKeyUseCase
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val getApiKeyUseCase: GetApiKeyUseCase,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { getApiKeyUseCase() }.getOrNull()

        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(request)
    }
}