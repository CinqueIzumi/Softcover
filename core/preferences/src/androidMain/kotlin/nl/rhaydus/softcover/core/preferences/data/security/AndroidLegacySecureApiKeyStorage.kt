package nl.rhaydus.softcover.core.preferences.data.security

import android.security.keystore.KeyProperties
import kotlinx.coroutines.withContext
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.content.Context
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.common.AppLog
import nl.rhaydus.common.runCatchingCancellable

/**
 * Reads the API key from Softcover's **pre-foundation** Android location: ciphertext at
 * `filesDir/api_key.enc`, encrypted with the AES/GCM key held in the Keystore under alias
 * `softcover_api_key`. See [LegacySecureApiKeyStorage] — migration only, delete after one release.
 *
 * Unlike the store it replaces, this never *generates* a Keystore key. A missing alias means there is
 * nothing to migrate, and creating one here would leave a stray key behind on every clean install.
 */
internal class AndroidLegacySecureApiKeyStorage(
    private val context: Context,
    private val dispatchers: AppDispatchers,
) : LegacySecureApiKeyStorage {
    override suspend fun read(): String? = withContext(dispatchers.io) {
        val file = storageFile()

        if (file.exists().not()) return@withContext null

        val secretKey = existingKey() ?: return@withContext null

        runCatchingCancellable {
            file.inputStream().use { input ->
                val ivSize = input.read()
                val iv = ByteArray(ivSize).also { input.read(it) }
                val ciphertext = input.readBytes()

                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(
                    Cipher.DECRYPT_MODE,
                    secretKey,
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
                "Failed to decrypt the legacy API key — discarding it",
            )

            file.delete()

            null
        }
    }

    override suspend fun delete() {
        withContext(dispatchers.io) {
            storageFile().delete()

            runCatchingCancellable {
                KeyStore.getInstance(ANDROID_KEY_STORE)
                    .apply { load(null) }
                    .deleteEntry(KEY_ALIAS)
            }.onFailure { error ->
                AppLog.e(
                    error,
                    "Failed to remove the legacy API key from the Keystore",
                )
            }
        }
    }

    private fun storageFile(): File = File(
        context.filesDir,
        FILE_NAME,
    )

    private fun existingKey(): SecretKey? = runCatchingCancellable {
        KeyStore.getInstance(ANDROID_KEY_STORE)
            .apply { load(null) }
            .getKey(
                KEY_ALIAS,
                null,
            ) as? SecretKey
    }.getOrNull()

    private companion object {
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val KEY_ALIAS = "softcover_api_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_LENGTH_BITS = 128
        const val FILE_NAME = "api_key.enc"
    }
}
