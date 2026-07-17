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
    private lateinit var profileRefreshGate: ProfileRefreshGate
    private lateinit var repository: ProfileRepositoryImpl

    @BeforeEach
    fun setUp() {
        profileRemoteDataSource = mockk(relaxed = true)
        profileLocalDataSource = mockk(relaxed = true)
        profileRefreshGate = mockk(relaxed = true)
        repository = ProfileRepositoryImpl(
            profileRemoteDataSource = profileRemoteDataSource,
            profileLocalDataSource = profileLocalDataSource,
            profileRefreshGate = profileRefreshGate,
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
    inner class GetActiveReadingDaysSince {
        @Test
        fun `returns dates on or after the since date, excluding earlier dates`() = runTest {
            // ----- Arrange -----
            val since = LocalDate(
                2026,
                6,
                10,
            )
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
                    6,
                    1,
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
            val result = repository.getActiveReadingDaysSince(
                userId = 7,
                since = since,
            )

            // ----- Assert -----
            result shouldBe setOf(
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
            )
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
    inner class CacheUserProfileData {
        @Test
        fun `delegates to local data source`() = runTest {
            // ----- Arrange -----
            val profileData = buildProfileData()

            // ----- Act -----
            repository.cacheUserProfileData(data = profileData)

            // ----- Assert -----
            coVerify(exactly = 1) { profileLocalDataSource.cacheUserProfileData(data = profileData) }
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
        fun `resets the refresh gate so the next session can refetch`() = runTest {
            // ----- Arrange -----
            // (refresh gate is relaxed)

            // ----- Act -----
            repository.clearProfileCache()

            // ----- Assert -----
            coVerify(exactly = 1) { profileRefreshGate.reset() }
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
