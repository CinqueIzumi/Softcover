package nl.rhaydus.softcover.core.preferences.data.datasource

import androidx.datastore.core.DataStore
import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.BottomBarStyle
import nl.rhaydus.softcover.core.domain.model.ColorPalette
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.LibrarySortSettings
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.domain.model.ThemeMode
import nl.rhaydus.softcover.core.preferences.data.datastore.AppSettingsDataStore
import nl.rhaydus.softcover.core.preferences.data.model.AppSettingsEntity
import nl.rhaydus.softcover.core.preferences.data.model.ThemeConfigurationEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SettingsLocalDataSourceImplTest {
    private lateinit var persisted: MutableStateFlow<AppSettingsEntity>
    private lateinit var dataSource: SettingsLocalDataSourceImpl

    @BeforeEach
    fun setUp() {
        persisted = MutableStateFlow(AppSettingsEntity())

        val dataStore = mockk<DataStore<AppSettingsEntity>>()

        every {
            dataStore.data
        } returns persisted

        coEvery {
            dataStore.updateData(any())
        } coAnswers {
            val transform = firstArg<suspend (AppSettingsEntity) -> AppSettingsEntity>()
            val updated = transform(persisted.value)
            persisted.value = updated
            updated
        }

        dataSource =
            SettingsLocalDataSourceImpl(
                appSettingsDataStore = AppSettingsDataStore(store = dataStore),
                apiKeyLocalDataSource = mockk(relaxed = true),
            )
    }

    @Nested
    inner class LibrarySortSettingsByTab {
        @Test
        fun `combines the stored mode and direction of each tab`() = runTest {
            // ----- Arrange -----
            persisted.value = AppSettingsEntity(
                librarySortModeByTab = mapOf("reading" to LibrarySortMode.TITLE),
                librarySortDirectionByTab = mapOf("reading" to SortDirection.DESCENDING),
            )

            // ----- Act & Assert -----
            dataSource.librarySortSettingsByTab.test {
                awaitItem() shouldBe mapOf(
                    "reading" to LibrarySortSettings(
                        mode = LibrarySortMode.TITLE,
                        direction = SortDirection.DESCENDING,
                    ),
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `falls back to the mode default direction when only a mode is stored`() = runTest {
            // ----- Arrange -----
            persisted.value = AppSettingsEntity(
                librarySortModeByTab = mapOf("reading" to LibrarySortMode.TITLE),
            )

            // ----- Act & Assert -----
            dataSource.librarySortSettingsByTab.test {
                awaitItem() shouldBe mapOf(
                    "reading" to LibrarySortSettings(
                        mode = LibrarySortMode.TITLE,
                        direction = LibrarySortMode.TITLE.defaultDirection,
                    ),
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `falls back to the default mode when only a direction is stored`() = runTest {
            // ----- Arrange -----
            persisted.value = AppSettingsEntity(
                librarySortDirectionByTab = mapOf("reading" to SortDirection.ASCENDING),
            )

            // ----- Act & Assert -----
            dataSource.librarySortSettingsByTab.test {
                awaitItem() shouldBe mapOf(
                    "reading" to LibrarySortSettings(
                        mode = LibrarySortMode.Default,
                        direction = SortDirection.ASCENDING,
                    ),
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `does not re-emit when an unrelated setting changes`() = runTest {
            // ----- Arrange -----
            dataSource.librarySortSettingsByTab.test {
                awaitItem() shouldBe emptyMap()

                // ----- Act -----
                dataSource.updateUserId(id = 7)

                // ----- Assert -----
                expectNoEvents()
            }
        }
    }

    @Nested
    inner class SetLibrarySortForTab {
        @Test
        fun `stores the sort for one tab and keeps the other tabs`() = runTest {
            // ----- Arrange -----
            dataSource.setLibrarySortForTab(
                tabId = "reading",
                mode = LibrarySortMode.TITLE,
                direction = SortDirection.ASCENDING,
            )

            // ----- Act -----
            dataSource.setLibrarySortForTab(
                tabId = "read",
                mode = LibrarySortMode.DATE_FINISHED,
                direction = SortDirection.ASCENDING,
            )

            // ----- Assert -----
            dataSource.librarySortSettingsByTab.test {
                awaitItem() shouldBe mapOf(
                    "reading" to LibrarySortSettings(
                        mode = LibrarySortMode.TITLE,
                        direction = SortDirection.ASCENDING,
                    ),
                    "read" to LibrarySortSettings(
                        mode = LibrarySortMode.DATE_FINISHED,
                        direction = SortDirection.ASCENDING,
                    ),
                )
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class GetThemeConfig {
        @Test
        fun `emits the stored theme as a domain model`() = runTest {
            // ----- Arrange -----
            persisted.value = AppSettingsEntity(
                themeConfig = ThemeConfigurationEntity(
                    bottomBarStyle = BottomBarStyle.DOCKED,
                    themeMode = ThemeMode.DARK,
                    colorPalette = ColorPalette.INK,
                    useDynamicColor = true,
                ),
            )

            // ----- Act & Assert -----
            dataSource.getThemeConfig().test {
                awaitItem() shouldBe ThemeConfiguration(
                    bottomBarStyle = BottomBarStyle.DOCKED,
                    themeMode = ThemeMode.DARK,
                    colorPalette = ColorPalette.INK,
                    useDynamicColor = true,
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `does not re-emit when a non-theme setting changes`() = runTest {
            // ----- Arrange -----
            dataSource.getThemeConfig().test {
                awaitItem()

                // ----- Act -----
                dataSource.updateUserId(id = 7)

                // ----- Assert -----
                expectNoEvents()
            }
        }
    }

    @Nested
    inner class ThemeSetters {
        @Test
        fun `each theme setter changes only its own field and leaves the rest of the settings alone`() = runTest {
            // ----- Arrange -----
            val untouched = AppSettingsEntity(userId = 42)
            persisted.value = untouched

            // ----- Act -----
            dataSource.setBottomBarStyle(style = BottomBarStyle.DOCKED)
            dataSource.setThemeMode(mode = ThemeMode.DARK)
            dataSource.setColorPalette(palette = ColorPalette.INK)
            dataSource.setDynamicColorEnabled(enabled = true)

            // ----- Assert -----
            persisted.value shouldBe untouched.copy(
                themeConfig = ThemeConfigurationEntity(
                    bottomBarStyle = BottomBarStyle.DOCKED,
                    themeMode = ThemeMode.DARK,
                    colorPalette = ColorPalette.INK,
                    useDynamicColor = true,
                ),
            )
        }
    }

    @Nested
    inner class SeedEnabledListIds {
        @Test
        fun `merges the ids into the enabled ones and marks the defaults as seeded`() = runTest {
            // ----- Arrange -----
            persisted.value = AppSettingsEntity(enabledListIds = setOf(1, 2))

            // ----- Act -----
            dataSource.seedEnabledListIds(ids = setOf(2, 3))

            // ----- Assert -----
            persisted.value.enabledListIds shouldBe setOf(1, 2, 3)
            persisted.value.listDefaultsSeeded shouldBe true
        }

        @Test
        fun `seeding no ids still marks the defaults as seeded`() = runTest {
            // ----- Arrange -----
            persisted.value = AppSettingsEntity(enabledListIds = setOf(1))

            // ----- Act -----
            dataSource.seedEnabledListIds(ids = emptySet())

            // ----- Assert -----
            persisted.value.enabledListIds shouldBe setOf(1)
            persisted.value.listDefaultsSeeded shouldBe true
        }
    }

    @Nested
    inner class ResetLibraryVisibilityPreferences {
        @Test
        fun `restores the library visibility and tab order defaults and keeps every other setting`() = runTest {
            // ----- Arrange -----
            val defaults = AppSettingsEntity()
            val customised = AppSettingsEntity(
                userId = 42,
                enabledStatusCodes = setOf(2),
                enabledListIds = setOf(9),
                listDefaultsSeeded = true,
                libraryTabOrder = listOf(
                    "read",
                    "reading",
                ),
            )
            persisted.value = customised

            // ----- Act -----
            dataSource.resetLibraryVisibilityPreferences()

            // ----- Assert -----
            persisted.value shouldBe customised.copy(
                enabledStatusCodes = defaults.enabledStatusCodes,
                enabledListIds = defaults.enabledListIds,
                listDefaultsSeeded = defaults.listDefaultsSeeded,
                libraryTabOrder = defaults.libraryTabOrder,
            )
        }
    }

    @Nested
    inner class ResetAllSettings {
        @Test
        fun `restores every setting to its default but keeps the paused shelf seeded`() = runTest {
            // ----- Arrange -----
            persisted.value = AppSettingsEntity(
                userId = 42,
                enabledListIds = setOf(9),
                listDefaultsSeeded = true,
                themeConfig = ThemeConfigurationEntity(themeMode = ThemeMode.DARK),
                librarySortModeByTab = mapOf("reading" to LibrarySortMode.TITLE),
            )

            // ----- Act -----
            dataSource.resetAllSettings()

            // ----- Assert -----
            persisted.value shouldBe AppSettingsEntity(pausedShelfSeeded = true)
        }
    }
}
