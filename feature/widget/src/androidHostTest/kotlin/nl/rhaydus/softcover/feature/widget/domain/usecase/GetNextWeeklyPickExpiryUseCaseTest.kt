package nl.rhaydus.softcover.feature.widget.domain.usecase

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickCadence
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickSettings
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickState
import nl.rhaydus.softcover.feature.widget.domain.repository.RandomPickRepository
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class GetNextWeeklyPickExpiryUseCaseTest {
    private val now = Instant.fromEpochMilliseconds(1_700_000_000_000L)
    private val clock = object : Clock {
        override fun now(): Instant = now
    }
    private val repository = mockk<RandomPickRepository>()
    private val useCase = GetNextWeeklyPickExpiryUseCase(
        randomPickRepository = repository,
        clock = clock,
    )

    private fun stubInstance(
        appWidgetId: Int,
        cadence: RandomPickCadence,
        pickedAt: Instant?,
    ) {
        coEvery {
            repository.getSettings(appWidgetId = appWidgetId)
        } returns RandomPickSettings(cadence = cadence)
        coEvery {
            repository.getState(appWidgetId = appWidgetId)
        } returns RandomPickState(
            pickedBookId = 1,
            pickedAtMillis = pickedAt?.toEpochMilliseconds(),
        )
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns the earliest of several future expiries`() = runTest {
            // ----- Arrange -----
            stubInstance(
                appWidgetId = 1,
                cadence = RandomPickCadence.Weekly,
                pickedAt = now - 1.days,
            )
            stubInstance(
                appWidgetId = 2,
                cadence = RandomPickCadence.Weekly,
                pickedAt = now - 5.days,
            )
            stubInstance(
                appWidgetId = 3,
                cadence = RandomPickCadence.Weekly,
                pickedAt = now - 3.days,
            )

            // ----- Act -----
            val result = useCase(appWidgetIds = listOf(1, 2, 3))

            // ----- Assert -----
            result.getOrNull() shouldBe now + 2.days
        }

        @Test
        fun `skips an expiry in the past in favour of a later one`() = runTest {
            // ----- Arrange -----
            stubInstance(
                appWidgetId = 1,
                cadence = RandomPickCadence.Weekly,
                pickedAt = now - 8.days,
            )
            stubInstance(
                appWidgetId = 2,
                cadence = RandomPickCadence.Weekly,
                pickedAt = now - 2.days,
            )

            // ----- Act -----
            val result = useCase(appWidgetIds = listOf(1, 2))

            // ----- Assert -----
            result.getOrNull() shouldBe now + 5.days
        }

        @Test
        fun `returns null when every expiry is past or not weekly`() = runTest {
            // ----- Arrange -----
            stubInstance(
                appWidgetId = 1,
                cadence = RandomPickCadence.Weekly,
                pickedAt = now - 7.days,
            )
            stubInstance(
                appWidgetId = 2,
                cadence = RandomPickCadence.Daily,
                pickedAt = now - 1.days,
            )
            stubInstance(
                appWidgetId = 3,
                cadence = RandomPickCadence.OnShuffleOnly,
                pickedAt = now - 1.days,
            )

            // ----- Act -----
            val result = useCase(appWidgetIds = listOf(1, 2, 3))

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull().shouldBeNull()
        }

        @Test
        fun `surfaces a repository failure as a failed result`() = runTest {
            // ----- Arrange -----
            val failure = IllegalStateException("store unavailable")
            coEvery {
                repository.getSettings(appWidgetId = 1)
            } throws failure

            // ----- Act -----
            val result = useCase(appWidgetIds = listOf(1))

            // ----- Assert -----
            result.exceptionOrNull() shouldBe failure
        }
    }
}
