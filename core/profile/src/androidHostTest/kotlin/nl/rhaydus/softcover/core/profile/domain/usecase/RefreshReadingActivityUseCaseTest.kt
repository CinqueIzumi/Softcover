package nl.rhaydus.softcover.core.profile.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.core.profile.domain.ProfileRefreshGate
import nl.rhaydus.softcover.core.profile.domain.repository.ProfileRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RefreshReadingActivityUseCaseTest {
    private val fixedClock: Clock = object : Clock {
        override fun now(): Instant = Instant.parse("2026-05-04T12:00:00Z")
    }

    // Fixed clock's "today" and the window start derived from READING_ACTIVITY_WINDOW_DAYS (21).
    private val today = LocalDate(
        2026,
        5,
        4,
    )
    private val windowStart = LocalDate(
        2026,
        4,
        14,
    )

    private fun day(offset: Int): LocalDate = today.minus(
        offset,
        DateTimeUnit.DAY,
    )

    private lateinit var profileRepository: ProfileRepository
    private lateinit var getUserIdUseCase: GetUserIdUseCase

    // A real gate (not mocked): it has no external dependencies, and using the real
    // check-fetch-mark logic is what lets the GateBehavior tests below observe it actually
    // skipping or retrying the network fetch across successive useCase() calls.
    private lateinit var activityRefreshGate: ProfileRefreshGate
    private lateinit var useCase: RefreshReadingActivityUseCase

    @BeforeEach
    fun setUp() {
        profileRepository = mockk()
        getUserIdUseCase = mockk()
        activityRefreshGate = ProfileRefreshGate()
        useCase = RefreshReadingActivityUseCase(
            profileRepository = profileRepository,
            getUserIdUseCase = getUserIdUseCase,
            activityRefreshGate = activityRefreshGate,
            clock = fixedClock,
            timeZone = TimeZone.UTC,
        )
    }

    // Captures the streak and recent-days arguments passed to cacheUserProfileActivity after
    // driving the use case with the given descending reading-day stream.
    private suspend fun capturedActivityFor(streamedDaysDescending: List<LocalDate>): Pair<Int, Set<LocalDate>> {
        val streakSlot = slot<Int>()
        val recentDaysSlot = slot<Set<LocalDate>>()

        coEvery {
            getUserIdUseCase()
        } returns Result.success(42)
        coEvery {
            profileRepository.streamReadingDaysDescending(userId = 42)
        } returns flowOf(*streamedDaysDescending.toTypedArray())
        coEvery {
            profileRepository.cacheUserProfileActivity(
                readingStreak = capture(streakSlot),
                recentReadingDays = capture(recentDaysSlot),
            )
        } returns Unit

        useCase()

        return streakSlot.captured to recentDaysSlot.captured
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns success and caches the streak and recent reading days`() = runTest {
            // ----- Arrange -----
            coEvery {
                getUserIdUseCase()
            } returns Result.success(42)
            coEvery {
                profileRepository.streamReadingDaysDescending(userId = 42)
            } returns flowOf(today)
            coEvery {
                profileRepository.cacheUserProfileActivity(
                    readingStreak = 1,
                    recentReadingDays = setOf(today),
                )
            } returns Unit

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 1) {
                profileRepository.cacheUserProfileActivity(
                    readingStreak = 1,
                    recentReadingDays = setOf(today),
                )
            }
        }

        @Test
        fun `returns failure when getUserIdUseCase fails and never streams reading days`() = runTest {
            // ----- Arrange -----
            val exception = RuntimeException("user id error")

            coEvery {
                getUserIdUseCase()
            } returns Result.failure(exception)

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
            coVerify(exactly = 0) { profileRepository.streamReadingDaysDescending(userId = any()) }
        }

        @Test
        fun `returns failure when the reading-day stream throws`() = runTest {
            // ----- Arrange -----
            val exception = RuntimeException("network error")

            coEvery {
                getUserIdUseCase()
            } returns Result.success(42)
            coEvery {
                profileRepository.streamReadingDaysDescending(userId = 42)
            } returns flow { throw exception }

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
        }

        @Test
        fun `returns failure when cacheUserProfileActivity throws`() = runTest {
            // ----- Arrange -----
            val exception = RuntimeException("cache write failed")

            coEvery {
                getUserIdUseCase()
            } returns Result.success(42)
            coEvery {
                profileRepository.streamReadingDaysDescending(userId = 42)
            } returns flowOf(today)
            coEvery {
                profileRepository.cacheUserProfileActivity(
                    readingStreak = any(),
                    recentReadingDays = any(),
                )
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
        @Test
        fun `returns 0 when the reading-day stream is empty`() = runTest {
            // ----- Act & Assert -----
            capturedActivityFor(streamedDaysDescending = emptyList()).first shouldBe 0
        }

        @Test
        fun `returns 1 when today is absent but yesterday is present (grace day)`() = runTest {
            // ----- Act & Assert -----
            capturedActivityFor(streamedDaysDescending = listOf(day(1))).first shouldBe 1
        }

        @Test
        fun `returns short streak when the stream has an early gap`() = runTest {
            // ----- Arrange -----
            // today and yesterday are consecutive, then a gap (day-before-yesterday missing)
            // before an older, unrelated date further back in the descending stream.
            val streamedDays = listOf(
                day(0),
                day(1),
                day(3),
            )

            // ----- Act & Assert -----
            capturedActivityFor(streamedDaysDescending = streamedDays).first shouldBe 2
        }

        @Test
        fun `returns a streak longer than the reading-activity window when the stream has 30 consecutive days`() =
            runTest {
                // ----- Arrange -----
                // 30 consecutive days ending on today, strictly descending. Only the most recent
                // 21 days fall inside the activity window, but the streak itself is unbounded —
                // it must not be truncated by any window-like limit.
                val streamedDays = (0 until 30).map(::day)

                // ----- Act & Assert -----
                capturedActivityFor(streamedDaysDescending = streamedDays).first shouldBe 30
            }
    }

    @Nested
    inner class RecentReadingDays {
        @Test
        fun `includes only dates within the 21-day window`() = runTest {
            // ----- Arrange -----
            // One date inside the window, one date exactly at the boundary, one date just
            // outside it.
            val insideDate = day(5)
            val boundaryDate = windowStart
            val outsideDate = day(21)
            val streamedDays = listOf(
                insideDate,
                boundaryDate,
                outsideDate,
            )

            // ----- Act -----
            val (_, recentReadingDays) = capturedActivityFor(streamedDaysDescending = streamedDays)

            // ----- Assert -----
            recentReadingDays shouldBe setOf(insideDate, boundaryDate)
        }

        @Test
        fun `still includes in-window dates that come after an early streak-breaking gap`() = runTest {
            // ----- Arrange -----
            // The streak breaks after two days, but the window must keep collecting every
            // in-window date regardless of where the streak itself stopped.
            val streamedDays = listOf(
                day(0),
                day(1),
                day(5),
                day(10),
            )

            // ----- Act -----
            val (readingStreak, recentReadingDays) = capturedActivityFor(streamedDaysDescending = streamedDays)

            // ----- Assert -----
            readingStreak shouldBe 2
            recentReadingDays shouldBe setOf(day(0), day(1), day(5), day(10))
        }
    }

    @Nested
    inner class SinglePassEarlyCancellation {
        private fun pagedFlow(
            pages: List<List<LocalDate>>,
            pagesRequested: MutableList<Int>,
        ): Flow<LocalDate> = flow {
            pages.forEachIndexed { index, page ->
                pagesRequested.add(index + 1)
                page.forEach { date -> emit(date) }
            }
        }

        @Test
        fun `stops paging once the streak is determined and the window boundary is crossed`() = runTest {
            // ----- Arrange -----
            // A 200-day account history: today, yesterday, and the day before are present; the
            // day before that (day 3) is a genuine gap; every day from day 4 through day 200 is
            // present. The streak (3) is determined as soon as day 4 is seen (it confirms day 3
            // is a real gap, not just unfetched), but the combined predicate must keep paging
            // until it also crosses the 21-day window boundary (day 21) before stopping — so
            // pagination should stop after page 5, never touching the other 35 available pages.
            val presentDays = listOf(0, 1, 2).map(::day) + (4..200).map(::day)
            val pageSize = 5
            val pages = presentDays.chunked(pageSize)
            val pagesRequested = mutableListOf<Int>()

            coEvery {
                getUserIdUseCase()
            } returns Result.success(42)
            coEvery {
                profileRepository.streamReadingDaysDescending(userId = 42)
            } returns pagedFlow(
                pages = pages,
                pagesRequested = pagesRequested,
            )
            coEvery {
                profileRepository.cacheUserProfileActivity(
                    readingStreak = any(),
                    recentReadingDays = any(),
                )
            } returns Unit

            // ----- Act -----
            useCase()

            // ----- Assert -----
            pagesRequested shouldBe listOf(1, 2, 3, 4, 5)
            coVerify(exactly = 1) { profileRepository.streamReadingDaysDescending(userId = 42) }
        }

        @Test
        fun `caches the streak and window derived from the single paged pass`() = runTest {
            // ----- Arrange -----
            // Same dataset as above — asserts the values the early-cancelled pass actually
            // produced, not just that it stopped early.
            val presentDays = listOf(0, 1, 2).map(::day) + (4..200).map(::day)
            val pages = presentDays.chunked(5)
            val streakSlot = slot<Int>()
            val recentDaysSlot = slot<Set<LocalDate>>()

            coEvery {
                getUserIdUseCase()
            } returns Result.success(42)
            coEvery {
                profileRepository.streamReadingDaysDescending(userId = 42)
            } returns pagedFlow(
                pages = pages,
                pagesRequested = mutableListOf(),
            )
            coEvery {
                profileRepository.cacheUserProfileActivity(
                    readingStreak = capture(streakSlot),
                    recentReadingDays = capture(recentDaysSlot),
                )
            } returns Unit

            // ----- Act -----
            useCase()

            // ----- Assert -----
            val expectedRecentReadingDays = (0..20).map(::day).toSet() - day(3)

            streakSlot.captured shouldBe 3
            recentDaysSlot.captured shouldBe expectedRecentReadingDays
        }
    }

    @Nested
    inner class GateBehavior {
        private fun arrangeSuccessfulRepository() {
            coEvery {
                getUserIdUseCase()
            } returns Result.success(42)
            coEvery {
                profileRepository.streamReadingDaysDescending(userId = 42)
            } returns flowOf()
            coEvery {
                profileRepository.cacheUserProfileActivity(
                    readingStreak = any(),
                    recentReadingDays = any(),
                )
            } returns Unit
        }

        @Test
        fun `a second invoke within the same session skips the network fetch entirely`() = runTest {
            // ----- Arrange -----
            arrangeSuccessfulRepository()

            // ----- Act -----
            useCase()
            useCase()

            // ----- Assert -----
            coVerify(exactly = 1) { profileRepository.streamReadingDaysDescending(userId = 42) }
        }

        @Test
        fun `a failed fetch leaves the gate open so the next invoke retries`() = runTest {
            // ----- Arrange -----
            var fetchCallCount = 0

            coEvery {
                getUserIdUseCase()
            } returns Result.success(42)
            coEvery {
                profileRepository.streamReadingDaysDescending(userId = 42)
            } answers {
                fetchCallCount++
                if (fetchCallCount == 1) flow { throw RuntimeException("network error") } else flowOf()
            }
            coEvery {
                profileRepository.cacheUserProfileActivity(
                    readingStreak = any(),
                    recentReadingDays = any(),
                )
            } returns Unit

            // ----- Act -----
            val firstResult = useCase()
            val secondResult = useCase()

            // ----- Assert -----
            firstResult.isFailure shouldBe true
            secondResult.isSuccess shouldBe true
            coVerify(exactly = 2) { profileRepository.streamReadingDaysDescending(userId = 42) }
        }
    }
}
