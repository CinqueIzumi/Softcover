package nl.rhaydus.softcover.core.profile.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlinx.datetime.TimeZone
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileSnapshot
import nl.rhaydus.softcover.core.profile.domain.repository.ProfileRepository

class RefreshUserProfileDataUseCaseTest {
    private val fixedClock: Clock = object : Clock {
        override fun now(): Instant = Instant.parse("2026-05-04T12:00:00Z")
    }

    // Fixed clock's "today" and the window start derived from READING_ACTIVITY_WINDOW_DAYS (21).
    private val today = LocalDate(2026, 5, 4)
    private val windowStart = LocalDate(2026, 4, 14)

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
            timeZone = TimeZone.UTC,
        )
    }

    private fun snapshot(): UserProfileSnapshot = UserProfileSnapshot(
        profileImageUrl = "https://example.com/avatar.png",
        name = "Jane Doe",
        username = "cinque",
        bio = "Avid reader",
        booksRead = 42,
        totalPagesRead = 12000,
        averageRating = 4.2,
    )

    // Drives readingStreak via streamReadingDaysDescending (must be strictly descending) and
    // recentReadingDays via getActiveReadingDaysSince — the two are independent inputs.
    private suspend fun capturedDataFor(
        streamedDaysDescending: List<LocalDate>,
        activeReadingDaysSince: Set<LocalDate>,
    ): UserProfileData {
        val capturedData = mutableListOf<UserProfileData>()

        coEvery { getUserIdUseCase() } returns Result.success(42)
        coEvery { profileRepository.fetchUserProfileSnapshot() } returns snapshot()

        coEvery {
            profileRepository.streamReadingDaysDescending(userId = 42)
        } returns flowOf(*streamedDaysDescending.toTypedArray())

        coEvery {
            profileRepository.getActiveReadingDaysSince(userId = 42, since = windowStart)
        } returns activeReadingDaysSince

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
            val profileSnapshot = snapshot()
            val expectedData = UserProfileData(
                profileImageUrl = profileSnapshot.profileImageUrl,
                name = profileSnapshot.name,
                username = profileSnapshot.username,
                bio = profileSnapshot.bio,
                booksRead = profileSnapshot.booksRead,
                totalPagesRead = profileSnapshot.totalPagesRead,
                averageRating = profileSnapshot.averageRating,
                readingStreak = 0,
                recentReadingDays = emptySet(),
            )

            coEvery { getUserIdUseCase() } returns Result.success(42)
            coEvery { profileRepository.fetchUserProfileSnapshot() } returns profileSnapshot

            coEvery {
                profileRepository.streamReadingDaysDescending(userId = 42)
            } returns flowOf()

            coEvery {
                profileRepository.getActiveReadingDaysSince(userId = 42, since = windowStart)
            } returns emptySet()

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
            coVerify(exactly = 0) { profileRepository.fetchUserProfileSnapshot() }
            coVerify(exactly = 0) { profileRepository.streamReadingDaysDescending(userId = any()) }
        }

        @Test
        fun `returns failure when fetchUserProfileSnapshot throws`() = runTest {
            // ----- Arrange -----
            val exception = RuntimeException("network error")

            coEvery { getUserIdUseCase() } returns Result.success(42)

            coEvery {
                profileRepository.fetchUserProfileSnapshot()
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
            val cached = capturedDataFor(
                streamedDaysDescending = emptyList(),
                activeReadingDaysSince = emptySet(),
            )

            // ----- Assert -----
            cached.username shouldBe "cinque"
        }
    }

    @Nested
    inner class ReadingStreak {
        private suspend fun streakFor(streamedDaysDescending: List<LocalDate>): Int =
            capturedDataFor(
                streamedDaysDescending = streamedDaysDescending,
                activeReadingDaysSince = emptySet(),
            ).readingStreak

        @Test
        fun `returns 0 when the reading-day stream is empty`() = runTest {
            // ----- Act & Assert -----
            streakFor(streamedDaysDescending = emptyList()) shouldBe 0
        }

        @Test
        fun `returns 1 when today is absent but yesterday is present (grace day)`() = runTest {
            // ----- Act & Assert -----
            streakFor(
                streamedDaysDescending = listOf(today.minus(1, DateTimeUnit.DAY)),
            ) shouldBe 1
        }

        @Test
        fun `returns short streak when the stream has an early gap`() = runTest {
            // ----- Arrange -----
            // today and yesterday are consecutive, then a gap (day-before-yesterday missing)
            // before an older, unrelated date further back in the descending stream.
            val streamedDays = listOf(
                today,
                today.minus(1, DateTimeUnit.DAY),
                today.minus(3, DateTimeUnit.DAY),
            )

            // ----- Act & Assert -----
            streakFor(streamedDaysDescending = streamedDays) shouldBe 2
        }

        @Test
        fun `returns a streak longer than the reading-activity window when the stream has 30 consecutive days`() =
            runTest {
                // ----- Arrange -----
                // 30 consecutive days ending on today, strictly descending. Only the most recent
                // 21 days fall inside the activity window, but the streak itself is unbounded —
                // it must not be truncated by any page-like limit.
                val streamedDays = (0 until 30).map { today.minus(it, DateTimeUnit.DAY) }

                // ----- Act & Assert -----
                streakFor(streamedDaysDescending = streamedDays) shouldBe 30
            }
    }

    @Nested
    inner class RecentReadingDaysIndependence {
        @Test
        fun `recentReadingDays reflects getActiveReadingDaysSince regardless of where the streamed streak breaks`() =
            runTest {
                // ----- Arrange -----
                // The streamed streak breaks after two days, but getActiveReadingDaysSince
                // returns an entirely different, older set of in-window days — the two inputs
                // must stay independent.
                val streamedDays = listOf(
                    today,
                    today.minus(1, DateTimeUnit.DAY),
                    today.minus(5, DateTimeUnit.DAY),
                )
                val recentReadingDays = setOf(
                    windowStart,
                    windowStart.plus(2, DateTimeUnit.DAY),
                )

                // ----- Act -----
                val cached = capturedDataFor(
                    streamedDaysDescending = streamedDays,
                    activeReadingDaysSince = recentReadingDays,
                )

                // ----- Assert -----
                cached.recentReadingDays shouldBe recentReadingDays
                coVerify(exactly = 1) {
                    profileRepository.getActiveReadingDaysSince(userId = 42, since = windowStart)
                }
            }

        @Test
        fun `readingStreak is unaffected by an unrelated or empty getActiveReadingDaysSince result`() = runTest {
            // ----- Arrange -----
            // A real streak comes from the streamed days while getActiveReadingDaysSince returns
            // an unrelated, empty set — the streak must be driven only by the streamed days.
            val streamedDays = listOf(
                today,
                today.minus(1, DateTimeUnit.DAY),
                today.minus(2, DateTimeUnit.DAY),
            )

            // ----- Act -----
            val cached = capturedDataFor(
                streamedDaysDescending = streamedDays,
                activeReadingDaysSince = emptySet(),
            )

            // ----- Assert -----
            cached.readingStreak shouldBe 3
        }
    }
}
