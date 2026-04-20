package nl.rhaydus.softcover.feature.settings.data.repository

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsLocalDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsRemoteDataSource
import nl.rhaydus.softcover.feature.settings.domain.model.BottomBarStyle
import nl.rhaydus.softcover.feature.settings.domain.model.DateStyle
import nl.rhaydus.softcover.feature.settings.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.feature.settings.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.feature.settings.domain.model.UserProfileData
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
        repository = SettingsRepositoryImpl(
            settingsLocalDataSource = settingsLocalDataSource,
            settingsRemoteDataSource = settingsRemoteDataSource,
        )
    }

    @Nested
    inner class DateStyle {

        @Test
        fun `dateStyle property is wired to local data source dateStyle flow`() = runTest {
            // ----- Arrange -----
            every {
                settingsLocalDataSource.dateStyle
            } returns flowOf(nl.rhaydus.softcover.feature.settings.domain.model.DateStyle.MONTH_DAY_YEAR)

            val freshRepository = SettingsRepositoryImpl(
                settingsLocalDataSource = settingsLocalDataSource,
                settingsRemoteDataSource = settingsRemoteDataSource,
            )

            // ----- Act & Assert -----
            freshRepository.dateStyle.test {
                awaitItem() shouldBe nl.rhaydus.softcover.feature.settings.domain.model.DateStyle.MONTH_DAY_YEAR
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

            val freshRepository = SettingsRepositoryImpl(
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
            val style = nl.rhaydus.softcover.feature.settings.domain.model.DateStyle.YEAR_MONTH_DAY

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
    inner class GetUserProfileData {

        @Test
        fun `delegates to remote data source and returns the result`() = runTest {
            // ----- Arrange -----
            val expectedData = UserProfileData(
                profileImageUrl = "https://example.com/image.png",
                name = "Jane Doe",
                bio = "Reader",
                booksRead = 10,
            )

            coEvery {
                settingsRemoteDataSource.getUserProfileData()
            } returns expectedData

            // ----- Act -----
            val result = repository.getUserProfileData()

            // ----- Assert -----
            result shouldBe expectedData
        }

        @Test
        fun `propagates exception thrown by remote data source`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("network error")

            coEvery {
                settingsRemoteDataSource.getUserProfileData()
            } throws expectedError

            // ----- Act -----
            val thrownError = runCatching { repository.getUserProfileData() }.exceptionOrNull()

            // ----- Assert -----
            thrownError shouldBe expectedError
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
}
