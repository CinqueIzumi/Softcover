package nl.rhaydus.softcover.core.preferences.data.repository

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.LibrarySortSettings
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.preferences.data.datasource.SettingsLocalDataSource
import nl.rhaydus.softcover.core.preferences.data.datasource.SettingsRemoteDataSource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SettingsRepositoryImplNewPathsTest {
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
    inner class LibrarySortSettingsByTab {
        @Test
        fun `librarySortSettingsByTab is wired to local data source flow`() = runTest {
            // ----- Arrange -----
            val expected = mapOf(
                "reading" to LibrarySortSettings(
                    mode = LibrarySortMode.TITLE,
                    direction = SortDirection.ASCENDING,
                ),
            )

            every {
                settingsLocalDataSource.librarySortSettingsByTab
            } returns flowOf(expected)

            val freshRepository =
                SettingsRepositoryImpl(
                    settingsLocalDataSource = settingsLocalDataSource,
                    settingsRemoteDataSource = settingsRemoteDataSource,
                )

            // ----- Act & Assert -----
            freshRepository.librarySortSettingsByTab.test {
                awaitItem() shouldBe expected
                awaitComplete()
            }
        }

        @Test
        fun `librarySortSettingsByTab emits empty map when no overrides set`() = runTest {
            // ----- Arrange -----
            every {
                settingsLocalDataSource.librarySortSettingsByTab
            } returns flowOf(emptyMap())

            val freshRepository =
                SettingsRepositoryImpl(
                    settingsLocalDataSource = settingsLocalDataSource,
                    settingsRemoteDataSource = settingsRemoteDataSource,
                )

            // ----- Act & Assert -----
            freshRepository.librarySortSettingsByTab.test {
                awaitItem() shouldBe emptyMap()
                awaitComplete()
            }
        }
    }

    @Nested
    inner class SetLibrarySortForTab {
        @Test
        fun `delegates to local data source with given tab id, mode, and direction`() = runTest {
            // ----- Arrange -----

            // ----- Act -----
            repository.setLibrarySortForTab(
                tabId = "want_to_read",
                mode = LibrarySortMode.RATING,
                direction = SortDirection.DESCENDING,
            )

            // ----- Assert -----
            coVerify {
                settingsLocalDataSource.setLibrarySortForTab(
                    tabId = "want_to_read",
                    mode = LibrarySortMode.RATING,
                    direction = SortDirection.DESCENDING,
                )
            }
        }
    }

    @Nested
    inner class DismissedPlanTodayByBook {
        @Test
        fun `dismissedPlanTodayByBook is wired to local data source flow`() = runTest {
            // ----- Arrange -----
            val expected = mapOf(42 to "2025-05-13")

            every {
                settingsLocalDataSource.dismissedPlanTodayByBook
            } returns flowOf(expected)

            val freshRepository =
                SettingsRepositoryImpl(
                    settingsLocalDataSource = settingsLocalDataSource,
                    settingsRemoteDataSource = settingsRemoteDataSource,
                )

            // ----- Act & Assert -----
            freshRepository.dismissedPlanTodayByBook.test {
                awaitItem() shouldBe expected
                awaitComplete()
            }
        }
    }

    @Nested
    inner class SetPlanTodayDismissed {
        @Test
        fun `delegates to local data source with given bookId and isoDate`() = runTest {
            // ----- Arrange -----

            // ----- Act -----
            repository.setPlanTodayDismissed(
                bookId = 7,
                isoDate = "2025-05-13",
            )

            // ----- Assert -----
            coVerify {
                settingsLocalDataSource.setPlanTodayDismissed(
                    bookId = 7,
                    isoDate = "2025-05-13",
                )
            }
        }
    }
}
