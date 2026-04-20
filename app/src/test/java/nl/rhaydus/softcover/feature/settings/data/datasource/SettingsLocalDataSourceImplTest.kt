package nl.rhaydus.softcover.feature.settings.data.datasource

import androidx.datastore.core.DataStore
import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.settings.data.datastore.AppSettingsDataStore
import nl.rhaydus.softcover.feature.settings.data.model.AppSettingsEntity
import nl.rhaydus.softcover.feature.settings.data.model.ThemeConfigurationEntity
import nl.rhaydus.softcover.feature.settings.domain.model.BottomBarStyle
import nl.rhaydus.softcover.feature.settings.domain.model.DateStyle
import nl.rhaydus.softcover.feature.settings.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.feature.settings.domain.model.ThemeConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SettingsLocalDataSourceImplTest {

    private lateinit var dataStore: DataStore<AppSettingsEntity>
    private lateinit var apiKeyLocalDataSource: ApiKeyLocalDataSource
    private lateinit var dataSource: SettingsLocalDataSourceImpl

    @BeforeEach
    fun setUp() {
        dataStore = mockk(relaxed = true)
        apiKeyLocalDataSource = mockk(relaxed = true)
        dataSource = SettingsLocalDataSourceImpl(
            appSettingsDataStore = AppSettingsDataStore(store = dataStore),
            apiKeyLocalDataSource = apiKeyLocalDataSource,
        )
    }

    private fun stubEntity(
        apiKey: String = "",
        userId: Int = -1,
        dateStyle: DateStyle = DateStyle.DAY_MONTH_YEAR,
        themeConfig: ThemeConfigurationEntity = ThemeConfigurationEntity(),
        libraryGridLayout: LibraryGridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
    ): AppSettingsEntity = AppSettingsEntity(
        apiKey = apiKey,
        userId = userId,
        dateStyle = dateStyle,
        themeConfig = themeConfig,
        libraryGridLayout = libraryGridLayout,
    )

    @Nested
    inner class DateStyleFlow {

        @Test
        fun `emits the dateStyle field from each entity in the data store flow`() = runTest {
            // ----- Arrange -----
            val entity = stubEntity(dateStyle = DateStyle.MONTH_DAY_YEAR)

            every {
                dataStore.data
            } returns flowOf(entity)

            val freshDataSource = SettingsLocalDataSourceImpl(
                appSettingsDataStore = AppSettingsDataStore(store = dataStore),
                apiKeyLocalDataSource = apiKeyLocalDataSource,
            )

            // ----- Act & Assert -----
            freshDataSource.dateStyle.test {
                awaitItem() shouldBe DateStyle.MONTH_DAY_YEAR
                awaitComplete()
            }
        }

        @Test
        fun `deduplicates identical consecutive dateStyle values`() = runTest {
            // ----- Arrange -----
            val entity1 = stubEntity(dateStyle = DateStyle.DAY_MONTH_YEAR)
            val entity2 = stubEntity(dateStyle = DateStyle.DAY_MONTH_YEAR)

            every {
                dataStore.data
            } returns flowOf(entity1, entity2)

            val freshDataSource = SettingsLocalDataSourceImpl(
                appSettingsDataStore = AppSettingsDataStore(store = dataStore),
                apiKeyLocalDataSource = apiKeyLocalDataSource,
            )

            // ----- Act & Assert -----
            freshDataSource.dateStyle.test {
                awaitItem() shouldBe DateStyle.DAY_MONTH_YEAR
                awaitComplete()
            }
        }

        @Test
        fun `emits distinct consecutive dateStyle values`() = runTest {
            // ----- Arrange -----
            val entity1 = stubEntity(dateStyle = DateStyle.DAY_MONTH_YEAR)
            val entity2 = stubEntity(dateStyle = DateStyle.YEAR_MONTH_DAY)

            every {
                dataStore.data
            } returns flowOf(entity1, entity2)

            val freshDataSource = SettingsLocalDataSourceImpl(
                appSettingsDataStore = AppSettingsDataStore(store = dataStore),
                apiKeyLocalDataSource = apiKeyLocalDataSource,
            )

            // ----- Act & Assert -----
            freshDataSource.dateStyle.test {
                awaitItem() shouldBe DateStyle.DAY_MONTH_YEAR
                awaitItem() shouldBe DateStyle.YEAR_MONTH_DAY
                awaitComplete()
            }
        }
    }

    @Nested
    inner class SetDateStyle {

        @Test
        fun `calls updateData on the data store`() = runTest {
            // ----- Arrange -----
            val style = DateStyle.YEAR_MONTH_DAY
            val existingEntity = stubEntity(dateStyle = DateStyle.DAY_MONTH_YEAR)

            coEvery {
                dataStore.updateData(any())
            } coAnswers {
                val updater = firstArg<suspend (AppSettingsEntity) -> AppSettingsEntity>()
                updater(existingEntity)
            }

            // ----- Act -----
            dataSource.setDateStyle(style = style)

            // ----- Assert -----
            coVerify {
                dataStore.updateData(any())
            }
        }

        @Test
        fun `update lambda sets dateStyle to the given style`() = runTest {
            // ----- Arrange -----
            val style = DateStyle.MONTH_DAY_YEAR
            val existingEntity = stubEntity(dateStyle = DateStyle.DAY_MONTH_YEAR)
            var capturedResult: AppSettingsEntity? = null

            coEvery {
                dataStore.updateData(any())
            } coAnswers {
                val updater = firstArg<suspend (AppSettingsEntity) -> AppSettingsEntity>()
                capturedResult = updater(existingEntity)
                capturedResult!!
            }

            // ----- Act -----
            dataSource.setDateStyle(style = style)

            // ----- Assert -----
            capturedResult?.dateStyle shouldBe style
        }
    }

    @Nested
    inner class UpdateApiKey {

        @Test
        fun `delegates to apiKeyLocalDataSource with the given key`() = runTest {
            // ----- Arrange -----
            val key = "new-api-key"

            // ----- Act -----
            dataSource.updateApiKey(key = key)

            // ----- Assert -----
            coVerify { apiKeyLocalDataSource.updateApiKey(key = key) }
        }

        @Test
        fun `delegates to apiKeyLocalDataSource with empty string when clearing`() = runTest {
            // ----- Act -----
            dataSource.updateApiKey(key = "")

            // ----- Assert -----
            coVerify { apiKeyLocalDataSource.updateApiKey(key = "") }
        }

        @Test
        fun `does not call dataStore updateData for api key updates`() = runTest {
            // ----- Act -----
            dataSource.updateApiKey(key = "any-key")

            // ----- Assert -----
            coVerify(exactly = 0) { dataStore.updateData(any()) }
        }
    }

    @Nested
    inner class GetUserId {

        @Test
        fun `emits the userId field from each entity in the data store flow`() = runTest {
            // ----- Arrange -----
            val entity = stubEntity(userId = 42)

            every {
                dataStore.data
            } returns flowOf(entity)

            // ----- Act & Assert -----
            dataSource.getUserId().test {
                awaitItem() shouldBe 42
                awaitComplete()
            }
        }

        @Test
        fun `deduplicates identical consecutive userId values`() = runTest {
            // ----- Arrange -----
            val entity1 = stubEntity(userId = 5)
            val entity2 = stubEntity(userId = 5)

            every {
                dataStore.data
            } returns flowOf(entity1, entity2)

            // ----- Act & Assert -----
            dataSource.getUserId().test {
                awaitItem() shouldBe 5
                awaitComplete()
            }
        }

        @Test
        fun `emits distinct consecutive userId values`() = runTest {
            // ----- Arrange -----
            val entity1 = stubEntity(userId = 1)
            val entity2 = stubEntity(userId = 2)

            every {
                dataStore.data
            } returns flowOf(entity1, entity2)

            // ----- Act & Assert -----
            dataSource.getUserId().test {
                awaitItem() shouldBe 1
                awaitItem() shouldBe 2
                awaitComplete()
            }
        }
    }

    @Nested
    inner class GetThemeConfig {

        @Test
        fun `emits the mapped ThemeConfiguration from each entity`() = runTest {
            // ----- Arrange -----
            val entity = stubEntity(themeConfig = ThemeConfigurationEntity(bottomBarStyle = BottomBarStyle.DOCKED))

            every {
                dataStore.data
            } returns flowOf(entity)

            // ----- Act & Assert -----
            dataSource.getThemeConfig().test {
                awaitItem() shouldBe ThemeConfiguration(bottomBarStyle = BottomBarStyle.DOCKED)
                awaitComplete()
            }
        }

        @Test
        fun `deduplicates identical consecutive ThemeConfiguration values`() = runTest {
            // ----- Arrange -----
            val entity1 = stubEntity(themeConfig = ThemeConfigurationEntity(bottomBarStyle = BottomBarStyle.FLOATING))
            val entity2 = stubEntity(themeConfig = ThemeConfigurationEntity(bottomBarStyle = BottomBarStyle.FLOATING))

            every {
                dataStore.data
            } returns flowOf(entity1, entity2)

            // ----- Act & Assert -----
            dataSource.getThemeConfig().test {
                awaitItem() shouldBe ThemeConfiguration(bottomBarStyle = BottomBarStyle.FLOATING)
                awaitComplete()
            }
        }

        @Test
        fun `emits distinct consecutive ThemeConfiguration values`() = runTest {
            // ----- Arrange -----
            val entity1 = stubEntity(themeConfig = ThemeConfigurationEntity(bottomBarStyle = BottomBarStyle.FLOATING))
            val entity2 = stubEntity(themeConfig = ThemeConfigurationEntity(bottomBarStyle = BottomBarStyle.DOCKED))

            every {
                dataStore.data
            } returns flowOf(entity1, entity2)

            // ----- Act & Assert -----
            dataSource.getThemeConfig().test {
                awaitItem() shouldBe ThemeConfiguration(bottomBarStyle = BottomBarStyle.FLOATING)
                awaitItem() shouldBe ThemeConfiguration(bottomBarStyle = BottomBarStyle.DOCKED)
                awaitComplete()
            }
        }
    }

    @Nested
    inner class UpdateUserId {

        @Test
        fun `update lambda sets userId to the given id`() = runTest {
            // ----- Arrange -----
            val existingEntity = stubEntity(userId = -1)
            var capturedResult: AppSettingsEntity? = null

            coEvery {
                dataStore.updateData(any())
            } coAnswers {
                val updater = firstArg<suspend (AppSettingsEntity) -> AppSettingsEntity>()
                capturedResult = updater(existingEntity)
                capturedResult!!
            }

            // ----- Act -----
            dataSource.updateUserId(id = 99)

            // ----- Assert -----
            capturedResult?.userId shouldBe 99
        }

        @Test
        fun `update lambda preserves other fields when updating userId`() = runTest {
            // ----- Arrange -----
            val existingEntity = stubEntity(apiKey = "key-abc", dateStyle = DateStyle.MONTH_DAY_YEAR)
            var capturedResult: AppSettingsEntity? = null

            coEvery {
                dataStore.updateData(any())
            } coAnswers {
                val updater = firstArg<suspend (AppSettingsEntity) -> AppSettingsEntity>()
                capturedResult = updater(existingEntity)
                capturedResult!!
            }

            // ----- Act -----
            dataSource.updateUserId(id = 10)

            // ----- Assert -----
            capturedResult?.apiKey shouldBe "key-abc"
            capturedResult?.dateStyle shouldBe DateStyle.MONTH_DAY_YEAR
        }
    }

    @Nested
    inner class LibraryGridLayoutFlow {

        @Test
        fun `emits the libraryGridLayout field from each entity in the data store flow`() = runTest {
            // ----- Arrange -----
            val entity = stubEntity(libraryGridLayout = LibraryGridLayout.GRID_THREE_COLUMNS)

            every {
                dataStore.data
            } returns flowOf(entity)

            val freshDataSource = SettingsLocalDataSourceImpl(
                appSettingsDataStore = AppSettingsDataStore(store = dataStore),
                apiKeyLocalDataSource = apiKeyLocalDataSource,
            )

            // ----- Act & Assert -----
            freshDataSource.libraryGridLayout.test {
                awaitItem() shouldBe LibraryGridLayout.GRID_THREE_COLUMNS
                awaitComplete()
            }
        }

        @Test
        fun `deduplicates identical consecutive libraryGridLayout values`() = runTest {
            // ----- Arrange -----
            val entity1 = stubEntity(libraryGridLayout = LibraryGridLayout.GRID_TWO_COLUMNS)
            val entity2 = stubEntity(libraryGridLayout = LibraryGridLayout.GRID_TWO_COLUMNS)

            every {
                dataStore.data
            } returns flowOf(entity1, entity2)

            val freshDataSource = SettingsLocalDataSourceImpl(
                appSettingsDataStore = AppSettingsDataStore(store = dataStore),
                apiKeyLocalDataSource = apiKeyLocalDataSource,
            )

            // ----- Act & Assert -----
            freshDataSource.libraryGridLayout.test {
                awaitItem() shouldBe LibraryGridLayout.GRID_TWO_COLUMNS
                awaitComplete()
            }
        }

        @Test
        fun `emits distinct consecutive libraryGridLayout values`() = runTest {
            // ----- Arrange -----
            val entity1 = stubEntity(libraryGridLayout = LibraryGridLayout.LIST_COMPACT)
            val entity2 = stubEntity(libraryGridLayout = LibraryGridLayout.LIST_LARGE)

            every {
                dataStore.data
            } returns flowOf(entity1, entity2)

            val freshDataSource = SettingsLocalDataSourceImpl(
                appSettingsDataStore = AppSettingsDataStore(store = dataStore),
                apiKeyLocalDataSource = apiKeyLocalDataSource,
            )

            // ----- Act & Assert -----
            freshDataSource.libraryGridLayout.test {
                awaitItem() shouldBe LibraryGridLayout.LIST_COMPACT
                awaitItem() shouldBe LibraryGridLayout.LIST_LARGE
                awaitComplete()
            }
        }
    }

    @Nested
    inner class SetLibraryGridLayout {

        @Test
        fun `calls updateData on the data store`() = runTest {
            // ----- Arrange -----
            val layout = LibraryGridLayout.GRID_THREE_COLUMNS
            val existingEntity = stubEntity(libraryGridLayout = LibraryGridLayout.GRID_TWO_COLUMNS)

            coEvery {
                dataStore.updateData(any())
            } coAnswers {
                val updater = firstArg<suspend (AppSettingsEntity) -> AppSettingsEntity>()
                updater(existingEntity)
            }

            // ----- Act -----
            dataSource.setLibraryGridLayout(layout = layout)

            // ----- Assert -----
            coVerify {
                dataStore.updateData(any())
            }
        }

        @Test
        fun `update lambda sets libraryGridLayout to the given layout`() = runTest {
            // ----- Arrange -----
            val layout = LibraryGridLayout.LIST_COMPACT
            val existingEntity = stubEntity(libraryGridLayout = LibraryGridLayout.GRID_TWO_COLUMNS)
            var capturedResult: AppSettingsEntity? = null

            coEvery {
                dataStore.updateData(any())
            } coAnswers {
                val updater = firstArg<suspend (AppSettingsEntity) -> AppSettingsEntity>()
                capturedResult = updater(existingEntity)
                capturedResult!!
            }

            // ----- Act -----
            dataSource.setLibraryGridLayout(layout = layout)

            // ----- Assert -----
            capturedResult?.libraryGridLayout shouldBe layout
        }

        @Test
        fun `update lambda preserves other entity fields when updating libraryGridLayout`() = runTest {
            // ----- Arrange -----
            val existingEntity = stubEntity(
                apiKey = "key-xyz",
                userId = 7,
                dateStyle = DateStyle.MONTH_DAY_YEAR,
                libraryGridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )
            var capturedResult: AppSettingsEntity? = null

            coEvery {
                dataStore.updateData(any())
            } coAnswers {
                val updater = firstArg<suspend (AppSettingsEntity) -> AppSettingsEntity>()
                capturedResult = updater(existingEntity)
                capturedResult!!
            }

            // ----- Act -----
            dataSource.setLibraryGridLayout(layout = LibraryGridLayout.LIST_LARGE)

            // ----- Assert -----
            capturedResult?.apiKey shouldBe "key-xyz"
            capturedResult?.userId shouldBe 7
            capturedResult?.dateStyle shouldBe DateStyle.MONTH_DAY_YEAR
        }
    }

    @Nested
    inner class SetBottomBarStyle {

        @Test
        fun `update lambda sets bottomBarStyle inside themeConfig`() = runTest {
            // ----- Arrange -----
            val existingEntity = stubEntity(
                themeConfig = ThemeConfigurationEntity(bottomBarStyle = BottomBarStyle.FLOATING)
            )
            var capturedResult: AppSettingsEntity? = null

            coEvery {
                dataStore.updateData(any())
            } coAnswers {
                val updater = firstArg<suspend (AppSettingsEntity) -> AppSettingsEntity>()
                capturedResult = updater(existingEntity)
                capturedResult!!
            }

            // ----- Act -----
            dataSource.setBottomBarStyle(style = BottomBarStyle.DOCKED)

            // ----- Assert -----
            capturedResult?.themeConfig?.bottomBarStyle shouldBe BottomBarStyle.DOCKED
        }

        @Test
        fun `update lambda preserves other themeConfig fields`() = runTest {
            // ----- Arrange -----
            val existingEntity = stubEntity(
                themeConfig = ThemeConfigurationEntity(bottomBarStyle = BottomBarStyle.DOCKED)
            )
            var capturedResult: AppSettingsEntity? = null

            coEvery {
                dataStore.updateData(any())
            } coAnswers {
                val updater = firstArg<suspend (AppSettingsEntity) -> AppSettingsEntity>()
                capturedResult = updater(existingEntity)
                capturedResult!!
            }

            // ----- Act -----
            dataSource.setBottomBarStyle(style = BottomBarStyle.FLOATING)

            // ----- Assert -----
            capturedResult?.themeConfig?.bottomBarStyle shouldBe BottomBarStyle.FLOATING
        }

        @Test
        fun `update lambda preserves non-theme entity fields`() = runTest {
            // ----- Arrange -----
            val existingEntity = stubEntity(
                apiKey = "preserve-me",
                userId = 55,
                themeConfig = ThemeConfigurationEntity(bottomBarStyle = BottomBarStyle.FLOATING),
            )
            var capturedResult: AppSettingsEntity? = null

            coEvery {
                dataStore.updateData(any())
            } coAnswers {
                val updater = firstArg<suspend (AppSettingsEntity) -> AppSettingsEntity>()
                capturedResult = updater(existingEntity)
                capturedResult!!
            }

            // ----- Act -----
            dataSource.setBottomBarStyle(style = BottomBarStyle.DOCKED)

            // ----- Assert -----
            capturedResult?.apiKey shouldBe "preserve-me"
            capturedResult?.userId shouldBe 55
        }
    }
}
