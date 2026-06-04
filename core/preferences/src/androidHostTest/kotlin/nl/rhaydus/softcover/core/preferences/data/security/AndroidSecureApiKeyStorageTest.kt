package nl.rhaydus.softcover.core.preferences.data.security

import android.content.Context
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
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
import javax.crypto.spec.GCMParameterSpec

class AndroidSecureApiKeyStorageTest {
    @TempDir
    lateinit var tempDir: File

    private lateinit var context: Context
    private lateinit var dispatchers: AppDispatchers

    private lateinit var encryptCipher: Cipher
    private lateinit var decryptCipher: Cipher
    private lateinit var secretKey: SecretKey
    private lateinit var keyStore: KeyStore
    private lateinit var keyGenerator: KeyGenerator

    private val fakeIv = ByteArray(12) { it.toByte() }

    @BeforeEach
    fun setUp() {
        context = mockk {
            every { filesDir } returns tempDir
        }

        val dispatcher = UnconfinedTestDispatcher()
        dispatchers = AppDispatchers(
            main = dispatcher,
            io = dispatcher,
            default = dispatcher,
        )

        secretKey = mockk()
        keyStore = mockk(relaxed = true)
        keyGenerator = mockk(relaxed = true)
        encryptCipher = mockk(relaxed = true)
        decryptCipher = mockk(relaxed = true)

        mockkStatic(Cipher::class)
        mockkStatic(KeyStore::class)
        mockkStatic(KeyGenerator::class)

        every { KeyStore.getInstance("AndroidKeyStore") } returns keyStore
        every { keyStore.getKey(
            "softcover_api_key",
            null,
        ) } returns secretKey

        every { encryptCipher.iv } returns fakeIv

        val plaintextSlot = slot<ByteArray>()
        every { encryptCipher.doFinal(capture(plaintextSlot)) } answers { plaintextSlot.captured }

        val ciphertextSlot = slot<ByteArray>()
        every { decryptCipher.doFinal(capture(ciphertextSlot)) } answers { ciphertextSlot.captured }

        every { Cipher.getInstance("AES/GCM/NoPadding") } returnsMany listOf(encryptCipher, decryptCipher)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    private fun buildStorage(): AndroidSecureApiKeyStorage =
        AndroidSecureApiKeyStorage(
            context = context,
            dispatchers = dispatchers,
        )

    @Nested
    inner class Write {
        @Test
        fun `creates api_key enc file on disk after write`() = runTest {
            // ----- Arrange -----
            val storage = buildStorage()
            val storageFile = File(
                tempDir,
                "api_key.enc",
            )

            // ----- Act -----
            storage.write("my-api-key")

            // ----- Assert -----
            storageFile.exists() shouldBe true
        }
    }

    @Nested
    inner class Read {
        @Test
        fun `returns the round-tripped value after write then read`() = runTest {
            // ----- Arrange -----
            val storage = buildStorage()
            storage.write("my-api-key")

            // Reset so read gets decryptCipher on the next getInstance call.
            every { Cipher.getInstance("AES/GCM/NoPadding") } returns decryptCipher

            // ----- Act -----
            val result = storage.read()

            // ----- Assert -----
            result shouldBe "my-api-key"
        }

        @Test
        fun `returns null when no file exists`() = runTest {
            // ----- Arrange -----
            val storage = buildStorage()

            // ----- Act -----
            val result = storage.read()

            // ----- Assert -----
            result shouldBe null
        }
    }

    @Nested
    inner class Delete {
        @Test
        fun `removes api_key enc file from disk after delete`() = runTest {
            // ----- Arrange -----
            val storage = buildStorage()
            storage.write("key-to-delete")
            val storageFile = File(
                tempDir,
                "api_key.enc",
            )

            // ----- Act -----
            storage.delete()

            // ----- Assert -----
            storageFile.exists() shouldBe false
        }
    }
}
