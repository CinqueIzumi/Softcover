package nl.rhaydus.softcover.feature.settings.data.repository

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsLocalDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsRemoteDataSource
import nl.rhaydus.softcover.feature.settings.domain.model.LibrarySortMode
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
        repository = SettingsRepositoryImpl(
            settingsLocalDataSource = settingsLocalDataSource,
            settingsRemoteDataSource = settingsRemoteDataSource,
        )
    }

    @Nested
    inner class LibrarySortModeByTab {

        @Test
        fun `librarySortModeByTab is wired to local data source flow`() = runTest {
            // ----- Arrange -----
            val expected = mapOf("reading" to LibrarySortMode.TITLE)

            every {
                settingsLocalDataSource.librarySortModeByTab
            } returns flowOf(expected)

            val freshRepository = SettingsRepositoryImpl(
                settingsLocalDataSource = settingsLocalDataSource,
                settingsRemoteDataSource = settingsRemoteDataSource,
            )

            // ----- Act & Assert -----
            freshRepository.librarySortModeByTab.test {
                awaitItem() shouldBe expected
                awaitComplete()
            }
        }

        @Test
        fun `librarySortModeByTab emits empty map when no overrides set`() = runTest {
            // ----- Arrange -----
            every {
                settingsLocalDataSource.librarySortModeByTab
            } returns flowOf(emptyMap())

            val freshRepository = SettingsRepositoryImpl(
                settingsLocalDataSource = settingsLocalDataSource,
                settingsRemoteDataSource = settingsRemoteDataSource,
            )

            // ----- Act & Assert -----
            freshRepository.librarySortModeByTab.test {
                awaitItem() shouldBe emptyMap()
                awaitComplete()
            }
        }
    }

    @Nested
    inner class SetLibrarySortModeForTab {

        @Test
        fun `delegates to local data source with given tab id and mode`() = runTest {
            // ----- Arrange -----

            // ----- Act -----
            repository.setLibrarySortModeForTab(
                tabId = "want_to_read",
                mode = LibrarySortMode.RATING,
            )

            // ----- Assert -----
            coVerify {
                settingsLocalDataSource.setLibrarySortModeForTab(
                    tabId = "want_to_read",
                    mode = LibrarySortMode.RATING,
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

            val freshRepository = SettingsRepositoryImpl(
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
