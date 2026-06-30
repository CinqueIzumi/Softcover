package nl.rhaydus.softcover.core.profile.data.repository

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.profile.data.datasource.ProfileLocalDataSource
import nl.rhaydus.softcover.core.profile.data.datasource.ProfileRemoteDataSource
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileSnapshot

class ProfileRepositoryImplTest {
    private lateinit var profileRemoteDataSource: ProfileRemoteDataSource
    private lateinit var profileLocalDataSource: ProfileLocalDataSource
    private lateinit var repository: ProfileRepositoryImpl

    @BeforeEach
    fun setUp() {
        profileRemoteDataSource = mockk(relaxed = true)
        profileLocalDataSource = mockk(relaxed = true)
        repository = ProfileRepositoryImpl(
            profileRemoteDataSource = profileRemoteDataSource,
            profileLocalDataSource = profileLocalDataSource,
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
        activeReadingDates = setOf(LocalDate(
            2026,
            5,
            4,
        ),),
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
                profileRemoteDataSource.getUserProfileSnapshot(userId = 42)
            } returns expected

            // ----- Act -----
            val result = repository.fetchUserProfileSnapshot(userId = 42)

            // ----- Assert -----
            result shouldBe expected
            coVerify(exactly = 1) { profileRemoteDataSource.getUserProfileSnapshot(userId = 42) }
        }

        @Test
        fun `propagates exception thrown by remote`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("network error")

            coEvery {
                profileRemoteDataSource.getUserProfileSnapshot(userId = 42)
            } throws expectedError

            // ----- Act -----
            val thrownError = runCatching { repository.fetchUserProfileSnapshot(userId = 42) }.exceptionOrNull()

            // ----- Assert -----
            thrownError shouldBe expectedError
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
