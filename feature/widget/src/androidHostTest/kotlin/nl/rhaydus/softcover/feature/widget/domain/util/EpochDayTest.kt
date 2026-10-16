package nl.rhaydus.softcover.feature.widget.domain.util

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class EpochDayTest {
    private val firstDay = 20_000L
    private val startOfFirstDayMillis = firstDay * 86_400_000L

    private fun TestScope.clockAt(offsetMillis: Long): Clock = object : Clock {
        override fun now(): Instant =
            Instant.fromEpochMilliseconds(startOfFirstDayMillis + offsetMillis + testScheduler.currentTime)
    }

    @Nested
    inner class EpochDayChanges {
        @Test
        fun `emits today first`() = runTest {
            // ----- Arrange -----
            val flow = epochDayChanges(
                clock = clockAt(offsetMillis = 10.hours.inWholeMilliseconds),
                timeZone = { TimeZone.UTC },
            )

            // ----- Act & Assert -----
            flow.test {
                awaitItem() shouldBe firstDay
            }
        }

        @Test
        fun `emits the next day once local midnight passes`() = runTest {
            // ----- Arrange -----
            val flow = epochDayChanges(
                clock = clockAt(offsetMillis = 23.hours.inWholeMilliseconds),
                timeZone = { TimeZone.UTC },
            )

            // ----- Act & Assert -----
            flow.test {
                awaitItem() shouldBe firstDay
                awaitItem() shouldBe firstDay + 1
            }
        }

        @Test
        fun `emits nothing more while the day is still running`() = runTest {
            // ----- Arrange -----
            val flow = epochDayChanges(
                clock = clockAt(offsetMillis = 23.hours.inWholeMilliseconds),
                timeZone = { TimeZone.UTC },
            )

            // ----- Act & Assert -----
            flow.test {
                awaitItem() shouldBe firstDay
                testScheduler.advanceTimeBy(59.minutes)
                expectNoEvents()
            }
        }
    }
}
