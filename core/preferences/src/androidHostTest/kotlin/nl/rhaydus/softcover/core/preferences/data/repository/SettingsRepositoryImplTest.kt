package nl.rhaydus.softcover.core.preferences.data.repository

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.BottomBarStyle
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.DesktopWindowState
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.ProgressUnit
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.domain.model.WindowPlacement
import nl.rhaydus.softcover.core.preferences.data.datasource.SettingsLocalDataSource
import nl.rhaydus.softcover.core.preferences.data.datasource.SettingsRemoteDataSource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SettingsRepositoryImplTest {
    private lateinit var settingsLocalDataSource: SettingsLocalDataSource
    private lateinit var settingsRemoteDataSource: SettingsRemoteDataSource
    private lateinit var repository: SettingsRepositoryImpl

    @BeforeEach
    fun setUp() {
        settingsLocalDataSource = mockk(relaxed = true)
        settingsRemoteDataSource = mockk(relaxed = true)
        repository =
            SettingsRepositoryImpl(
                settingsLocalDataSource = settingsLocalDataSource,
                settingsRemoteDataSource = settingsRemoteDataSource,
            )
    }

    @Nested
    inner class DateStyleProperty {
        @Test
        fun `dateStyle property is wired to local data source dateStyle flow`() = runTest {
            // ----- Arrange -----
            every {
                settingsLocalDataSource.dateStyle
            } returns flowOf(DateStyle.MONTH_DAY_YEAR)

            val freshRepository =
                SettingsRepositoryImpl(
                    settingsLocalDataSource = settingsLocalDataSource,
                    settingsRemoteDataSource = settingsRemoteDataSource,
                )

            // ----- Act & Assert -----
            freshRepository.dateStyle.test {
                awaitItem() shouldBe DateStyle.MONTH_DAY_YEAR
                awaitComplete()
            }
        }
    }

    @Nested
    inner class LibraryGridLayoutProperty {
        @Test
        fun `libraryGridLayout property is wired to local data source libraryGridLayout flow`() = runTest {
            // ----- Arrange -----
            every {
                settingsLocalDataSource.libraryGridLayout
            } returns flowOf(LibraryGridLayout.GRID_THREE_COLUMNS)

            val freshRepository =
                SettingsRepositoryImpl(
                    settingsLocalDataSource = settingsLocalDataSource,
                    settingsRemoteDataSource = settingsRemoteDataSource,
                )

            // ----- Act & Assert -----
            freshRepository.libraryGridLayout.test {
                awaitItem() shouldBe LibraryGridLayout.GRID_THREE_COLUMNS
                awaitComplete()
            }
        }
    }

    @Nested
    inner class SetLibraryGridLayout {
        @Test
        fun `delegates to local data source with the given layout`() = runTest {
            // ----- Arrange -----
            val layout = LibraryGridLayout.LIST_COMPACT

            // ----- Act -----
            repository.setLibraryGridLayout(layout = layout)

            // ----- Assert -----
            coVerify {
                settingsLocalDataSource.setLibraryGridLayout(layout = layout)
            }
        }
    }

    @Nested
    inner class SetDateStyle {
        @Test
        fun `delegates to local data source with the given style`() = runTest {
            // ----- Arrange -----
            val style = DateStyle.YEAR_MONTH_DAY

            // ----- Act -----
            repository.setDateStyle(style = style)

            // ----- Assert -----
            coVerify {
                settingsLocalDataSource.setDateStyle(style = style)
            }
        }
    }

    @Nested
    inner class UpdateApiKey {
        @Test
        fun `delegates to local data source with the given key`() = runTest {
            // ----- Arrange -----
            val key = "test-api-key"

            // ----- Act -----
            repository.updateApiKey(key = key)

            // ----- Assert -----
            coVerify {
                settingsLocalDataSource.updateApiKey(key = key)
            }
        }
    }

    @Nested
    inner class GetUserId {
        @Test
        fun `getUserId is wired to local data source getUserId flow`() = runTest {
            // ----- Arrange -----
            every {
                settingsLocalDataSource.getUserId()
            } returns flowOf(99)

            // ----- Act & Assert -----
            repository.getUserId().test {
                awaitItem() shouldBe 99
                awaitComplete()
            }
        }
    }

    @Nested
    inner class GetThemeConfig {
        @Test
        fun `getThemeConfig is wired to local data source getThemeConfig flow`() = runTest {
            // ----- Arrange -----
            val configuration = ThemeConfiguration(bottomBarStyle = BottomBarStyle.DOCKED)

            every {
                settingsLocalDataSource.getThemeConfig()
            } returns flowOf(configuration)

            // ----- Act & Assert -----
            repository.getThemeConfig().test {
                awaitItem() shouldBe configuration
                awaitComplete()
            }
        }
    }

    @Nested
    inner class UpdateUserId {
        @Test
        fun `delegates to local data source with the given id`() = runTest {
            // ----- Arrange -----
            val id = 42

            // ----- Act -----
            repository.updateUserId(id = id)

            // ----- Assert -----
            coVerify {
                settingsLocalDataSource.updateUserId(id = id)
            }
        }
    }

    @Nested
    inner class SetBottomBarStyle {
        @Test
        fun `delegates to local data source with the given style`() = runTest {
            // ----- Arrange -----
            val style = BottomBarStyle.FLOATING

            // ----- Act -----
            repository.setBottomBarStyle(style = style)

            // ----- Assert -----
            coVerify {
                settingsLocalDataSource.setBottomBarStyle(style = style)
            }
        }
    }

    @Nested
    inner class GetUserIdFromBackend {
        @Test
        fun `delegates to remote data source and returns the result`() = runTest {
            // ----- Arrange -----
            val expectedId = 77

            coEvery {
                settingsRemoteDataSource.getUserIdFromBackend()
            } returns expectedId

            // ----- Act -----
            val result = repository.getUserIdFromBackend()

            // ----- Assert -----
            result shouldBe expectedId
        }
    }

    @Nested
    inner class EnabledStatusCodesProperty {
        @Test
        fun `enabledStatusCodes is wired to local data source enabledStatusCodes flow`() = runTest {
            // ----- Arrange -----
            every {
                settingsLocalDataSource.enabledStatusCodes
            } returns flowOf(setOf(1, 3, 5))

            val freshRepository =
                SettingsRepositoryImpl(
                    settingsLocalDataSource = settingsLocalDataSource,
                    settingsRemoteDataSource = settingsRemoteDataSource,
                )

            // ----- Act & Assert -----
            freshRepository.enabledStatusCodes.test {
                awaitItem() shouldBe setOf(1, 3, 5)
                awaitComplete()
            }
        }
    }

    @Nested
    inner class EnabledListIdsProperty {
        @Test
        fun `enabledListIds is wired to local data source enabledListIds flow`() = runTest {
            // ----- Arrange -----
            every {
                settingsLocalDataSource.enabledListIds
            } returns flowOf(setOf(7, 9))

            val freshRepository =
                SettingsRepositoryImpl(
                    settingsLocalDataSource = settingsLocalDataSource,
                    settingsRemoteDataSource = settingsRemoteDataSource,
                )

            // ----- Act & Assert -----
            freshRepository.enabledListIds.test {
                awaitItem() shouldBe setOf(7, 9)
                awaitComplete()
            }
        }
    }

    @Nested
    inner class ListDefaultsSeededProperty {
        @Test
        fun `listDefaultsSeeded is wired to local data source listDefaultsSeeded flow`() = runTest {
            // ----- Arrange -----
            every {
                settingsLocalDataSource.listDefaultsSeeded
            } returns flowOf(true)

            val freshRepository =
                SettingsRepositoryImpl(
                    settingsLocalDataSource = settingsLocalDataSource,
                    settingsRemoteDataSource = settingsRemoteDataSource,
                )

            // ----- Act & Assert -----
            freshRepository.listDefaultsSeeded.test {
                awaitItem() shouldBe true
                awaitComplete()
            }
        }
    }

    @Nested
    inner class SeedEnabledListIds {
        @Test
        fun `delegates to local data source with the given ids`() = runTest {
            // ----- Arrange -----
            val ids = setOf(1, 5, 9)

            // ----- Act -----
            repository.seedEnabledListIds(ids = ids)

            // ----- Assert -----
            coVerify {
                settingsLocalDataSource.seedEnabledListIds(ids = ids)
            }
        }

        @Test
        fun `delegates to local data source with empty set`() = runTest {
            // ----- Arrange -----
            val ids = emptySet<Int>()

            // ----- Act -----
            repository.seedEnabledListIds(ids = ids)

            // ----- Assert -----
            coVerify {
                settingsLocalDataSource.seedEnabledListIds(ids = ids)
            }
        }
    }

    @Nested
    inner class GetUserIdFromBackendError {
        @Test
        fun `propagates exception thrown by remote data source`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("network error")

            coEvery {
                settingsRemoteDataSource.getUserIdFromBackend()
            } throws expectedError

            // ----- Act -----
            val thrownError = runCatching { repository.getUserIdFromBackend() }.exceptionOrNull()

            // ----- Assert -----
            thrownError shouldBe expectedError
        }
    }

    @Nested
    inner class SetEnabledStatusCodes {
        @Test
        fun `delegates to local data source with the given codes`() = runTest {
            // ----- Arrange -----
            val codes = setOf(1, 3, 5)

            // ----- Act -----
            repository.setEnabledStatusCodes(codes = codes)

            // ----- Assert -----
            coVerify {
                settingsLocalDataSource.setEnabledStatusCodes(codes = codes)
            }
        }

        @Test
        fun `delegates to local data source with empty set`() = runTest {
            // ----- Arrange -----
            val codes = emptySet<Int>()

            // ----- Act -----
            repository.setEnabledStatusCodes(codes = codes)

            // ----- Assert -----
            coVerify {
                settingsLocalDataSource.setEnabledStatusCodes(codes = codes)
            }
        }
    }

    @Nested
    inner class SetEnabledListIds {
        @Test
        fun `delegates to local data source with the given ids`() = runTest {
            // ----- Arrange -----
            val ids = setOf(10, 20, 30)

            // ----- Act -----
            repository.setEnabledListIds(ids = ids)

            // ----- Assert -----
            coVerify {
                settingsLocalDataSource.setEnabledListIds(ids = ids)
            }
        }

        @Test
        fun `delegates to local data source with empty set`() = runTest {
            // ----- Arrange -----
            val ids = emptySet<Int>()

            // ----- Act -----
            repository.setEnabledListIds(ids = ids)

            // ----- Assert -----
            coVerify {
                settingsLocalDataSource.setEnabledListIds(ids = ids)
            }
        }
    }

    @Nested
    inner class DesktopWindowStateProperty {
        @Test
        fun `desktopWindowState property is wired to local data source desktopWindowState flow`() = runTest {
            // ----- Arrange -----
            val state = DesktopWindowState(
                width = 1280,
                height = 800,
                x = 100,
                y = 200,
                placement = WindowPlacement.FLOATING,
            )

            every {
                settingsLocalDataSource.desktopWindowState
            } returns flowOf(state)

            val freshRepository =
                SettingsRepositoryImpl(
                    settingsLocalDataSource = settingsLocalDataSource,
                    settingsRemoteDataSource = settingsRemoteDataSource,
                )

            // ----- Act & Assert -----
            freshRepository.desktopWindowState.test {
                awaitItem() shouldBe state
                awaitComplete()
            }
        }
    }

    @Nested
    inner class SetDesktopWindowState {
        @Test
        fun `delegates to local data source with the given state`() = runTest {
            // ----- Arrange -----
            val state = DesktopWindowState(
                width = 1440,
                height = 900,
                x = 50,
                y = 80,
                placement = WindowPlacement.FLOATING,
            )

            // ----- Act -----
            repository.setDesktopWindowState(state = state)

            // ----- Assert -----
            coVerify {
                settingsLocalDataSource.setDesktopWindowState(state = state)
            }
        }

        @Test
        fun `delegates to local data source with a maximized state`() = runTest {
            // ----- Arrange -----
            val state = DesktopWindowState(placement = WindowPlacement.MAXIMIZED)

            // ----- Act -----
            repository.setDesktopWindowState(state = state)

            // ----- Assert -----
            coVerify {
                settingsLocalDataSource.setDesktopWindowState(state = state)
            }
        }
    }

    @Nested
    inner class LastUsedProgressUnitProperty {
        @Test
        fun `lastUsedProgressUnit is wired to local data source flow`() = runTest {
            // ----- Arrange -----
            every {
                settingsLocalDataSource.lastUsedProgressUnit
            } returns flowOf(ProgressUnit.TIME)

            val freshRepository =
                SettingsRepositoryImpl(
                    settingsLocalDataSource = settingsLocalDataSource,
                    settingsRemoteDataSource = settingsRemoteDataSource,
                )

            // ----- Act & Assert -----
            freshRepository.lastUsedProgressUnit.test {
                awaitItem() shouldBe ProgressUnit.TIME
                awaitComplete()
            }
        }
    }

    @Nested
    inner class SetLastUsedProgressUnit {
        @Test
        fun `delegates to local data source with the given unit`() = runTest {
            // ----- Act -----
            repository.setLastUsedProgressUnit(unit = ProgressUnit.PERCENTAGE)

            // ----- Assert -----
            coVerify {
                settingsLocalDataSource.setLastUsedProgressUnit(unit = ProgressUnit.PERCENTAGE)
            }
        }
    }
}
