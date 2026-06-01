package nl.rhaydus.softcover.core.profile.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileSnapshot
import nl.rhaydus.softcover.core.profile.domain.repository.ProfileRepository
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
        username = "cinque",
        bio = "Avid reader",
        booksRead = 42,
        totalPagesRead = 12000,
        averageRating = 4.2,
        activeReadingDates = activeReadingDates,
    )

    private suspend fun capturedDataFor(activeReadingDates: Set<LocalDate>): UserProfileData {
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

        return capturedData.first()
    }

    @Nested
    inner class Invoke {

        @Test
        fun `returns success and caches profile data when repository returns snapshot`() = runTest {
            // ----- Arrange -----
            val profileSnapshot = snapshot(activeReadingDates = emptySet())
            val expectedData = UserProfileData(
                profileImageUrl = profileSnapshot.profileImageUrl,
                name = profileSnapshot.name,
                username = profileSnapshot.username,
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

        @Test
        fun `username from snapshot propagates into the cached UserProfileData`() = runTest {
            // ----- Act -----
            val cached = capturedDataFor(activeReadingDates = emptySet())

            // ----- Assert -----
            cached.username shouldBe "cinque"
        }
    }

    @Nested
    inner class ReadingStreak {

        private suspend fun streakFor(activeReadingDates: Set<LocalDate>): Int =
            capturedDataFor(activeReadingDates).readingStreak

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

    @Nested
    inner class ActiveReadingDatesWindowing {

        // Fixed today in this test class: 2026-05-04
        // Window start: today.minusDays(20) = 2026-04-14
        // Dates before 2026-04-14 must be dropped from the cached model.

        @Test
        fun `cached activeReadingDates does not contain dates older than 20 days`() = runTest {
            // ----- Arrange -----
            val insideWindow = setOf(
                LocalDate.of(2026, 4, 14), // exactly the window start — must be kept
                LocalDate.of(2026, 4, 20),
                LocalDate.of(2026, 5, 4),
            )
            val outsideWindow = setOf(
                LocalDate.of(2026, 4, 13), // one day before window start — must be dropped
                LocalDate.of(2026, 3, 1),
            )

            // ----- Act -----
            val cached = capturedDataFor(activeReadingDates = insideWindow + outsideWindow)

            // ----- Assert -----
            cached.activeReadingDates shouldBe insideWindow
        }

        @Test
        fun `cached activeReadingDates does not contain future dates`() = runTest {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 5, 4)
            val futureDate = today.plusDays(1)
            val validDate = today.minusDays(1)

            // ----- Act -----
            val cached = capturedDataFor(
                activeReadingDates = setOf(today, validDate, futureDate),
            )

            // ----- Assert -----
            cached.activeReadingDates.contains(futureDate) shouldBe false
        }

        @Test
        fun `readingStreak uses the full snapshot set so a streak longer than 21 days is correct`() = runTest {
            // ----- Arrange -----
            // Build 30 consecutive days ending on today (2026-05-04).
            // Only the last 21 days fall within the window, but the streak is 30.
            val today = LocalDate.of(2026, 5, 4)
            val thirtyDays = (0L until 30L).map { today.minusDays(it) }.toSet()

            // ----- Act -----
            val cached = capturedDataFor(activeReadingDates = thirtyDays)

            // ----- Assert -----
            cached.readingStreak shouldBe 30
            // The cached date set is limited to the window.
            cached.activeReadingDates.size shouldBe 21
        }

        @Test
        fun `cached activeReadingDates is empty when all snapshot dates are outside the window`() = runTest {
            // ----- Arrange -----
            val oldDates = setOf(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2025, 12, 15),
            )

            // ----- Act -----
            val cached = capturedDataFor(activeReadingDates = oldDates)

            // ----- Assert -----
            cached.activeReadingDates shouldBe emptySet()
        }
    }
}
