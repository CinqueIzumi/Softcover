package nl.rhaydus.softcover.feature.settings.presentation.util

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class SecretTapCounterTest {
    @Nested
    inner class RegisterTap {
        @Test
        fun `first six taps one second apart return false and the seventh returns true`() {
            // ----- Arrange -----
            val counter = SecretTapCounter()
            val base = Instant.fromEpochMilliseconds(0)

            // ----- Act -----
            val results = (0 until 7).map { index ->
                counter.registerTap(at = base + (index * 1000).milliseconds)
            }

            // ----- Assert -----
            results.take(6).forEach { it shouldBe false }
            results.last() shouldBe true
        }

        @Test
        fun `a gap longer than the reset window restarts the count so a full seven-tap run is needed from the late tap`() {
            // ----- Arrange -----
            val counter = SecretTapCounter()
            val base = Instant.fromEpochMilliseconds(0)

            // ----- Act -----
            counter.registerTap(at = base)
            counter.registerTap(at = base + 1.seconds)
            counter.registerTap(at = base + 2.seconds)

            // gap of 5s, well over the 2s reset window, restarts the run at the late tap (count = 1),
            // so six more taps are needed to reach the seventh tap of the new run
            val lateTap = counter.registerTap(at = base + 7.seconds)
            val results = (1..6).map { offset ->
                counter.registerTap(at = base + 7.seconds + (offset * 1000).milliseconds)
            }

            // ----- Assert -----
            lateTap shouldBe false
            results.dropLast(1).forEach { it shouldBe false }
            results.last() shouldBe true
        }

        @Test
        fun `a tap exactly at the reset window boundary continues the run rather than restarting it`() {
            // ----- Arrange -----
            val counter = SecretTapCounter()
            val base = Instant.fromEpochMilliseconds(0)

            // ----- Act -----
            counter.registerTap(at = base)

            // exactly resetWindow later - the implementation's `<=` should treat this as a continuation
            val boundaryTap = counter.registerTap(at = base + 2.seconds)
            val results = (1..5).map { offset ->
                counter.registerTap(at = base + 2.seconds + offset.seconds)
            }

            // ----- Assert -----
            boundaryTap shouldBe false
            // if the boundary tap had instead restarted the run, five more taps would not be enough
            // to reach the seventh tap - only six of the required seven would have been registered
            results.dropLast(1).forEach { it shouldBe false }
            results.last() shouldBe true
        }

        @Test
        fun `unlocking resets the counter so a second run of seven taps unlocks again`() {
            // ----- Arrange -----
            val counter = SecretTapCounter()
            val base = Instant.fromEpochMilliseconds(0)

            // ----- Act -----
            val firstRun = (0 until 7).map { index ->
                counter.registerTap(at = base + (index * 1000).milliseconds)
            }
            val secondRun = (0 until 7).map { index ->
                counter.registerTap(at = base + 100.seconds + (index * 1000).milliseconds)
            }

            // ----- Assert -----
            firstRun.last() shouldBe true
            secondRun.take(6).forEach { it shouldBe false }
            secondRun.last() shouldBe true
        }

        @Test
        fun `the very first tap on a fresh counter counts as tap one, not tap two`() {
            // ----- Arrange -----
            val counter = SecretTapCounter(requiredTaps = 1)

            // ----- Act -----
            val result = counter.registerTap(at = Instant.fromEpochMilliseconds(0))

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `honours an injected requiredTaps smaller than the production default`() {
            // ----- Arrange -----
            val counter = SecretTapCounter(
                requiredTaps = 3,
                resetWindow = 500.milliseconds,
            )
            val base = Instant.fromEpochMilliseconds(0)

            // ----- Act -----
            val first = counter.registerTap(at = base)
            val second = counter.registerTap(at = base + 300.milliseconds)
            val third = counter.registerTap(at = base + 600.milliseconds)

            // ----- Assert -----
            first shouldBe false
            second shouldBe false
            third shouldBe true
        }

        @Test
        fun `honours an injected resetWindow shorter than the production default`() {
            // ----- Arrange -----
            val counter = SecretTapCounter(
                requiredTaps = 3,
                resetWindow = 500.milliseconds,
            )
            val base = Instant.fromEpochMilliseconds(0)

            // ----- Act -----
            val first = counter.registerTap(at = base)

            // 600ms gap exceeds the injected 500ms reset window, so this restarts the run
            val restarted = counter.registerTap(at = base + 600.milliseconds)
            val third = counter.registerTap(at = base + 900.milliseconds)
            val fourth = counter.registerTap(at = base + 1200.milliseconds)

            // ----- Assert -----
            first shouldBe false
            restarted shouldBe false
            third shouldBe false
            fourth shouldBe true
        }
    }
}
