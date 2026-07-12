package nl.rhaydus.softcover.core.preferences.data.security

import android.content.Context
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import nl.rhaydus.common.AppDispatchers

class AndroidLegacySecureApiKeyStorageTest {
    @TempDir
    lateinit var tempDir: File

    private lateinit var context: Context
    private lateinit var dispatchers: AppDispatchers

    private lateinit var decryptCipher: Cipher
    private lateinit var secretKey: SecretKey
    private lateinit var keyStore: KeyStore

    private val fakeIv = ByteArray(12) { it.toByte() }

    @BeforeEach
    fun setUp() {
        context = mockk {
            every {
                filesDir
            } returns tempDir
        }

        val dispatcher = UnconfinedTestDispatcher()
        dispatchers = AppDispatchers(
            main = dispatcher,
            io = dispatcher,
            default = dispatcher,
        )

        secretKey = mockk()
        keyStore = mockk(relaxed = true)
        decryptCipher = mockk(relaxed = true)

        mockkStatic(Cipher::class)
        mockkStatic(KeyStore::class)
        mockkStatic(KeyGenerator::class)

        every {
            KeyStore.getInstance("AndroidKeyStore")
        } returns keyStore

        every {
            keyStore.getKey(
                "softcover_api_key",
                null,
            )
        } returns secretKey

        every {
            Cipher.getInstance("AES/GCM/NoPadding")
        } returns decryptCipher

        every {
            KeyGenerator.getInstance(any())
        } returns mockk(relaxed = true)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    private fun buildStorage(): AndroidLegacySecureApiKeyStorage =
        AndroidLegacySecureApiKeyStorage(
            context = context,
            dispatchers = dispatchers,
        )

    private fun storageFile(): File = File(
        tempDir,
        "api_key.enc",
    )

    private fun writeLegacyFile(
        iv: ByteArray = fakeIv,
        ciphertext: ByteArray = "ciphertext".toByteArray(),
    ) {
        storageFile().outputStream().use { out ->
            out.write(iv.size)
            out.write(iv)
            out.write(ciphertext)
        }
    }

    @Nested
    inner class Read {
        @Test
        fun `returns the decrypted value when the legacy file and Keystore alias both exist`() = runTest {
            // ----- Arrange -----
            val storage = buildStorage()
            writeLegacyFile()

            every {
                decryptCipher.doFinal(any())
            } returns "my-legacy-api-key".toByteArray(Charsets.UTF_8)

            // ----- Act -----
            val result = storage.read()

            // ----- Assert -----
            result shouldBe "my-legacy-api-key"
        }

        @Test
        fun `returns null when the legacy file is absent`() = runTest {
            // ----- Arrange -----
            val storage = buildStorage()

            // ----- Act -----
            val result = storage.read()

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns null and never generates a new key when the Keystore alias is gone`() = runTest {
            // ----- Arrange -----
            val storage = buildStorage()
            writeLegacyFile()

            every {
                keyStore.getKey(
                    "softcover_api_key",
                    null,
                )
            } returns null

            // ----- Act -----
            val result = storage.read()

            // ----- Assert -----
            result shouldBe null
            verify(exactly = 0) { KeyGenerator.getInstance(any()) }
        }

        @Test
        fun `returns null and deletes the file when decryption throws`() = runTest {
            // ----- Arrange -----
            val storage = buildStorage()
            writeLegacyFile()

            every {
                decryptCipher.doFinal(any())
            } throws RuntimeException("decryption failed")

            // ----- Act -----
            val result = storage.read()

            // ----- Assert -----
            result shouldBe null
            storageFile().exists() shouldBe false
        }
    }

    @Nested
    inner class Delete {
        @Test
        fun `removes both the file and the Keystore entry`() = runTest {
            // ----- Arrange -----
            val storage = buildStorage()
            writeLegacyFile()

            // ----- Act -----
            storage.delete()

            // ----- Assert -----
            storageFile().exists() shouldBe false
            verify(exactly = 1) { keyStore.deleteEntry("softcover_api_key") }
        }
    }
}
