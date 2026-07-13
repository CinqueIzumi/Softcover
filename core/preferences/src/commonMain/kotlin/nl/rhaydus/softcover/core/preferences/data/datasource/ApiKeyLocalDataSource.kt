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
import kotlin.concurrent.Volatile
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.common.AppLog
import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.platform.SecureStorage
import nl.rhaydus.softcover.core.domain.auth.AuthTokenProvider
import nl.rhaydus.softcover.core.preferences.data.datastore.AppSettingsDataStore
import nl.rhaydus.softcover.core.preferences.data.security.LegacySecureApiKeyStorage

/** The single key Softcover stores in the foundation's keyed [SecureStorage]. */
private const val API_KEY = "api_key"

interface ApiKeyLocalDataSource : AuthTokenProvider {
    override val apiKey: Flow<String?>

    suspend fun updateApiKey(key: String)

    suspend fun clear()
}

internal class ApiKeyLocalDataSourceImpl(
    private val secureStorage: SecureStorage,
    private val appSettingsDataStore: AppSettingsDataStore,
    private val dispatchers: AppDispatchers,
    // Absent on desktop, where the key never moved. Null once the migration release has aged out.
    private val legacySecureStorage: LegacySecureApiKeyStorage? = null,
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

            apiKeyFlow.value = readSecureKey()
            hasLoaded = true
        }
    }

    override suspend fun updateApiKey(key: String) {
        withContext(dispatchers.io) {
            diskMutex.withLock {
                if (key.isBlank()) {
                    secureStorage.delete(key = API_KEY)
                    apiKeyFlow.value = null
                } else {
                    secureStorage.write(
                        key = API_KEY,
                        value = key,
                    )
                    apiKeyFlow.value = key
                }

                hasLoaded = true
            }
        }
    }

    override suspend fun clear() {
        updateApiKey(key = "")
    }

    /**
     * Carries the API key forward into the foundation's [SecureStorage] from wherever an older build left
     * it. Two legs, newest first: Softcover's pre-foundation secure store (Android Keystore file / iOS
     * Keychain, both of which the foundation store addresses differently), then the plain-text field in the
     * settings DataStore that predates secure storage entirely.
     *
     * Idempotent: a key already in the foundation store skips straight to the plain-text cleanup. Every call
     * is wrapped — this runs from the unsupervised `init` coroutine, where a throw reaches the default
     * uncaught handler and takes the process down, and a failed migration should cost a re-login, never a
     * crash loop.
     */
    private suspend fun migrateLegacyKeyIfNeeded() {
        if (readSecureKey() == null) {
            val carried = migrateFromLegacySecureStorage() || migrateFromPlainTextSettings()

            if (carried.not()) return
        }

        // Runs on every start, not only on the launch that migrates: clearing the plain-text field can
        // fail, and short-circuiting on a present secure key would then leave the secret readable on disk
        // forever. Guarded on the secure copy existing, so a failed write never destroys the only copy.
        if (readSecureKey() != null) clearPlainTextApiKey()
    }

    private suspend fun migrateFromLegacySecureStorage(): Boolean {
        val legacyKey = runCatchingCancellable { legacySecureStorage?.read() }
            .onFailure { error ->
                AppLog.e(
                    error,
                    "Failed to read the pre-foundation secure API key",
                )
            }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: return false

        val written = runCatchingCancellable {
            secureStorage.write(
                key = API_KEY,
                value = legacyKey,
            )
        }.onFailure { error ->
            AppLog.e(
                error,
                "Failed to migrate the pre-foundation API key into secure storage",
            )
        }.isSuccess

        // Only drop the old copy once the new one is safely written; otherwise a failure here would
        // destroy the only copy of the key and silently sign the user out.
        if (written) {
            runCatchingCancellable { legacySecureStorage?.delete() }
                .onFailure { error ->
                    AppLog.e(
                        error,
                        "Migrated the API key but failed to remove the pre-foundation copy",
                    )
                }
        }

        return written
    }

    private suspend fun migrateFromPlainTextSettings(): Boolean {
        val legacyKey = readPlainTextApiKey()

        if (legacyKey.isBlank()) return false

        return runCatchingCancellable {
            secureStorage.write(
                key = API_KEY,
                value = legacyKey,
            )
        }.onFailure { error ->
            AppLog.e(
                error,
                "Failed to migrate legacy API key to secure storage",
            )
        }.isSuccess
    }

    /**
     * A terminal read re-throws an upstream failure even in its `firstOrNull()` form (DataStore raises
     * `CorruptionException` on a damaged file): an absent OR unreadable snapshot alike means there is
     * nothing to migrate.
     */
    private suspend fun readPlainTextApiKey(): String = runCatchingCancellable {
        appSettingsDataStore.store.data.firstOrNull()?.apiKey
    }.onFailure { error ->
        AppLog.e(
            error,
            "Failed to read legacy settings while migrating the API key",
        )
    }.getOrNull().orEmpty()

    private suspend fun clearPlainTextApiKey() {
        if (readPlainTextApiKey().isBlank()) return

        runCatchingCancellable {
            appSettingsDataStore.store.updateData { it.copy(apiKey = "") }
        }.onFailure { error ->
            AppLog.e(
                error,
                "Failed to clear the plain-text API key from settings; will retry on next start",
            )
        }
    }

    private suspend fun readSecureKey(): String? = runCatchingCancellable {
        secureStorage.read(key = API_KEY)
    }.onFailure { error ->
        AppLog.e(
            error,
            "Failed to read the API key from secure storage",
        )
    }.getOrNull()
}
