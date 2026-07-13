package nl.rhaydus.softcover.core.profile.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
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
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.core.profile.domain.repository.ProfileRepository

class ObserveRecentReadingActivityUseCaseTest {
    // Fixed today: 2026-05-04, so window is 2026-04-14 … 2026-05-04
    private val fixedClock: Clock = object : Clock {
        override fun now(): Instant = Instant.parse("2026-05-04T12:00:00Z")
    }
    private val today: LocalDate = LocalDate(
        2026,
        5,
        4,
    )

    private lateinit var profileRepository: ProfileRepository
    private lateinit var useCase: ObserveRecentReadingActivityUseCase

    @BeforeEach
    fun setUp() {
        profileRepository = mockk()
        useCase = ObserveRecentReadingActivityUseCase(
            profileRepository = profileRepository,
            clock = fixedClock,
            timeZone = TimeZone.UTC,
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
        recentReadingDays = dates,
    )

    @Nested
    inner class Invoke {
        @Test
        fun `emits exactly 21 entries`() = runTest {
            // ----- Arrange -----
            every {
                profileRepository.observeUserProfileData()
            } returns flowOf(profileWith(emptySet()))

            // ----- Act & Assert -----
            useCase().test {
                awaitItem().size shouldBe 21
                awaitComplete()
            }
        }

        @Test
        fun `entries are in ascending date order and last entry date equals today`() = runTest {
            // ----- Arrange -----
            every {
                profileRepository.observeUserProfileData()
            } returns flowOf(profileWith(emptySet()))

            // ----- Act & Assert -----
            useCase().test {
                val items = awaitItem()
                items.zipWithNext { a, b -> (a.date < b.date) shouldBe true }
                items.last().date shouldBe today
                awaitComplete()
            }
        }

        @Test
        fun `first entry date is 20 days before today`() = runTest {
            // ----- Arrange -----
            every {
                profileRepository.observeUserProfileData()
            } returns flowOf(profileWith(emptySet()))

            // ----- Act & Assert -----
            useCase().test {
                val items = awaitItem()
                items.first().date shouldBe today.minus(
                    20,
                    DateTimeUnit.DAY,
                )
                awaitComplete()
            }
        }

        @Test
        fun `all entries have didRead false when active dates set is empty`() = runTest {
            // ----- Arrange -----
            every {
                profileRepository.observeUserProfileData()
            } returns flowOf(profileWith(emptySet()))

            // ----- Act & Assert -----
            useCase().test {
                awaitItem().all { it.didRead.not() } shouldBe true
                awaitComplete()
            }
        }

        @Test
        fun `null profile emits 21 entries all with didRead false`() = runTest {
            // ----- Arrange -----
            every {
                profileRepository.observeUserProfileData()
            } returns flowOf(null)

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
            val activeDates = setOf(today, today.minus(
                3,
                DateTimeUnit.DAY,
            ), today.minus(
                10,
                DateTimeUnit.DAY,
            ),)

            every {
                profileRepository.observeUserProfileData()
            } returns flowOf(profileWith(activeDates))

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
            val windowStart = today.minus(
                20,
                DateTimeUnit.DAY,
            )
            val activeDates = (0..16).map { windowStart.plus(
                it,
                DateTimeUnit.DAY,
            ) }.toSet() +
                setOf(
                    windowStart.plus(
                        18,
                        DateTimeUnit.DAY,
                    ),
                    windowStart.plus(
                        19,
                        DateTimeUnit.DAY,
                    ),
                    windowStart.plus(
                        20,
                        DateTimeUnit.DAY,
                    ),
                )
            val gapDate = windowStart.plus(
                17,
                DateTimeUnit.DAY,
            )

            every {
                profileRepository.observeUserProfileData()
            } returns flowOf(profileWith(activeDates))

            // ----- Act & Assert -----
            useCase().test {
                val items = awaitItem()
                val byDate = items.associateBy { it.date }

                byDate[gapDate]?.didRead shouldBe false
                byDate[windowStart.plus(
                    16,
                    DateTimeUnit.DAY,
                )]?.didRead shouldBe true
                byDate[windowStart.plus(
                    18,
                    DateTimeUnit.DAY,
                )]?.didRead shouldBe true
                awaitComplete()
            }
        }

        @Test
        fun `dates outside the 21-day window present in the set do not produce true entries`() = runTest {
            // ----- Arrange -----
            // A date 22 days ago is outside the window, so it must not light up any strip dot.
            val outsideDate = today.minus(
                22,
                DateTimeUnit.DAY,
            )
            val activeDates = setOf(outsideDate)

            every {
                profileRepository.observeUserProfileData()
            } returns flowOf(profileWith(activeDates))

            // ----- Act & Assert -----
            useCase().test {
                val items = awaitItem()
                items.any { it.didRead } shouldBe false
                awaitComplete()
            }
        }
    }
}
