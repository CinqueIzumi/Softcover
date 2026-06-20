package nl.rhaydus.softcover.core.profile.domain.usecase

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.activity.MarkReadingActivityTodayUseCase
import nl.rhaydus.softcover.core.profile.domain.repository.ProfileRepository

class MarkReadingActivityTodayUseCaseTest {
    // Fixed instant: 2026-06-19T23:30:00Z — still the 19th in UTC
    private val fixedClock: Clock = object : Clock {
        override fun now(): Instant = Instant.parse("2026-06-19T23:30:00Z")
    }

    private lateinit var profileRepository: ProfileRepository
    private lateinit var useCase: MarkReadingActivityTodayUseCase

    @BeforeEach
    fun setUp() {
        profileRepository = mockk(relaxed = true)
        useCase = MarkReadingActivityTodayUseCaseImpl(
            profileRepository = profileRepository,
            clock = fixedClock,
        )
    }

    @Nested
    inner class Invoke {
        @Test
        fun `forwards today's UTC date to repository`() = runTest {
            // ----- Arrange -----

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerify(exactly = 1) {
                profileRepository.markActiveReadingDate(date = LocalDate(
                    2026,
                    6,
                    19,
                ),)
            }
        }

        @Test
        fun `swallows a non-cancellation failure`() = runTest {
            // ----- Arrange -----
            coEvery {
                profileRepository.markActiveReadingDate(any())
            } throws RuntimeException("io")

            // ----- Act & Assert -----
            useCase()
        }

        @Test
        fun `rethrows CancellationException`() = runTest {
            // ----- Arrange -----
            coEvery {
                profileRepository.markActiveReadingDate(any())
            } throws kotlin.coroutines.cancellation.CancellationException("cancelled")

            // ----- Act & Assert -----
            shouldThrow<kotlin.coroutines.cancellation.CancellationException> {
                useCase()
            }
        }
    }
}
