package nl.rhaydus.softcover.core.preferences.data.datasource

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.common.AppLog
import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.softcover.core.domain.auth.AuthTokenProvider
import nl.rhaydus.softcover.core.preferences.data.datastore.AppSettingsDataStore
import nl.rhaydus.softcover.core.preferences.data.security.SecureApiKeyStorage
import kotlin.concurrent.Volatile

interface ApiKeyLocalDataSource : AuthTokenProvider {
    override val apiKey: Flow<String?>

    suspend fun updateApiKey(key: String)

    suspend fun clear()
}

internal class ApiKeyLocalDataSourceImpl(
    private val secureStorage: SecureApiKeyStorage,
    private val appSettingsDataStore: AppSettingsDataStore,
    private val dispatchers: AppDispatchers,
) : ApiKeyLocalDataSource {
    private val apiKeyFlow = MutableStateFlow<String?>(null)
    private val diskMutex = Mutex()

    @Volatile
    private var hasLoaded = false

    override val apiKey: Flow<String?> = apiKeyFlow.asStateFlow()

    init {
        CoroutineScope(dispatchers.io + SupervisorJob()).launch {
            initialize()
        }
    }

    private suspend fun initialize() = withContext(dispatchers.io) {
        diskMutex.withLock {
            if (hasLoaded) return@withLock

            migrateLegacyKeyIfNeeded()

            apiKeyFlow.value = secureStorage.read()
            hasLoaded = true
        }
    }

    override suspend fun updateApiKey(key: String) {
        withContext(dispatchers.io) {
            diskMutex.withLock {
                if (key.isBlank()) {
                    secureStorage.delete()
                    apiKeyFlow.value = null
                } else {
                    secureStorage.write(value = key)
                    apiKeyFlow.value = key
                }

                hasLoaded = true
            }
        }
    }

    override suspend fun clear() {
        updateApiKey(key = "")
    }

    private suspend fun migrateLegacyKeyIfNeeded() {
        // Runs from the unsupervised `init` coroutine, so nothing downstream would catch a throw. A
        // terminal read re-throws an upstream failure even in its `firstOrNull()` form (DataStore raises
        // `CorruptionException` on a damaged file), so the read is wrapped: an absent OR unreadable
        // snapshot alike means there is nothing to migrate.
        val legacyKey = runCatchingCancellable {
            appSettingsDataStore.store.data.firstOrNull()?.apiKey
        }.onFailure { error ->
            AppLog.e(
                error,
                "Failed to read legacy settings while migrating the API key",
            )
        }.getOrNull().orEmpty()

        if (legacyKey.isBlank()) return

        runCatching {
            secureStorage.write(value = legacyKey)
        }.onFailure { error ->
            AppLog.e(
                error,
                "Failed to migrate legacy API key to secure storage",
            )

            return
        }

        appSettingsDataStore.store.updateData { it.copy(apiKey = "") }
    }
}
