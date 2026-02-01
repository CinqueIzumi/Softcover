package nl.rhaydus.softcover.core.data.network.interceptor

import kotlinx.coroutines.runBlocking
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetApiKeyUseCase
import okhttp3.Interceptor
import okhttp3.Response
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class AuthInterceptor : Interceptor, KoinComponent {

    private val getApiKeyUseCase: GetApiKeyUseCase by inject()

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { getApiKeyUseCase() }
            .onFailure { Timber.e("-=- $it") }
            .getOrNull()

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