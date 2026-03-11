package nl.rhaydus.softcover.core.data.network.interceptor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import nl.rhaydus.softcover.feature.settings.data.datastore.AppSettingsDataStore
import okhttp3.Interceptor
import okhttp3.Response
import org.koin.core.component.KoinComponent

class AuthInterceptor(
    // NOTE: Directly injects the local storage to prevent a dependency circle
    appSettingsDataStore: AppSettingsDataStore,
) : Interceptor, KoinComponent {

    @Volatile
    private var cachedToken: String? = null

    init {
        appSettingsDataStore.store.data.map { it.apiKey }
            .distinctUntilChanged()
            .onEach { cachedToken = it.ifBlank { null } }
            .launchIn(CoroutineScope(Dispatchers.IO + SupervisorJob()))
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = if (cachedToken != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $cachedToken")
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(request)
    }
}