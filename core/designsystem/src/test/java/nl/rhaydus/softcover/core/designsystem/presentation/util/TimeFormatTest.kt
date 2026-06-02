package nl.rhaydus.softcover.core.designsystem.presentation.util

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TimeFormatTest {

    // =========================================================
    // secondsToHm
    // =========================================================

    @Nested
    inner class SecondsToHm {

        @Test
        fun `formats hours and minutes when both are non-zero`() {
            // ----- Act -----
            val result = secondsToHm(3661)

            // ----- Assert -----
            result shouldBe "1h 1m"
        }

        @Test
        fun `always shows zero minutes when minutes component is zero`() {
            // ----- Act -----
            val result = secondsToHm(7200)

            // ----- Assert -----
            result shouldBe "2h 0m"
        }

        @Test
        fun `always shows zero hours when less than one hour`() {
            // ----- Act -----
            val result = secondsToHm(2700)

            // ----- Assert -----
            result shouldBe "0h 45m"
        }

        @Test
        fun `formats 0 as 0h 0m`() {
            // ----- Act -----
            val result = secondsToHm(0)

            // ----- Assert -----
            result shouldBe "0h 0m"
        }

        @Test
        fun `treats negative input as 0h 0m`() {
            // ----- Act -----
            val result = secondsToHm(-500)

            // ----- Assert -----
            result shouldBe "0h 0m"
        }

        @Test
        fun `formats large values with zero minutes`() {
            // ----- Act -----
            val result = secondsToHm(36000)

            // ----- Assert -----
            result shouldBe "10h 0m"
        }

        @Test
        fun `formats large value with hours and minutes`() {
            // ----- Act -----
            val result = secondsToHm(36090)

            // ----- Assert -----
            result shouldBe "10h 1m"
        }
    }

    // =========================================================
    // secondsToClock
    // =========================================================

    @Nested
    inner class SecondsToClock {

        @Test
        fun `formats zero as 00_00_00`() {
            // ----- Act -----
            val result = secondsToClock(0)

            // ----- Assert -----
            result shouldBe "00:00:00"
        }

        @Test
        fun `treats negative input as 00_00_00`() {
            // ----- Act -----
            val result = secondsToClock(-1)

            // ----- Assert -----
            result shouldBe "00:00:00"
        }

        @Test
        fun `formats exactly one hour as 01_00_00`() {
            // ----- Act -----
            val result = secondsToClock(3600)

            // ----- Assert -----
            result shouldBe "01:00:00"
        }

        @Test
        fun `formats 3723 seconds as 01_02_03`() {
            // ----- Act -----
            val result = secondsToClock(3723)

            // ----- Assert -----
            result shouldBe "01:02:03"
        }

        @Test
        fun `pads single-digit components with leading zero`() {
            // ----- Act -----
            val result = secondsToClock(61)

            // ----- Assert -----
            result shouldBe "00:01:01"
        }

        @Test
        fun `formats large hours-only value correctly`() {
            // ----- Act -----
            val result = secondsToClock(36000)

            // ----- Assert -----
            result shouldBe "10:00:00"
        }
    }

    // =========================================================
    // Int.toHoursMinutesSeconds
    // =========================================================

    @Nested
    inner class IntToHoursMinutesSeconds {

        @Test
        fun `converts zero to all-zero components`() {
            // ----- Act -----
            val result = 0.toHoursMinutesSeconds()

            // ----- Assert -----
            result.hours shouldBe 0
            result.minutes shouldBe 0
            result.seconds shouldBe 0
        }

        @Test
        fun `converts negative to all-zero components`() {
            // ----- Act -----
            val result = (-100).toHoursMinutesSeconds()

            // ----- Assert -----
            result.hours shouldBe 0
            result.minutes shouldBe 0
            result.seconds shouldBe 0
        }

        @Test
        fun `converts 3661 to 1 hour 1 minute 1 second`() {
            // ----- Act -----
            val result = 3661.toHoursMinutesSeconds()

            // ----- Assert -----
            result.hours shouldBe 1
            result.minutes shouldBe 1
            result.seconds shouldBe 1
        }

        @Test
        fun `converts 59 to 0 hours 0 minutes 59 seconds`() {
            // ----- Act -----
            val result = 59.toHoursMinutesSeconds()

            // ----- Assert -----
            result.hours shouldBe 0
            result.minutes shouldBe 0
            result.seconds shouldBe 59
        }

        @Test
        fun `totalSeconds round-trips through toHoursMinutesSeconds`() {
            // ----- Arrange -----
            val original = 7384

            // ----- Act -----
            val result = original.toHoursMinutesSeconds().totalSeconds

            // ----- Assert -----
            result shouldBe original
        }

        @Test
        fun `converts value with only minutes component`() {
            // ----- Act -----
            val result = 3600.toHoursMinutesSeconds()

            // ----- Assert -----
            result.hours shouldBe 1
            result.minutes shouldBe 0
            result.seconds shouldBe 0
        }
    }
}
