package nl.rhaydus.softcover.core.preferences.data.datasource

import androidx.datastore.core.DataStore
import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.platform.SecureStorage
import nl.rhaydus.softcover.core.preferences.data.datastore.AppSettingsDataStore
import nl.rhaydus.softcover.core.preferences.data.model.AppSettingsEntity
import nl.rhaydus.softcover.core.preferences.data.security.LegacySecureApiKeyStorage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private const val API_KEY = "api_key"

class ApiKeyLocalDataSourceImplTest {
    private lateinit var dataStore: DataStore<AppSettingsEntity>
    private lateinit var secureStorage: SecureStorage
    private lateinit var legacySecureStorage: LegacySecureApiKeyStorage
    private lateinit var dispatchers: AppDispatchers

    // AppSettingsDataStore is an inline value class — reassigned each setUp via a var.
    private var appSettingsDataStore: AppSettingsDataStore = AppSettingsDataStore(store = mockk(relaxed = true))

    // Backs the stateful secureStorage stub below so write() is visible to a later read().
    private var storedKey: String? = null

    @BeforeEach
    fun setUp() {
        dataStore = mockk(relaxed = true)
        appSettingsDataStore = AppSettingsDataStore(store = dataStore)
        storedKey = null

        secureStorage = mockk(relaxed = true)

        coEvery {
            secureStorage.read(API_KEY)
        } answers {
            storedKey
        }

        coEvery {
            secureStorage.write(
                API_KEY,
                any(),
            )
        } answers {
            storedKey = secondArg()
        }

        coEvery {
            secureStorage.delete(API_KEY)
        } answers {
            storedKey = null
        }

        legacySecureStorage = mockk(relaxed = true)

        coEvery {
            legacySecureStorage.read()
        } returns null

        val dispatcher = UnconfinedTestDispatcher()
        dispatchers = AppDispatchers(
            main = dispatcher,
            io = dispatcher,
            default = dispatcher,
        )
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

    private fun buildDataSource(
        legacySecureStorage: LegacySecureApiKeyStorage? = this.legacySecureStorage,
    ): ApiKeyLocalDataSourceImpl =
        ApiKeyLocalDataSourceImpl(
            secureStorage = secureStorage,
            appSettingsDataStore = appSettingsDataStore,
            dispatchers = dispatchers,
            legacySecureStorage = legacySecureStorage,
        )

    @Nested
    inner class Construction {
        @Test
        fun `foundation store already has a key — never reads the legacy secure store`() = runTest {
            // ----- Arrange -----
            storedKey = "existing-key"

            // ----- Act -----
            buildDataSource()

            // ----- Assert -----
            coVerify(exactly = 0) { legacySecureStorage.read() }
        }

        @Test
        fun `foundation store already has a key — reads the plain-text settings field but writes nothing`() = runTest {
            // ----- Arrange -----
            storedKey = "existing-key"
            stubLegacyKey(apiKey = "")

            // ----- Act -----
            buildDataSource()

            // ----- Assert -----
            verify(exactly = 1) { dataStore.data }
            coVerify(exactly = 0) {
                secureStorage.write(
                    any(),
                    any(),
                )
            }
            coVerify(exactly = 0) { legacySecureStorage.read() }
        }

        @Test
        fun `foundation store already has a key and the plain-text field is non-blank — clears the residue`() = runTest {
            // ----- Arrange -----
            storedKey = "existing-key"
            stubLegacyKey(apiKey = "leftover-plaintext-key")

            // ----- Act -----
            buildDataSource()

            // ----- Assert -----
            coVerify(exactly = 1) { dataStore.updateData(any()) }
        }

        @Test
        fun `foundation store already has a key and the plain-text field is blank — does not touch the store`() = runTest {
            // ----- Arrange -----
            storedKey = "existing-key"
            stubLegacyKey(apiKey = "")

            // ----- Act -----
            buildDataSource()

            // ----- Assert -----
            coVerify(exactly = 0) { dataStore.updateData(any()) }
        }

        @Test
        fun `both secure stores are empty and the secure write throws — does not clear the plain-text field`() = runTest {
            // ----- Arrange -----
            coEvery {
                legacySecureStorage.read()
            } returns "legacy-secure-key"

            coEvery {
                secureStorage.write(
                    API_KEY,
                    any(),
                )
            } throws RuntimeException("write failed")

            stubLegacyKey(apiKey = "")

            // ----- Act -----
            buildDataSource()

            // ----- Assert -----
            coVerify(exactly = 0) { dataStore.updateData(any()) }
        }

        @Test
        fun `clearing the plain-text field throws — construction completes and the flow still emits the stored key`() = runTest {
            // ----- Arrange -----
            storedKey = "existing-key"

            every {
                dataStore.data
            } returns flowOf(AppSettingsEntity(apiKey = "leftover-plaintext-key"))

            coEvery {
                dataStore.updateData(any())
            } throws RuntimeException("disk full")

            // ----- Act -----
            val dataSource = buildDataSource()

            // ----- Assert -----
            dataSource.apiKey.test {
                awaitItem() shouldBe "existing-key"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `foundation store already has a key — flow emits the existing key`() = runTest {
            // ----- Arrange -----
            storedKey = "stored-key"

            // ----- Act -----
            val dataSource = buildDataSource()

            // ----- Assert -----
            dataSource.apiKey.test {
                awaitItem() shouldBe "stored-key"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `legacy secure store has a key — writes it to the foundation store`() = runTest {
            // ----- Arrange -----
            coEvery {
                legacySecureStorage.read()
            } returns "legacy-secure-key"

            // ----- Act -----
            buildDataSource()

            // ----- Assert -----
            coVerify(exactly = 1) {
                secureStorage.write(
                    API_KEY,
                    "legacy-secure-key",
                )
            }
        }

        @Test
        fun `legacy secure store has a key — deletes the legacy copy after a successful write`() = runTest {
            // ----- Arrange -----
            coEvery {
                legacySecureStorage.read()
            } returns "legacy-secure-key"

            // ----- Act -----
            buildDataSource()

            // ----- Assert -----
            coVerify(exactly = 1) { legacySecureStorage.delete() }
        }

        @Test
        fun `legacy secure store has a key but the write fails — does not delete the legacy copy`() = runTest {
            // ----- Arrange -----
            coEvery {
                legacySecureStorage.read()
            } returns "legacy-secure-key"

            coEvery {
                secureStorage.write(
                    API_KEY,
                    any(),
                )
            } throws RuntimeException("write failed")

            // ----- Act -----
            buildDataSource()

            // ----- Assert -----
            coVerify(exactly = 0) { legacySecureStorage.delete() }
        }

        @Test
        fun `both secure stores are empty — writes the plain-text settings key to the foundation store`() = runTest {
            // ----- Arrange -----
            val legacyKey = "legacy-plaintext-api-key"
            stubLegacyKey(apiKey = legacyKey)

            // ----- Act -----
            buildDataSource()

            // ----- Assert -----
            coVerify(exactly = 1) {
                secureStorage.write(
                    API_KEY,
                    legacyKey,
                )
            }
        }

        @Test
        fun `both secure stores are empty — clears the plain-text settings field after writing`() = runTest {
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
        fun `both secure stores are empty — flow emits the migrated key after construction`() = runTest {
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
        fun `skips migration — does not call secureStorage write when legacy key is blank`() = runTest {
            // ----- Arrange -----
            stubLegacyKey(apiKey = "")

            // ----- Act -----
            buildDataSource()

            // ----- Assert -----
            coVerify(exactly = 0) {
                secureStorage.write(
                    any(),
                    any(),
                )
            }
        }

        @Test
        fun `skips migration — does not call updateData when legacy key is blank`() = runTest {
            // ----- Arrange -----
            stubLegacyKey(apiKey = "")

            // ----- Act -----
            buildDataSource()

            // ----- Assert -----
            coVerify(exactly = 0) { dataStore.updateData(any()) }
        }

        @Test
        fun `legacySecureStorage is null — migrates the plain-text settings key without throwing`() = runTest {
            // ----- Arrange -----
            val legacyKey = "legacy-plaintext-api-key"
            stubLegacyKey(apiKey = legacyKey)

            // ----- Act -----
            val dataSource = buildDataSource(legacySecureStorage = null)

            // ----- Assert -----
            dataSource.apiKey.test {
                awaitItem() shouldBe legacyKey
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `legacy secure store read throws — falls through to the plain-text settings leg instead of propagating`() = runTest {
            // ----- Arrange -----
            val legacyKey = "legacy-plaintext-api-key"
            stubLegacyKey(apiKey = legacyKey)

            coEvery {
                legacySecureStorage.read()
            } throws RuntimeException("keystore unavailable")

            // ----- Act -----
            val dataSource = buildDataSource()

            // ----- Assert -----
            dataSource.apiKey.test {
                awaitItem() shouldBe legacyKey
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `nothing to migrate — flow emits null`() = runTest {
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
        fun `non-blank key — emits the new key on the flow`() = runTest {
            // ----- Arrange -----
            stubLegacyKey(apiKey = "")
            val dataSource = buildDataSource()
            val newKey = "my-new-api-key"

            // ----- Act -----
            dataSource.updateApiKey(key = newKey)

            // ----- Assert -----
            dataSource.apiKey.test {
                awaitItem() shouldBe newKey
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `non-blank key — calls secureStorage write with the key`() = runTest {
            // ----- Arrange -----
            stubLegacyKey(apiKey = "")
            val dataSource = buildDataSource()
            val newKey = "my-new-api-key"

            // ----- Act -----
            dataSource.updateApiKey(key = newKey)

            // ----- Assert -----
            coVerify(exactly = 1) {
                secureStorage.write(
                    API_KEY,
                    newKey,
                )
            }
        }

        @Test
        fun `blank key — emits null on the flow`() = runTest {
            // ----- Arrange -----
            stubLegacyKey(apiKey = "")
            val dataSource = buildDataSource()
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
        fun `blank key — calls secureStorage delete`() = runTest {
            // ----- Arrange -----
            stubLegacyKey(apiKey = "")
            val dataSource = buildDataSource()

            // ----- Act -----
            dataSource.updateApiKey(key = "")

            // ----- Assert -----
            coVerify(exactly = 1) { secureStorage.delete(API_KEY) }
        }

        @Test
        fun `whitespace-only key — emits null on the flow`() = runTest {
            // ----- Arrange -----
            stubLegacyKey(apiKey = "")
            val dataSource = buildDataSource()
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
        fun `whitespace-only key — calls secureStorage delete`() = runTest {
            // ----- Arrange -----
            stubLegacyKey(apiKey = "")
            val dataSource = buildDataSource()

            // ----- Act -----
            dataSource.updateApiKey(key = "   ")

            // ----- Assert -----
            coVerify(exactly = 1) { secureStorage.delete(API_KEY) }
        }
    }

    @Nested
    inner class Clear {
        @Test
        fun `emits null on the flow after clear`() = runTest {
            // ----- Arrange -----
            stubLegacyKey(apiKey = "")
            val dataSource = buildDataSource()
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
        fun `calls secureStorage delete after clear`() = runTest {
            // ----- Arrange -----
            stubLegacyKey(apiKey = "")
            val dataSource = buildDataSource()

            // ----- Act -----
            dataSource.clear()

            // ----- Assert -----
            coVerify(atLeast = 1) { secureStorage.delete(API_KEY) }
        }
    }
}
