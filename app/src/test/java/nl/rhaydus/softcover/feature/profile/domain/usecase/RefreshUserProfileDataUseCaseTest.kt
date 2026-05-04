package nl.rhaydus.softcover.feature.profile.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.profile.domain.model.UserProfileSnapshot
import nl.rhaydus.softcover.feature.profile.domain.repository.ProfileRepository
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class RefreshUserProfileDataUseCaseTest {

    private val fixedClock: Clock = Clock.fixed(
        Instant.parse("2026-05-04T12:00:00Z"),
        ZoneOffset.UTC,
    )

    private lateinit var profileRepository: ProfileRepository
    private lateinit var getUserIdUseCase: GetUserIdUseCase
    private lateinit var useCase: RefreshUserProfileDataUseCase

    @BeforeEach
    fun setUp() {
        profileRepository = mockk()
        getUserIdUseCase = mockk()
        useCase = RefreshUserProfileDataUseCase(
            profileRepository = profileRepository,
            getUserIdUseCase = getUserIdUseCase,
            clock = fixedClock,
        )
    }

    private fun snapshot(activeReadingDates: Set<LocalDate>): UserProfileSnapshot = UserProfileSnapshot(
        profileImageUrl = "https://example.com/avatar.png",
        name = "Jane Doe",
        bio = "Avid reader",
        booksRead = 42,
        totalPagesRead = 12000,
        averageRating = 4.2,
        activeReadingDates = activeReadingDates,
    )

    @Nested
    inner class Invoke {

        @Test
        fun `returns success and caches profile data when repository returns snapshot`() = runTest {
            // ----- Arrange -----
            val profileSnapshot = snapshot(activeReadingDates = emptySet())
            val expectedData = UserProfileData(
                profileImageUrl = profileSnapshot.profileImageUrl,
                name = profileSnapshot.name,
                bio = profileSnapshot.bio,
                booksRead = profileSnapshot.booksRead,
                totalPagesRead = profileSnapshot.totalPagesRead,
                averageRating = profileSnapshot.averageRating,
                readingStreak = 0,
            )

            coEvery { getUserIdUseCase() } returns Result.success(42)
            coEvery {
                profileRepository.fetchUserProfileSnapshot(userId = 42)
            } returns profileSnapshot
            coEvery {
                profileRepository.cacheUserProfileData(data = expectedData)
            } returns Unit

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 1) { profileRepository.cacheUserProfileData(data = expectedData) }
        }

        @Test
        fun `returns failure when getUserIdUseCase fails and never calls repository`() = runTest {
            // ----- Arrange -----
            val exception = RuntimeException("user id error")

            coEvery { getUserIdUseCase() } returns Result.failure(exception)

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
            coVerify(exactly = 0) { profileRepository.fetchUserProfileSnapshot(userId = any()) }
        }

        @Test
        fun `returns failure when fetchUserProfileSnapshot throws`() = runTest {
            // ----- Arrange -----
            val exception = RuntimeException("network error")

            coEvery { getUserIdUseCase() } returns Result.success(42)
            coEvery {
                profileRepository.fetchUserProfileSnapshot(userId = 42)
            } throws exception

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
        }
    }

    @Nested
    inner class ReadingStreak {

        private suspend fun streakFor(activeReadingDates: Set<LocalDate>): Int {
            val capturedData = mutableListOf<UserProfileData>()
            coEvery { getUserIdUseCase() } returns Result.success(42)
            coEvery {
                profileRepository.fetchUserProfileSnapshot(userId = 42)
            } returns snapshot(activeReadingDates = activeReadingDates)
            coEvery {
                profileRepository.cacheUserProfileData(data = any())
            } answers {
                capturedData.add(firstArg())
            }
            useCase()
            return capturedData.first().readingStreak
        }

        @Test
        fun `returns 0 when active reading dates is empty`() = runTest {
            // ----- Act & Assert -----
            streakFor(activeReadingDates = emptySet()) shouldBe 0
        }

        @Test
        fun `returns 1 when only today has an active reading date`() = runTest {
            // ----- Act & Assert -----
            streakFor(
                activeReadingDates = setOf(LocalDate.of(2026, 5, 4)),
            ) shouldBe 1
        }

        @Test
        fun `returns 3 when today, yesterday, and day-before-yesterday are all present`() = runTest {
            // ----- Act & Assert -----
            streakFor(
                activeReadingDates = setOf(
                    LocalDate.of(2026, 5, 4),
                    LocalDate.of(2026, 5, 3),
                    LocalDate.of(2026, 5, 2),
                ),
            ) shouldBe 3
        }

        @Test
        fun `returns 2 when yesterday and day-before are present but not today (grace day)`() = runTest {
            // ----- Act & Assert -----
            streakFor(
                activeReadingDates = setOf(
                    LocalDate.of(2026, 5, 3),
                    LocalDate.of(2026, 5, 2),
                ),
            ) shouldBe 2
        }

        @Test
        fun `returns 1 when today is present but yesterday is missing (gap)`() = runTest {
            // ----- Act & Assert -----
            streakFor(
                activeReadingDates = setOf(
                    LocalDate.of(2026, 5, 4),
                    LocalDate.of(2026, 5, 2),
                ),
            ) shouldBe 1
        }

        @Test
        fun `returns 0 when only an older date outside the grace window is present`() = runTest {
            // ----- Act & Assert -----
            streakFor(
                activeReadingDates = setOf(LocalDate.of(2026, 5, 1)),
            ) shouldBe 0
        }
    }
}
