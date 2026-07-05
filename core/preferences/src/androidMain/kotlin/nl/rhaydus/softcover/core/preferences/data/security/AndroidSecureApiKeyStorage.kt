package nl.rhaydus.softcover.core.preferences.data.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import kotlinx.coroutines.withContext
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.common.AppLog

/**
 * Android secure storage: the API key is encrypted with an AES/GCM key held in the Android Keystore
 * and the ciphertext (IV size byte + IV + ciphertext) is written to `filesDir/api_key.enc`. The key
 * alias, transformation, and file layout match the pre-KMP implementation, so keys written by older
 * builds keep decrypting.
 */
internal class AndroidSecureApiKeyStorage(
    private val context: Context,
    private val dispatchers: AppDispatchers,
) : SecureApiKeyStorage {
    override suspend fun read(): String? = withContext(dispatchers.io) {
        val file = storageFile()

        if (file.exists().not()) return@withContext null

        runCatching {
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

    override suspend fun write(value: String) {
        withContext(dispatchers.io) {
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
    }

    override suspend fun delete() {
        withContext(dispatchers.io) {
            storageFile().delete()
        }
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
