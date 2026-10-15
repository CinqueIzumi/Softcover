package nl.rhaydus.softcover.core.profile.domain.usecase

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import nl.rhaydus.softcover.core.domain.activity.MarkReadingActivityUseCase
import nl.rhaydus.softcover.core.profile.domain.repository.ProfileRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.Instant

class MarkReadingActivityUseCaseTest {
    // Fixed instant: 2026-06-19T23:30:00Z — UTC pinned as a deterministic test timezone, not production behavior
    private val fixedClock: Clock = object : Clock {
        override fun now(): Instant = Instant.parse("2026-06-19T23:30:00Z")
    }

    private lateinit var profileRepository: ProfileRepository
    private lateinit var useCase: MarkReadingActivityUseCase

    @BeforeEach
    fun setUp() {
        profileRepository = mockk(relaxed = true)
        useCase = MarkReadingActivityUseCaseImpl(
            profileRepository = profileRepository,
            clock = fixedClock,
            timeZone = TimeZone.UTC,
        )
    }

    @Nested
    inner class Invoke {
        @Test
        fun `marks today when actionAt is null`() = runTest {
            // ----- Arrange -----
            val expected = LocalDate(
                year = 2026,
                month = 6,
                day = 19,
            )

            // ----- Act -----
            useCase(actionAt = null)

            // ----- Assert -----
            coVerify(exactly = 1) { profileRepository.markActiveReadingDate(date = expected) }
        }

        @Test
        fun `marks the backdated day when actionAt is in the past`() = runTest {
            // ----- Arrange -----
            val expected = LocalDate(
                year = 2026,
                month = 6,
                day = 10,
            )

            // ----- Act -----
            useCase(actionAt = "2026-06-10T12:00:00Z")

            // ----- Assert -----
            coVerify(exactly = 1) { profileRepository.markActiveReadingDate(date = expected) }
        }

        @Test
        fun `buckets actionAt by the injected timezone`() = runTest {
            // ----- Arrange -----
            val expected = LocalDate(
                year = 2026,
                month = 6,
                day = 10,
            )
            val amsterdamUseCase = MarkReadingActivityUseCaseImpl(
                profileRepository = profileRepository,
                clock = fixedClock,
                timeZone = TimeZone.of("Europe/Amsterdam"),
            )

            // ----- Act -----
            // 22:30Z on the 9th is 00:30 on the 10th in Amsterdam (UTC+2 in June)
            amsterdamUseCase(actionAt = "2026-06-09T22:30:00Z")

            // ----- Assert -----
            coVerify(exactly = 1) { profileRepository.markActiveReadingDate(date = expected) }
        }

        @Test
        fun `clamps a future actionAt to today`() = runTest {
            // ----- Arrange -----
            val expected = LocalDate(
                year = 2026,
                month = 6,
                day = 19,
            )

            // ----- Act -----
            useCase(actionAt = "2026-06-25T08:00:00Z")

            // ----- Assert -----
            coVerify(exactly = 1) { profileRepository.markActiveReadingDate(date = expected) }
        }

        @Test
        fun `does not throw or mark a day when actionAt is malformed`() = runTest {
            // ----- Arrange -----

            // ----- Act -----
            useCase(actionAt = "not-a-date")

            // ----- Assert -----
            coVerify(exactly = 0) { profileRepository.markActiveReadingDate(any()) }
        }

        @Test
        fun `swallows a non-cancellation failure`() = runTest {
            // ----- Arrange -----
            coEvery {
                profileRepository.markActiveReadingDate(any())
            } throws RuntimeException("io")

            // ----- Act & Assert -----
            useCase(actionAt = null)
        }

        @Test
        fun `rethrows CancellationException`() = runTest {
            // ----- Arrange -----
            coEvery {
                profileRepository.markActiveReadingDate(any())
            } throws CancellationException("cancelled")

            // ----- Act & Assert -----
            shouldThrow<CancellationException> {
                useCase(actionAt = null)
            }
        }
    }
}
