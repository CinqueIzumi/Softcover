package nl.rhaydus.softcover.core.preferences.data.datasource

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import nl.rhaydus.softcover.core.domain.auth.AuthTokenProvider
import nl.rhaydus.softcover.core.domain.logging.AppLog
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
import nl.rhaydus.softcover.core.preferences.data.datastore.AppSettingsDataStore

interface ApiKeyLocalDataSource : AuthTokenProvider {
    override val apiKey: Flow<String?>

    suspend fun updateApiKey(key: String)

    suspend fun clear()
}

internal class ApiKeyLocalDataSourceImpl(
    private val context: Context,
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

            apiKeyFlow.value = readFromDisk()
            hasLoaded = true
        }
    }

    override suspend fun updateApiKey(key: String) {
        withContext(dispatchers.io) {
            diskMutex.withLock {
                if (key.isBlank()) {
                    deleteFromDisk()
                    apiKeyFlow.value = null
                } else {
                    writeToDisk(value = key)
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
        val legacyKey = appSettingsDataStore.store.data.first().apiKey

        if (legacyKey.isBlank()) return

        runCatching {
            writeToDisk(value = legacyKey)
        }.onFailure { error ->
            AppLog.e(
                error,
                "Failed to migrate legacy API key to secure storage",
            )

            return
        }

        appSettingsDataStore.store.updateData { it.copy(apiKey = "") }
    }

    private fun writeToDisk(value: String) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.ENCRYPT_MODE,
            getOrCreateKey(),
        )

        val iv = cipher.iv
        val ciphertext = cipher.doFinal(value.toByteArray(Charsets.UTF_8))

        storageFile().outputStream().use { output ->
            output.write(iv.size)
            output.write(iv)
            output.write(ciphertext)
        }
    }

    private fun readFromDisk(): String? {
        val file = storageFile()

        if (file.exists().not()) return null

        return runCatching {
            file.inputStream().use { input ->
                val ivSize = input.read()
                val iv = ByteArray(ivSize).also { input.read(it) }
                val ciphertext = input.readBytes()

                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(
                    Cipher.DECRYPT_MODE,
                    getOrCreateKey(),
                    GCMParameterSpec(
                        GCM_TAG_LENGTH_BITS,
                        iv,
                    ),
                )

                String(
                    cipher.doFinal(ciphertext),
                    Charsets.UTF_8,
                )
            }
        }.getOrElse { error ->
            AppLog.e(
                error,
                "Failed to decrypt API key — discarding ciphertext",
            )

            file.delete()

            null
        }
    }

    private fun deleteFromDisk() {
        storageFile().delete()
    }

    private fun storageFile(): File = File(
        context.filesDir,
        FILE_NAME,
    )

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }

        val existing = keyStore.getKey(
            KEY_ALIAS,
            null,
        ) as? SecretKey

        if (existing != null) return existing

        val generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEY_STORE,
        )

        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_SIZE_BITS)
            .build()

        generator.init(spec)

        return generator.generateKey()
    }

    private companion object {
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val KEY_ALIAS = "softcover_api_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_SIZE_BITS = 256
        const val GCM_TAG_LENGTH_BITS = 128
        const val FILE_NAME = "api_key.enc"
    }
}
