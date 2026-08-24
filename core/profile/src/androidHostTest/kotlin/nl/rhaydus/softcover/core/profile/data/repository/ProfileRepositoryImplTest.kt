package nl.rhaydus.softcover.core.profile.data.repository

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.profile.data.datasource.ProfileLocalDataSource
import nl.rhaydus.softcover.core.profile.data.datasource.ProfileRemoteDataSource
import nl.rhaydus.softcover.core.profile.domain.ProfileRefreshGate
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileSnapshot

class ProfileRepositoryImplTest {
    private lateinit var profileRemoteDataSource: ProfileRemoteDataSource
    private lateinit var profileLocalDataSource: ProfileLocalDataSource
    private lateinit var activityRefreshGate: ProfileRefreshGate
    private lateinit var statsRefreshGate: ProfileRefreshGate
    private lateinit var repository: ProfileRepositoryImpl

    @BeforeEach
    fun setUp() {
        profileRemoteDataSource = mockk(relaxed = true)
        profileLocalDataSource = mockk(relaxed = true)
        activityRefreshGate = mockk(relaxed = true)
        statsRefreshGate = mockk(relaxed = true)
        repository = ProfileRepositoryImpl(
            profileRemoteDataSource = profileRemoteDataSource,
            profileLocalDataSource = profileLocalDataSource,
            activityRefreshGate = activityRefreshGate,
            statsRefreshGate = statsRefreshGate,
        )
    }

    private fun buildSnapshot(): UserProfileSnapshot = UserProfileSnapshot(
        profileImageUrl = "https://example.com/avatar.png",
        name = "Jane Doe",
        username = "cinque",
        bio = "Avid reader",
        booksRead = 42,
        totalPagesRead = 12345,
        averageRating = 4.2,
    )

    private fun buildProfileData(): UserProfileData = UserProfileData(
        profileImageUrl = "https://example.com/avatar.png",
        name = "Jane Doe",
        username = "cinque",
        bio = "Avid reader",
        booksRead = 42,
        totalPagesRead = 12345,
        averageRating = 4.2,
        readingStreak = 7,
    )

    @Nested
    inner class FetchUserProfileSnapshot {
        @Test
        fun `delegates to remote and returns its result`() = runTest {
            // ----- Arrange -----
            val expected = buildSnapshot()

            coEvery {
                profileRemoteDataSource.getUserProfileSnapshot()
            } returns expected

            // ----- Act -----
            val result = repository.fetchUserProfileSnapshot()

            // ----- Assert -----
            result shouldBe expected
            coVerify(exactly = 1) { profileRemoteDataSource.getUserProfileSnapshot() }
        }

        @Test
        fun `propagates exception thrown by remote`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("network error")

            coEvery {
                profileRemoteDataSource.getUserProfileSnapshot()
            } throws expectedError

            // ----- Act -----
            val thrownError = runCatching { repository.fetchUserProfileSnapshot() }.exceptionOrNull()

            // ----- Assert -----
            thrownError shouldBe expectedError
        }
    }

    @Nested
    inner class StreamReadingDaysDescending {
        @Test
        fun `delegates to remote and returns its flow`() = runTest {
            // ----- Arrange -----
            val dates = listOf(
                LocalDate(
                    2026,
                    6,
                    19,
                ),
                LocalDate(
                    2026,
                    6,
                    10,
                ),
                LocalDate(
                    2026,
                    5,
                    4,
                ),
            )

            every {
                profileRemoteDataSource.streamReadingDaysDescending(userId = 7)
            } returns flowOf(*dates.toTypedArray())

            // ----- Act -----
            val result = repository.streamReadingDaysDescending(userId = 7).toList()

            // ----- Assert -----
            result shouldBe dates
        }
    }

    @Nested
    inner class ObserveUserProfileData {
        @Test
        fun `delegates to local data source and returns its flow`() = runTest {
            // ----- Arrange -----
            val profileData = buildProfileData()
            val localFlow = MutableStateFlow<UserProfileData?>(profileData)

            every {
                profileLocalDataSource.observeUserProfileData()
            } returns localFlow

            // ----- Act -----
            val result = repository.observeUserProfileData().first()

            // ----- Assert -----
            result shouldBe profileData
        }
    }

    @Nested
    inner class CacheUserProfileActivity {
        @Test
        fun `delegates to local data source with the given streak and recent days`() = runTest {
            // ----- Arrange -----
            val recentReadingDays = setOf(
                LocalDate(
                    2026,
                    6,
                    19,
                ),
            )

            // ----- Act -----
            repository.cacheUserProfileActivity(
                readingStreak = 7,
                recentReadingDays = recentReadingDays,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                profileLocalDataSource.cacheUserProfileActivity(
                    readingStreak = 7,
                    recentReadingDays = recentReadingDays,
                )
            }
        }
    }

    @Nested
    inner class CacheUserProfileStats {
        @Test
        fun `delegates to local data source with the given snapshot`() = runTest {
            // ----- Arrange -----
            val snapshot = buildSnapshot()

            // ----- Act -----
            repository.cacheUserProfileStats(snapshot = snapshot)

            // ----- Assert -----
            coVerify(exactly = 1) { profileLocalDataSource.cacheUserProfileStats(snapshot = snapshot) }
        }
    }

    @Nested
    inner class ClearProfileCache {
        @Test
        fun `delegates to local data source clear`() = runTest {
            // ----- Arrange -----
            // (local data source is relaxed)

            // ----- Act -----
            repository.clearProfileCache()

            // ----- Assert -----
            coVerify(exactly = 1) { profileLocalDataSource.clear() }
        }

        @Test
        fun `resets the activity refresh gate so the next session can refetch`() = runTest {
            // ----- Arrange -----
            // (refresh gate is relaxed)

            // ----- Act -----
            repository.clearProfileCache()

            // ----- Assert -----
            coVerify(exactly = 1) { activityRefreshGate.reset() }
        }

        @Test
        fun `resets the stats refresh gate so the next session can refetch`() = runTest {
            // ----- Arrange -----
            // (refresh gate is relaxed)

            // ----- Act -----
            repository.clearProfileCache()

            // ----- Assert -----
            coVerify(exactly = 1) { statsRefreshGate.reset() }
        }
    }

    @Nested
    inner class MarkActiveReadingDate {
        @Test
        fun `delegates to local data source`() = runTest {
            // ----- Arrange -----
            val date = LocalDate(
                2026,
                6,
                19,
            )

            // ----- Act -----
            repository.markActiveReadingDate(date = date)

            // ----- Assert -----
            coVerify(exactly = 1) { profileLocalDataSource.markActiveReadingDate(date = date) }
        }
    }
}
