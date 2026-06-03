package nl.rhaydus.softcover.core.preferences.data.datasource

import androidx.datastore.core.DataStore
import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
import nl.rhaydus.softcover.core.preferences.data.datastore.AppSettingsDataStore
import nl.rhaydus.softcover.core.preferences.data.model.AppSettingsEntity
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

class ApiKeyLocalDataSourceImplTest {
    @TempDir
    lateinit var tempDir: File

    private lateinit var dataStore: DataStore<AppSettingsEntity>
    private var appSettingsDataStore: AppSettingsDataStore = AppSettingsDataStore(store = mockk(relaxed = true))
    private lateinit var dispatchers: AppDispatchers

    private lateinit var encryptCipher: Cipher
    private lateinit var decryptCipher: Cipher
    private lateinit var secretKey: SecretKey
    private lateinit var keyStore: KeyStore
    private lateinit var keyGenerator: KeyGenerator

    private val fakeIv = ByteArray(12) { it.toByte() }

    @BeforeEach
    fun setUp() {
        dataStore = mockk(relaxed = true)
        appSettingsDataStore = AppSettingsDataStore(store = dataStore)

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
            encryptCipher.iv
        } returns fakeIv

        // The encrypt cipher writes plaintext bytes as "ciphertext" so readFromDisk can reconstruct the value.
        val plaintextSlot = slot<ByteArray>()
        every {
            encryptCipher.doFinal(capture(plaintextSlot))
        } answers {
            plaintextSlot.captured
        }

        // The decrypt cipher returns whatever bytes it receives, simulating a no-op crypto round-trip.
        val ciphertextSlot = slot<ByteArray>()
        every {
            decryptCipher.doFinal(capture(ciphertextSlot))
        } answers {
            ciphertextSlot.captured
        }

        every {
            Cipher.getInstance("AES/GCM/NoPadding")
        } returnsMany listOf(encryptCipher, decryptCipher)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    private fun stubLegacyKey(apiKey: String) {
        val entity = AppSettingsEntity(apiKey = apiKey)
        every {
            dataStore.data
        } returns flowOf(entity)

        coEvery {
            dataStore.updateData(any())
        } coAnswers {
            val updater = firstArg<suspend (AppSettingsEntity) -> AppSettingsEntity>()
            updater(entity)
        }
    }

    private fun buildDataSource(): ApiKeyLocalDataSourceImpl {
        val context: android.content.Context = mockk {
            every { filesDir } returns tempDir
        }
        return ApiKeyLocalDataSourceImpl(
            context = context,
            appSettingsDataStore = appSettingsDataStore,
            dispatchers = dispatchers,
        )
    }

    @Nested
    inner class Construction {
        @Test
        fun `migrates legacy plaintext key to encrypted file when legacy key is present`() = runTest {
            // ----- Arrange -----
            val legacyKey = "legacy-plaintext-api-key"
            stubLegacyKey(apiKey = legacyKey)

            // ----- Act -----
            buildDataSource()

            // ----- Assert -----
            coVerify {
                dataStore.updateData(any())
            }
        }

        @Test
        fun `clears the legacy apiKey field in the data store after migrating`() = runTest {
            // ----- Arrange -----
            val legacyKey = "legacy-plaintext-api-key"
            val originalEntity = AppSettingsEntity(apiKey = legacyKey)
            var capturedResult: AppSettingsEntity? = null

            every {
                dataStore.data
            } returns flowOf(originalEntity)

            coEvery {
                dataStore.updateData(any())
            } coAnswers {
                val updater = firstArg<suspend (AppSettingsEntity) -> AppSettingsEntity>()
                capturedResult = updater(originalEntity)
                capturedResult!!
            }

            // ----- Act -----
            buildDataSource()

            // ----- Assert -----
            capturedResult?.apiKey shouldBe ""
        }

        @Test
        fun `emits the migrated key on apiKey flow after migration`() = runTest {
            // ----- Arrange -----
            val legacyKey = "legacy-plaintext-api-key"
            stubLegacyKey(apiKey = legacyKey)

            // ----- Act -----
            val dataSource = buildDataSource()

            // ----- Assert -----
            dataSource.apiKey.test {
                awaitItem() shouldBe legacyKey
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `skips migration when legacy apiKey field is blank`() = runTest {
            // ----- Arrange -----
            stubLegacyKey(apiKey = "")

            // ----- Act -----
            buildDataSource()

            // ----- Assert -----
            coVerify(exactly = 0) {
                dataStore.updateData(any())
            }
        }

        @Test
        fun `emits null on apiKey flow when no legacy key is present and no encrypted file exists`() = runTest {
            // ----- Arrange -----
            stubLegacyKey(apiKey = "")

            // ----- Act -----
            val dataSource = buildDataSource()

            // ----- Assert -----
            dataSource.apiKey.test {
                awaitItem() shouldBe null
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class UpdateApiKey {
        @Test
        fun `emits the new key on apiKey flow when key is non-blank`() = runTest {
            // ----- Arrange -----
            stubLegacyKey(apiKey = "")
            val dataSource = buildDataSource()
            val newKey = "my-new-api-key"

            // Reset Cipher mock so the next getInstance call returns encryptCipher for the updateApiKey path.
            every {
                Cipher.getInstance("AES/GCM/NoPadding")
            } returns encryptCipher

            // ----- Act -----
            dataSource.updateApiKey(key = newKey)

            // ----- Assert -----
            dataSource.apiKey.test {
                awaitItem() shouldBe newKey
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `emits null on apiKey flow when key is empty string`() = runTest {
            // ----- Arrange -----
            stubLegacyKey(apiKey = "")
            val dataSource = buildDataSource()

            // Seed a non-null value first so the null emission is observable.
            every {
                Cipher.getInstance("AES/GCM/NoPadding")
            } returns encryptCipher
            dataSource.updateApiKey(key = "initial-key")

            // ----- Act -----
            dataSource.updateApiKey(key = "")

            // ----- Assert -----
            dataSource.apiKey.test {
                awaitItem() shouldBe null
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `emits null on apiKey flow when key is whitespace-only`() = runTest {
            // ----- Arrange -----
            stubLegacyKey(apiKey = "")
            val dataSource = buildDataSource()

            every {
                Cipher.getInstance("AES/GCM/NoPadding")
            } returns encryptCipher
            dataSource.updateApiKey(key = "initial-key")

            // ----- Act -----
            dataSource.updateApiKey(key = "   ")

            // ----- Assert -----
            dataSource.apiKey.test {
                awaitItem() shouldBe null
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `writes encrypted file to disk when key is non-blank`() = runTest {
            // ----- Arrange -----
            stubLegacyKey(apiKey = "")
            val dataSource = buildDataSource()
            val storageFile = File(
                tempDir,
                "api_key.enc",
            )

            every {
                Cipher.getInstance("AES/GCM/NoPadding")
            } returns encryptCipher

            // ----- Act -----
            dataSource.updateApiKey(key = "persist-me")

            // ----- Assert -----
            storageFile.exists() shouldBe true
        }

        @Test
        fun `deletes the encrypted file from disk when key is empty`() = runTest {
            // ----- Arrange -----
            stubLegacyKey(apiKey = "")
            val dataSource = buildDataSource()
            val storageFile = File(
                tempDir,
                "api_key.enc",
            )

            every {
                Cipher.getInstance("AES/GCM/NoPadding")
            } returns encryptCipher

            dataSource.updateApiKey(key = "to-be-deleted")

            // ----- Act -----
            dataSource.updateApiKey(key = "")

            // ----- Assert -----
            storageFile.exists() shouldBe false
        }
    }

    @Nested
    inner class Clear {
        @Test
        fun `emits null on apiKey flow after clear`() = runTest {
            // ----- Arrange -----
            stubLegacyKey(apiKey = "")
            val dataSource = buildDataSource()

            every {
                Cipher.getInstance("AES/GCM/NoPadding")
            } returns encryptCipher
            dataSource.updateApiKey(key = "active-key")

            // ----- Act -----
            dataSource.clear()

            // ----- Assert -----
            dataSource.apiKey.test {
                awaitItem() shouldBe null
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `deletes the encrypted file from disk after clear`() = runTest {
            // ----- Arrange -----
            stubLegacyKey(apiKey = "")
            val dataSource = buildDataSource()
            val storageFile = File(
                tempDir,
                "api_key.enc",
            )

            every {
                Cipher.getInstance("AES/GCM/NoPadding")
            } returns encryptCipher
            dataSource.updateApiKey(key = "active-key")

            // ----- Act -----
            dataSource.clear()

            // ----- Assert -----
            storageFile.exists() shouldBe false
        }
    }

    @Nested
    inner class ApiKeyFlow {
        @Test
        fun `emits null as initial value when no key has been set`() = runTest {
            // ----- Arrange -----
            stubLegacyKey(apiKey = "")

            // ----- Act -----
            val dataSource = buildDataSource()

            // ----- Assert -----
            dataSource.apiKey.test {
                awaitItem() shouldBe null
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `emits updated value immediately after updateApiKey with a non-blank key`() = runTest {
            // ----- Arrange -----
            stubLegacyKey(apiKey = "")
            val dataSource = buildDataSource()

            every {
                Cipher.getInstance("AES/GCM/NoPadding")
            } returns encryptCipher

            // ----- Act -----
            dataSource.updateApiKey(key = "new-key")

            // ----- Assert -----
            dataSource.apiKey.test {
                awaitItem() shouldBe "new-key"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `emits null immediately after clear`() = runTest {
            // ----- Arrange -----
            stubLegacyKey(apiKey = "")
            val dataSource = buildDataSource()

            every {
                Cipher.getInstance("AES/GCM/NoPadding")
            } returns encryptCipher
            dataSource.updateApiKey(key = "existing-key")

            // ----- Act -----
            dataSource.clear()

            // ----- Assert -----
            dataSource.apiKey.test {
                awaitItem() shouldBe null
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
