package nl.rhaydus.softcover.core.network.interceptor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import okhttp3.Interceptor
import okhttp3.Response
import nl.rhaydus.softcover.core.domain.auth.AuthTokenProvider

class AuthInterceptor(
    authTokenProvider: AuthTokenProvider,
) : Interceptor {
    @Volatile
    private var cachedToken: String? = null

    init {
        authTokenProvider.apiKey
            .distinctUntilChanged()
            .onEach { cachedToken = it?.ifBlank { null } }
            .launchIn(CoroutineScope(Dispatchers.IO + SupervisorJob()))
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = if (cachedToken != null) {
            chain.request().newBuilder()
                .addHeader(
                    "Authorization",
                    "Bearer $cachedToken",
                )
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(request)
    }
}
