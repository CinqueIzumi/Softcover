package nl.rhaydus.softcover.core.preferences.data.datasource

import androidx.datastore.core.DataStore
import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.BottomBarStyle
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.ProgressUnit
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.preferences.data.datastore.AppSettingsDataStore
import nl.rhaydus.softcover.core.preferences.data.model.AppSettingsEntity
import nl.rhaydus.softcover.core.preferences.data.model.ThemeConfigurationEntity

class SettingsLocalDataSourceImplTest {
    private lateinit var dataStore: DataStore<AppSettingsEntity>
    private lateinit var apiKeyLocalDataSource: ApiKeyLocalDataSource
    private lateinit var dataSource: SettingsLocalDataSourceImpl

    @BeforeEach
    fun setUp() {
        dataStore = mockk(relaxed = true)
        apiKeyLocalDataSource = mockk(relaxed = true)
        dataSource =
            SettingsLocalDataSourceImpl(
                appSettingsDataStore = AppSettingsDataStore(
                    store = dataStore,
                ),
                apiKeyLocalDataSource = apiKeyLocalDataSource,
            )
    }

    private fun stubEntity(
        apiKey: String = "",
        userId: Int = -1,
        dateStyle: DateStyle = DateStyle.DAY_MONTH_YEAR,
        themeConfig: ThemeConfigurationEntity = ThemeConfigurationEntity(),
        libraryGridLayout: LibraryGridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
    ): AppSettingsEntity =
        AppSettingsEntity(
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

            val freshDataSource =
                SettingsLocalDataSourceImpl(
                    appSettingsDataStore = AppSettingsDataStore(
                        store = dataStore,
                    ),
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
            } returns flowOf(
                entity1,
                entity2,
            )

            val freshDataSource =
                SettingsLocalDataSourceImpl(
                    appSettingsDataStore = AppSettingsDataStore(
                        store = dataStore,
                    ),
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
            } returns flowOf(
                entity1,
                entity2,
            )

            val freshDataSource =
                SettingsLocalDataSourceImpl(
                    appSettingsDataStore = AppSettingsDataStore(
                        store = dataStore,
                    ),
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
            } returns flowOf(
                entity1,
                entity2,
            )

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
            } returns flowOf(
                entity1,
                entity2,
            )

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
            val entity = stubEntity(themeConfig = ThemeConfigurationEntity(
                bottomBarStyle = BottomBarStyle.DOCKED,
            ),
            )

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
            val entity1 = stubEntity(themeConfig = ThemeConfigurationEntity(
                bottomBarStyle = BottomBarStyle.FLOATING,
            ),
            )
            val entity2 = stubEntity(themeConfig = ThemeConfigurationEntity(
                bottomBarStyle = BottomBarStyle.FLOATING,
            ),
            )

            every {
                dataStore.data
            } returns flowOf(
                entity1,
                entity2,
            )

            // ----- Act & Assert -----
            dataSource.getThemeConfig().test {
                awaitItem() shouldBe ThemeConfiguration(bottomBarStyle = BottomBarStyle.FLOATING)
                awaitComplete()
            }
        }

        @Test
        fun `emits distinct consecutive ThemeConfiguration values`() = runTest {
            // ----- Arrange -----
            val entity1 = stubEntity(themeConfig = ThemeConfigurationEntity(
                bottomBarStyle = BottomBarStyle.FLOATING,
            ),
            )
            val entity2 = stubEntity(themeConfig = ThemeConfigurationEntity(
                bottomBarStyle = BottomBarStyle.DOCKED,
            ),
            )

            every {
                dataStore.data
            } returns flowOf(
                entity1,
                entity2,
            )

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
            val existingEntity = stubEntity(
                apiKey = "key-abc",
                dateStyle = DateStyle.MONTH_DAY_YEAR,
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

            val freshDataSource =
                SettingsLocalDataSourceImpl(
                    appSettingsDataStore = AppSettingsDataStore(
                        store = dataStore,
                    ),
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
            } returns flowOf(
                entity1,
                entity2,
            )

            val freshDataSource =
                SettingsLocalDataSourceImpl(
                    appSettingsDataStore = AppSettingsDataStore(
                        store = dataStore,
                    ),
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
            } returns flowOf(
                entity1,
                entity2,
            )

            val freshDataSource =
                SettingsLocalDataSourceImpl(
                    appSettingsDataStore = AppSettingsDataStore(
                        store = dataStore,
                    ),
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
    inner class EnabledStatusCodesFlow {
        @Test
        fun `emits the enabledStatusCodes field from the data store flow`() = runTest {
            // ----- Arrange -----
            val entity =
                AppSettingsEntity(
                    enabledStatusCodes = setOf(1, 3, 5),
                )

            every {
                dataStore.data
            } returns flowOf(entity)

            val freshDataSource =
                SettingsLocalDataSourceImpl(
                    appSettingsDataStore = AppSettingsDataStore(
                        store = dataStore,
                    ),
                    apiKeyLocalDataSource = apiKeyLocalDataSource,
                )

            // ----- Act & Assert -----
            freshDataSource.enabledStatusCodes.test {
                awaitItem() shouldBe setOf(1, 3, 5)
                awaitComplete()
            }
        }

        @Test
        fun `deduplicates identical consecutive enabledStatusCodes values`() = runTest {
            // ----- Arrange -----
            val entity1 =
                AppSettingsEntity(
                    enabledStatusCodes = setOf(2),
                )
            val entity2 =
                AppSettingsEntity(
                    enabledStatusCodes = setOf(2),
                )

            every {
                dataStore.data
            } returns flowOf(
                entity1,
                entity2,
            )

            val freshDataSource =
                SettingsLocalDataSourceImpl(
                    appSettingsDataStore = AppSettingsDataStore(
                        store = dataStore,
                    ),
                    apiKeyLocalDataSource = apiKeyLocalDataSource,
                )

            // ----- Act & Assert -----
            freshDataSource.enabledStatusCodes.test {
                awaitItem() shouldBe setOf(2)
                awaitComplete()
            }
        }

        @Test
        fun `emits distinct consecutive enabledStatusCodes values`() = runTest {
            // ----- Arrange -----
            val entity1 =
                AppSettingsEntity(
                    enabledStatusCodes = setOf(1),
                )
            val entity2 =
                AppSettingsEntity(
                    enabledStatusCodes = setOf(1, 3),
                )

            every {
                dataStore.data
            } returns flowOf(
                entity1,
                entity2,
            )

            val freshDataSource =
                SettingsLocalDataSourceImpl(
                    appSettingsDataStore = AppSettingsDataStore(
                        store = dataStore,
                    ),
                    apiKeyLocalDataSource = apiKeyLocalDataSource,
                )

            // ----- Act & Assert -----
            freshDataSource.enabledStatusCodes.test {
                awaitItem() shouldBe setOf(1)
                awaitItem() shouldBe setOf(1, 3)
                awaitComplete()
            }
        }
    }

    @Nested
    inner class EnabledListIdsFlow {
        @Test
        fun `emits the enabledListIds field from the data store flow`() = runTest {
            // ----- Arrange -----
            val entity =
                AppSettingsEntity(
                    enabledListIds = setOf(10, 20),
                )

            every {
                dataStore.data
            } returns flowOf(entity)

            val freshDataSource =
                SettingsLocalDataSourceImpl(
                    appSettingsDataStore = AppSettingsDataStore(
                        store = dataStore,
                    ),
                    apiKeyLocalDataSource = apiKeyLocalDataSource,
                )

            // ----- Act & Assert -----
            freshDataSource.enabledListIds.test {
                awaitItem() shouldBe setOf(10, 20)
                awaitComplete()
            }
        }

        @Test
        fun `deduplicates identical consecutive enabledListIds values`() = runTest {
            // ----- Arrange -----
            val entity1 =
                AppSettingsEntity(
                    enabledListIds = setOf(7),
                )
            val entity2 =
                AppSettingsEntity(
                    enabledListIds = setOf(7),
                )

            every {
                dataStore.data
            } returns flowOf(
                entity1,
                entity2,
            )

            val freshDataSource =
                SettingsLocalDataSourceImpl(
                    appSettingsDataStore = AppSettingsDataStore(
                        store = dataStore,
                    ),
                    apiKeyLocalDataSource = apiKeyLocalDataSource,
                )

            // ----- Act & Assert -----
            freshDataSource.enabledListIds.test {
                awaitItem() shouldBe setOf(7)
                awaitComplete()
            }
        }
    }

    @Nested
    inner class ListDefaultsSeededFlow {
        @Test
        fun `emits the listDefaultsSeeded field from the data store flow`() = runTest {
            // ----- Arrange -----
            val entity =
                AppSettingsEntity(
                    listDefaultsSeeded = true,
                )

            every {
                dataStore.data
            } returns flowOf(entity)

            val freshDataSource =
                SettingsLocalDataSourceImpl(
                    appSettingsDataStore = AppSettingsDataStore(
                        store = dataStore,
                    ),
                    apiKeyLocalDataSource = apiKeyLocalDataSource,
                )

            // ----- Act & Assert -----
            freshDataSource.listDefaultsSeeded.test {
                awaitItem() shouldBe true
                awaitComplete()
            }
        }

        @Test
        fun `emits false when listDefaultsSeeded is not set`() = runTest {
            // ----- Arrange -----
            val entity =
                AppSettingsEntity(
                    listDefaultsSeeded = false,
                )

            every {
                dataStore.data
            } returns flowOf(entity)

            val freshDataSource =
                SettingsLocalDataSourceImpl(
                    appSettingsDataStore = AppSettingsDataStore(
                        store = dataStore,
                    ),
                    apiKeyLocalDataSource = apiKeyLocalDataSource,
                )

            // ----- Act & Assert -----
            freshDataSource.listDefaultsSeeded.test {
                awaitItem() shouldBe false
                awaitComplete()
            }
        }
    }

    @Nested
    inner class SeedEnabledListIds {
        @Test
        fun `update lambda merges new ids into existing enabledListIds`() = runTest {
            // ----- Arrange -----
            val existingEntity =
                AppSettingsEntity(
                    enabledListIds = setOf(1),
                    listDefaultsSeeded = false,
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
            dataSource.seedEnabledListIds(ids = setOf(2, 3))

            // ----- Assert -----
            capturedResult?.enabledListIds shouldBe setOf(1, 2, 3)
        }

        @Test
        fun `update lambda sets listDefaultsSeeded to true`() = runTest {
            // ----- Arrange -----
            val existingEntity =
                AppSettingsEntity(
                    listDefaultsSeeded = false,
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
            dataSource.seedEnabledListIds(ids = setOf(5))

            // ----- Assert -----
            capturedResult?.listDefaultsSeeded shouldBe true
        }

        @Test
        fun `seeding empty ids still marks listDefaultsSeeded as true`() = runTest {
            // ----- Arrange -----
            val existingEntity =
                AppSettingsEntity(
                    enabledListIds = setOf(99),
                    listDefaultsSeeded = false,
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
            dataSource.seedEnabledListIds(ids = emptySet())

            // ----- Assert -----
            capturedResult?.listDefaultsSeeded shouldBe true
            capturedResult?.enabledListIds shouldBe setOf(99)
        }
    }

    @Nested
    inner class SetEnabledStatusCodes {
        @Test
        fun `update lambda replaces enabledStatusCodes with the given set`() = runTest {
            // ----- Arrange -----
            val existingEntity =
                AppSettingsEntity(
                    enabledStatusCodes = setOf(1, 3),
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
            dataSource.setEnabledStatusCodes(codes = setOf(5, 7))

            // ----- Assert -----
            capturedResult?.enabledStatusCodes shouldBe setOf(5, 7)
        }

        @Test
        fun `update lambda clears enabledStatusCodes when given an empty set`() = runTest {
            // ----- Arrange -----
            val existingEntity =
                AppSettingsEntity(
                    enabledStatusCodes = setOf(1, 3, 5),
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
            dataSource.setEnabledStatusCodes(codes = emptySet())

            // ----- Assert -----
            capturedResult?.enabledStatusCodes shouldBe emptySet()
        }

        @Test
        fun `update lambda preserves other entity fields`() = runTest {
            // ----- Arrange -----
            val existingEntity =
                AppSettingsEntity(
                    userId = 42,
                    enabledStatusCodes = setOf(1),
                    enabledListIds = setOf(99),
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
            dataSource.setEnabledStatusCodes(codes = setOf(3))

            // ----- Assert -----
            capturedResult?.userId shouldBe 42
            capturedResult?.enabledListIds shouldBe setOf(99)
        }
    }

    @Nested
    inner class SetEnabledListIds {
        @Test
        fun `update lambda replaces enabledListIds with the given set`() = runTest {
            // ----- Arrange -----
            val existingEntity =
                AppSettingsEntity(
                    enabledListIds = setOf(10, 20),
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
            dataSource.setEnabledListIds(ids = setOf(30, 40))

            // ----- Assert -----
            capturedResult?.enabledListIds shouldBe setOf(30, 40)
        }

        @Test
        fun `update lambda clears enabledListIds when given an empty set`() = runTest {
            // ----- Arrange -----
            val existingEntity =
                AppSettingsEntity(
                    enabledListIds = setOf(10, 20),
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
            dataSource.setEnabledListIds(ids = emptySet())

            // ----- Assert -----
            capturedResult?.enabledListIds shouldBe emptySet()
        }

        @Test
        fun `update lambda preserves other entity fields`() = runTest {
            // ----- Arrange -----
            val existingEntity =
                AppSettingsEntity(
                    userId = 77,
                    enabledStatusCodes = setOf(1, 3),
                    enabledListIds = setOf(5),
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
            dataSource.setEnabledListIds(ids = setOf(100))

            // ----- Assert -----
            capturedResult?.userId shouldBe 77
            capturedResult?.enabledStatusCodes shouldBe setOf(1, 3)
        }
    }

    @Nested
    inner class ResetAllSettings {
        @Test
        fun `calls updateData on the data store`() = runTest {
            // ----- Arrange -----
            val existingEntity = stubEntity(userId = 42)

            coEvery {
                dataStore.updateData(any())
            } coAnswers {
                val updater = firstArg<suspend (AppSettingsEntity) -> AppSettingsEntity>()
                updater(existingEntity)
            }

            // ----- Act -----
            dataSource.resetAllSettings()

            // ----- Assert -----
            coVerify { dataStore.updateData(any()) }
        }

        @Test
        fun `update lambda returns a fresh AppSettingsEntity with default userId`() = runTest {
            // ----- Arrange -----
            val existingEntity = stubEntity(userId = 42)
            var capturedResult: AppSettingsEntity? = null

            coEvery {
                dataStore.updateData(any())
            } coAnswers {
                val updater = firstArg<suspend (AppSettingsEntity) -> AppSettingsEntity>()
                capturedResult = updater(existingEntity)
                capturedResult!!
            }

            // ----- Act -----
            dataSource.resetAllSettings()

            // ----- Assert -----
            capturedResult?.userId shouldBe -1
        }

        @Test
        fun `update lambda discards all non-default entity state`() = runTest {
            // ----- Arrange -----
            val existingEntity = stubEntity(
                userId = 99,
                dateStyle = DateStyle.YEAR_MONTH_DAY,
                libraryGridLayout = LibraryGridLayout.LIST_LARGE,
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
            dataSource.resetAllSettings()

            // ----- Assert -----
            capturedResult shouldBe AppSettingsEntity()
        }
    }

    @Nested
    inner class LastUsedProgressUnitFlow {
        @Test
        fun `emits the default PAGE value when entity has not been changed`() = runTest {
            // ----- Arrange -----
            val entity = AppSettingsEntity(lastUsedProgressUnit = ProgressUnit.PAGE)

            every {
                dataStore.data
            } returns flowOf(entity)

            val freshDataSource =
                SettingsLocalDataSourceImpl(
                    appSettingsDataStore = AppSettingsDataStore(
                        store = dataStore,
                    ),
                    apiKeyLocalDataSource = apiKeyLocalDataSource,
                )

            // ----- Act & Assert -----
            freshDataSource.lastUsedProgressUnit.test {
                awaitItem() shouldBe ProgressUnit.PAGE
                awaitComplete()
            }
        }

        @Test
        fun `emits TIME when the entity carries TIME`() = runTest {
            // ----- Arrange -----
            val entity = AppSettingsEntity(lastUsedProgressUnit = ProgressUnit.TIME)

            every {
                dataStore.data
            } returns flowOf(entity)

            val freshDataSource =
                SettingsLocalDataSourceImpl(
                    appSettingsDataStore = AppSettingsDataStore(
                        store = dataStore,
                    ),
                    apiKeyLocalDataSource = apiKeyLocalDataSource,
                )

            // ----- Act & Assert -----
            freshDataSource.lastUsedProgressUnit.test {
                awaitItem() shouldBe ProgressUnit.TIME
                awaitComplete()
            }
        }

        @Test
        fun `emits PERCENTAGE when the entity carries PERCENTAGE`() = runTest {
            // ----- Arrange -----
            val entity = AppSettingsEntity(lastUsedProgressUnit = ProgressUnit.PERCENTAGE)

            every {
                dataStore.data
            } returns flowOf(entity)

            val freshDataSource =
                SettingsLocalDataSourceImpl(
                    appSettingsDataStore = AppSettingsDataStore(
                        store = dataStore,
                    ),
                    apiKeyLocalDataSource = apiKeyLocalDataSource,
                )

            // ----- Act & Assert -----
            freshDataSource.lastUsedProgressUnit.test {
                awaitItem() shouldBe ProgressUnit.PERCENTAGE
                awaitComplete()
            }
        }

        @Test
        fun `deduplicates identical consecutive lastUsedProgressUnit values`() = runTest {
            // ----- Arrange -----
            val entity1 = AppSettingsEntity(lastUsedProgressUnit = ProgressUnit.PAGE)
            val entity2 = AppSettingsEntity(lastUsedProgressUnit = ProgressUnit.PAGE)

            every {
                dataStore.data
            } returns flowOf(
                entity1,
                entity2,
            )

            val freshDataSource =
                SettingsLocalDataSourceImpl(
                    appSettingsDataStore = AppSettingsDataStore(
                        store = dataStore,
                    ),
                    apiKeyLocalDataSource = apiKeyLocalDataSource,
                )

            // ----- Act & Assert -----
            freshDataSource.lastUsedProgressUnit.test {
                awaitItem() shouldBe ProgressUnit.PAGE
                awaitComplete()
            }
        }
    }

    @Nested
    inner class SetLastUsedProgressUnit {
        @Test
        fun `update lambda sets lastUsedProgressUnit to TIME`() = runTest {
            // ----- Arrange -----
            val existingEntity = AppSettingsEntity(lastUsedProgressUnit = ProgressUnit.PAGE)
            var capturedResult: AppSettingsEntity? = null

            coEvery {
                dataStore.updateData(any())
            } coAnswers {
                val updater = firstArg<suspend (AppSettingsEntity) -> AppSettingsEntity>()
                capturedResult = updater(existingEntity)
                capturedResult!!
            }

            // ----- Act -----
            dataSource.setLastUsedProgressUnit(unit = ProgressUnit.TIME)

            // ----- Assert -----
            capturedResult?.lastUsedProgressUnit shouldBe ProgressUnit.TIME
        }

        @Test
        fun `update lambda sets lastUsedProgressUnit to PERCENTAGE`() = runTest {
            // ----- Arrange -----
            val existingEntity = AppSettingsEntity(lastUsedProgressUnit = ProgressUnit.PAGE)
            var capturedResult: AppSettingsEntity? = null

            coEvery {
                dataStore.updateData(any())
            } coAnswers {
                val updater = firstArg<suspend (AppSettingsEntity) -> AppSettingsEntity>()
                capturedResult = updater(existingEntity)
                capturedResult!!
            }

            // ----- Act -----
            dataSource.setLastUsedProgressUnit(unit = ProgressUnit.PERCENTAGE)

            // ----- Assert -----
            capturedResult?.lastUsedProgressUnit shouldBe ProgressUnit.PERCENTAGE
        }

        @Test
        fun `update lambda preserves other entity fields when updating lastUsedProgressUnit`() = runTest {
            // ----- Arrange -----
            val existingEntity = AppSettingsEntity(
                userId = 77,
                dateStyle = DateStyle.MONTH_DAY_YEAR,
                lastUsedProgressUnit = ProgressUnit.PAGE,
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
            dataSource.setLastUsedProgressUnit(unit = ProgressUnit.TIME)

            // ----- Assert -----
            capturedResult?.userId shouldBe 77
            capturedResult?.dateStyle shouldBe DateStyle.MONTH_DAY_YEAR
        }
    }

    @Nested
    inner class SetBottomBarStyle {
        @Test
        fun `update lambda sets bottomBarStyle inside themeConfig`() = runTest {
            // ----- Arrange -----
            val existingEntity = stubEntity(
                themeConfig = ThemeConfigurationEntity(
                    bottomBarStyle = BottomBarStyle.FLOATING,
                ),
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
                themeConfig = ThemeConfigurationEntity(
                    bottomBarStyle = BottomBarStyle.DOCKED,
                ),
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
                themeConfig = ThemeConfigurationEntity(
                    bottomBarStyle = BottomBarStyle.FLOATING,
                ),
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
