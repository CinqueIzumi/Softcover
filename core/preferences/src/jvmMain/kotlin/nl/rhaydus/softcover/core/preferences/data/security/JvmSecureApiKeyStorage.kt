package nl.rhaydus.softcover.core.preferences.data.security

import eu.anifantakis.lib.ksafe.KSafe
import kotlinx.coroutines.withContext
import nl.rhaydus.softcover.core.domain.logging.AppLog
import nl.rhaydus.ui.common.AppDispatchers

/**
 * Desktop secure storage: the API key is held by [KSafe], which encrypts it and custodies the
 * encryption key in the OS secret store — macOS Keychain, Windows DPAPI, or Linux Secret Service —
 * falling back to a software key (with a warning) when no OS vault is available. The desktop
 * counterpart of the Android Keystore / iOS Keychain implementations; I/O runs on the IO dispatcher.
 */
internal class JvmSecureApiKeyStorage(
    private val ksafe: KSafe,
    private val dispatchers: AppDispatchers,
) : SecureApiKeyStorage {
    override suspend fun read(): String? = withContext(dispatchers.io) {
        runCatching {
            ksafe.get<String?>(
                KEY,
                null,
            )
        }.getOrElse { error ->
            AppLog.e(
                error,
                "Failed to read API key from the OS secret store",
            )

            null
        }
    }

    override suspend fun write(value: String) {
        withContext(dispatchers.io) {
            ksafe.put(
                KEY,
                value,
            )
        }
    }

    override suspend fun delete() {
        withContext(dispatchers.io) {
            ksafe.delete(KEY)
        }
    }

    private companion object {
        const val KEY = "api_key"
    }
}
