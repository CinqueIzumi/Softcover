package nl.rhaydus.softcover.feature.profile.data.repository

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.profile.data.datasource.ProfileRemoteDataSource
import nl.rhaydus.softcover.feature.profile.domain.model.UserProfileSnapshot
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ProfileRepositoryImplTest {

    private lateinit var profileRemoteDataSource: ProfileRemoteDataSource
    private lateinit var repository: ProfileRepositoryImpl

    @BeforeEach
    fun setUp() {
        profileRemoteDataSource = mockk(relaxed = true)
        repository = ProfileRepositoryImpl(profileRemoteDataSource = profileRemoteDataSource)
    }

    @Nested
    inner class GetUserProfileSnapshot {

        @Test
        fun `delegates to remote and returns its result`() = runTest {
            // ----- Arrange -----
            val expected = UserProfileSnapshot(
                profileImageUrl = "https://example.com/avatar.png",
                name = "Jane Doe",
                bio = "Avid reader",
                booksRead = 42,
                totalPagesRead = 12345,
                averageRating = 4.2,
                activeReadingDates = setOf(LocalDate.of(2026, 5, 4)),
            )

            coEvery {
                profileRemoteDataSource.getUserProfileSnapshot(userId = 42)
            } returns expected

            // ----- Act -----
            val result = repository.getUserProfileSnapshot(userId = 42)

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
            val thrownError = runCatching { repository.getUserProfileSnapshot(userId = 42) }.exceptionOrNull()

            // ----- Assert -----
            thrownError shouldBe expectedError
        }
    }
}
