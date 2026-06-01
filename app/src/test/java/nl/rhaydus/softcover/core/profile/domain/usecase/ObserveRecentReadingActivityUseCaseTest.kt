package nl.rhaydus.softcover.core.profile.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.core.profile.domain.repository.ProfileRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class ObserveRecentReadingActivityUseCaseTest {

    // Fixed today: 2026-05-04, so window is 2026-04-14 … 2026-05-04
    private val fixedClock: Clock = Clock.fixed(
        Instant.parse("2026-05-04T12:00:00Z"),
        ZoneOffset.UTC,
    )
    private val today: LocalDate = LocalDate.of(2026, 5, 4)

    private lateinit var profileRepository: ProfileRepository
    private lateinit var useCase: ObserveRecentReadingActivityUseCase

    @BeforeEach
    fun setUp() {
        profileRepository = mockk()
        useCase = ObserveRecentReadingActivityUseCase(
            profileRepository = profileRepository,
            clock = fixedClock,
        )
    }

    private fun profileWith(dates: Set<LocalDate>): UserProfileData = UserProfileData(
        profileImageUrl = "https://example.com/avatar.png",
        name = "Jane Doe",
        username = "cinque",
        bio = "Avid reader",
        booksRead = 42,
        totalPagesRead = 12000,
        averageRating = 4.2,
        readingStreak = 5,
        activeReadingDates = dates,
    )

    @Nested
    inner class Invoke {

        @Test
        fun `emits exactly 21 entries`() = runTest {
            // ----- Arrange -----
            every { profileRepository.observeUserProfileData() } returns flowOf(profileWith(emptySet()))

            // ----- Act & Assert -----
            useCase().test {
                awaitItem().size shouldBe 21
                awaitComplete()
            }
        }

        @Test
        fun `entries are in ascending date order and last entry date equals today`() = runTest {
            // ----- Arrange -----
            every { profileRepository.observeUserProfileData() } returns flowOf(profileWith(emptySet()))

            // ----- Act & Assert -----
            useCase().test {
                val items = awaitItem()
                items.zipWithNext { a, b -> a.date.isBefore(b.date) shouldBe true }
                items.last().date shouldBe today
                awaitComplete()
            }
        }

        @Test
        fun `first entry date is 20 days before today`() = runTest {
            // ----- Arrange -----
            every { profileRepository.observeUserProfileData() } returns flowOf(profileWith(emptySet()))

            // ----- Act & Assert -----
            useCase().test {
                val items = awaitItem()
                items.first().date shouldBe today.minusDays(20)
                awaitComplete()
            }
        }

        @Test
        fun `all entries have didRead false when active dates set is empty`() = runTest {
            // ----- Arrange -----
            every { profileRepository.observeUserProfileData() } returns flowOf(profileWith(emptySet()))

            // ----- Act & Assert -----
            useCase().test {
                awaitItem().all { it.didRead.not() } shouldBe true
                awaitComplete()
            }
        }

        @Test
        fun `null profile emits 21 entries all with didRead false`() = runTest {
            // ----- Arrange -----
            every { profileRepository.observeUserProfileData() } returns flowOf(null)

            // ----- Act & Assert -----
            useCase().test {
                val items = awaitItem()
                items.size shouldBe 21
                items.all { it.didRead.not() } shouldBe true
                awaitComplete()
            }
        }

        @Test
        fun `dates in the cached set within the window have didRead true`() = runTest {
            // ----- Arrange -----
            val activeDates = setOf(today, today.minusDays(3), today.minusDays(10))

            every { profileRepository.observeUserProfileData() } returns flowOf(profileWith(activeDates))

            // ----- Act & Assert -----
            useCase().test {
                val items = awaitItem()
                val trueItems = items.filter { it.didRead }
                trueItems.map { it.date }.toSet() shouldBe activeDates
                awaitComplete()
            }
        }

        @Test
        fun `gapped active set — gap day has didRead false and surrounding days have didRead true`() = runTest {
            // ----- Arrange -----
            // Read 17 days (offset 0–16), gap on offset 17, then 3 more (offset 18–20)
            val windowStart = today.minusDays(20)
            val activeDates = (0..16).map { windowStart.plusDays(it.toLong()) }.toSet() +
                setOf(
                    windowStart.plusDays(18),
                    windowStart.plusDays(19),
                    windowStart.plusDays(20),
                )
            val gapDate = windowStart.plusDays(17)

            every { profileRepository.observeUserProfileData() } returns flowOf(profileWith(activeDates))

            // ----- Act & Assert -----
            useCase().test {
                val items = awaitItem()
                val byDate = items.associateBy { it.date }

                byDate[gapDate]?.didRead shouldBe false
                byDate[windowStart.plusDays(16)]?.didRead shouldBe true
                byDate[windowStart.plusDays(18)]?.didRead shouldBe true
                awaitComplete()
            }
        }

        @Test
        fun `dates outside the 21-day window present in the set do not produce true entries`() = runTest {
            // ----- Arrange -----
            // A date 22 days ago is outside the window, so it must not light up any strip dot.
            val outsideDate = today.minusDays(22)
            val activeDates = setOf(outsideDate)

            every { profileRepository.observeUserProfileData() } returns flowOf(profileWith(activeDates))

            // ----- Act & Assert -----
            useCase().test {
                val items = awaitItem()
                items.any { it.didRead } shouldBe false
                awaitComplete()
            }
        }
    }
}
