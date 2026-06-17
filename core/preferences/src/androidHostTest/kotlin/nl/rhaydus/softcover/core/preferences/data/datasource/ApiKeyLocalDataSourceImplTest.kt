package nl.rhaydus.softcover.core.preferences.data.datasource

import androidx.datastore.core.DataStore
import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.preferences.data.datastore.AppSettingsDataStore
import nl.rhaydus.softcover.core.preferences.data.model.AppSettingsEntity
import nl.rhaydus.softcover.core.preferences.data.security.SecureApiKeyStorage
import nl.rhaydus.ui.common.AppDispatchers
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ApiKeyLocalDataSourceImplTest {
    private lateinit var dataStore: DataStore<AppSettingsEntity>
    private lateinit var secureStorage: SecureApiKeyStorage
    private lateinit var dispatchers: AppDispatchers

    // AppSettingsDataStore is an inline value class — reassigned each setUp via a var.
    private var appSettingsDataStore: AppSettingsDataStore = AppSettingsDataStore(store = mockk(relaxed = true))

    @BeforeEach
    fun setUp() {
        dataStore = mockk(relaxed = true)
        appSettingsDataStore = AppSettingsDataStore(store = dataStore)
        secureStorage = mockk(relaxed = true)

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
        every { dataStore.data } returns flowOf(entity)
        coEvery { dataStore.updateData(any()) } coAnswers {
            val updater = firstArg<suspend (AppSettingsEntity) -> AppSettingsEntity>()
            updater(entity)
        }
    }

    private fun buildDataSource(): ApiKeyLocalDataSourceImpl =
        ApiKeyLocalDataSourceImpl(
            secureStorage = secureStorage,
            appSettingsDataStore = appSettingsDataStore,
            dispatchers = dispatchers,
        )

    @Nested
    inner class Construction {
        @Test
        fun `migrates legacy key — calls secureStorage write with the legacy key`() = runTest {
            // ----- Arrange -----
            val legacyKey = "legacy-plaintext-api-key"
            stubLegacyKey(apiKey = legacyKey)
            coEvery { secureStorage.read() } returns legacyKey

            // ----- Act -----
            buildDataSource()

            // ----- Assert -----
            coVerify(exactly = 1) { secureStorage.write(legacyKey) }
        }

        @Test
        fun `migrates legacy key — clears the apiKey field in the data store after writing`() = runTest {
            // ----- Arrange -----
            val legacyKey = "legacy-plaintext-api-key"
            val originalEntity = AppSettingsEntity(apiKey = legacyKey)
            var capturedResult: AppSettingsEntity? = null

            every { dataStore.data } returns flowOf(originalEntity)
            coEvery { dataStore.updateData(any()) } coAnswers {
                val updater = firstArg<suspend (AppSettingsEntity) -> AppSettingsEntity>()
                capturedResult = updater(originalEntity)
                capturedResult!!
            }
            coEvery { secureStorage.read() } returns legacyKey

            // ----- Act -----
            buildDataSource()

            // ----- Assert -----
            capturedResult?.apiKey shouldBe ""
        }

        @Test
        fun `migrates legacy key — flow emits the migrated key after construction`() = runTest {
            // ----- Arrange -----
            val legacyKey = "legacy-plaintext-api-key"
            stubLegacyKey(apiKey = legacyKey)
            coEvery { secureStorage.read() } returns legacyKey

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
            coEvery { secureStorage.read() } returns null

            // ----- Act -----
            buildDataSource()

            // ----- Assert -----
            coVerify(exactly = 0) { secureStorage.write(any()) }
        }

        @Test
        fun `skips migration — does not call updateData when legacy key is blank`() = runTest {
            // ----- Arrange -----
            stubLegacyKey(apiKey = "")
            coEvery { secureStorage.read() } returns null

            // ----- Act -----
            buildDataSource()

            // ----- Assert -----
            coVerify(exactly = 0) { dataStore.updateData(any()) }
        }

        @Test
        fun `flow initial value comes from secureStorage read when a key is stored`() = runTest {
            // ----- Arrange -----
            stubLegacyKey(apiKey = "")
            coEvery { secureStorage.read() } returns "stored-key"

            // ----- Act -----
            val dataSource = buildDataSource()

            // ----- Assert -----
            dataSource.apiKey.test {
                awaitItem() shouldBe "stored-key"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `flow initial value is null when secureStorage read returns null`() = runTest {
            // ----- Arrange -----
            stubLegacyKey(apiKey = "")
            coEvery { secureStorage.read() } returns null

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
            coEvery { secureStorage.read() } returns null
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
            coEvery { secureStorage.read() } returns null
            val dataSource = buildDataSource()
            val newKey = "my-new-api-key"

            // ----- Act -----
            dataSource.updateApiKey(key = newKey)

            // ----- Assert -----
            coVerify(exactly = 1) { secureStorage.write(newKey) }
        }

        @Test
        fun `blank key — emits null on the flow`() = runTest {
            // ----- Arrange -----
            stubLegacyKey(apiKey = "")
            coEvery { secureStorage.read() } returns null
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
            coEvery { secureStorage.read() } returns null
            val dataSource = buildDataSource()

            // ----- Act -----
            dataSource.updateApiKey(key = "")

            // ----- Assert -----
            coVerify(exactly = 1) { secureStorage.delete() }
        }

        @Test
        fun `whitespace-only key — emits null on the flow`() = runTest {
            // ----- Arrange -----
            stubLegacyKey(apiKey = "")
            coEvery { secureStorage.read() } returns null
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
            coEvery { secureStorage.read() } returns null
            val dataSource = buildDataSource()

            // ----- Act -----
            dataSource.updateApiKey(key = "   ")

            // ----- Assert -----
            coVerify(exactly = 1) { secureStorage.delete() }
        }
    }

    @Nested
    inner class Clear {
        @Test
        fun `emits null on the flow after clear`() = runTest {
            // ----- Arrange -----
            stubLegacyKey(apiKey = "")
            coEvery { secureStorage.read() } returns null
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
            coEvery { secureStorage.read() } returns null
            val dataSource = buildDataSource()

            // ----- Act -----
            dataSource.clear()

            // ----- Assert -----
            coVerify(atLeast = 1) { secureStorage.delete() }
        }
    }
}
