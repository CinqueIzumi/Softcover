package nl.rhaydus.softcover.core.uibinding.progress

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.progress.ProgressTimeUnit
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProgressTimeDraftTest {
    private val draft = ProgressTimeDraft(
        forSeconds = 100,
        hours = "1",
        minutes = "2",
        seconds = "3",
    )

    @Nested
    inner class WithText {
        @Test
        fun `replaces only the targeted unit`() {
            // ----- Arrange -----
            val replacement = "9"

            // ----- Act -----
            val hours = draft.withText(
                unit = ProgressTimeUnit.HOURS,
                text = replacement,
            )
            val minutes = draft.withText(
                unit = ProgressTimeUnit.MINUTES,
                text = replacement,
            )
            val seconds = draft.withText(
                unit = ProgressTimeUnit.SECONDS,
                text = replacement,
            )

            // ----- Assert -----
            hours shouldBe draft.copy(hours = replacement)
            minutes shouldBe draft.copy(minutes = replacement)
            seconds shouldBe draft.copy(seconds = replacement)
        }

        @Test
        fun `keeps empty text so the field can be cleared`() {
            // ----- Arrange -----
            val unit = ProgressTimeUnit.MINUTES
            val text = ""

            // ----- Act -----
            val result = draft.withText(
                unit = unit,
                text = text,
            )

            // ----- Assert -----
            result.minutes shouldBe ""
        }

        @Test
        fun `clamps minutes above 59`() {
            // ----- Arrange -----
            val unit = ProgressTimeUnit.MINUTES
            val text = "75"

            // ----- Act -----
            val result = draft.withText(
                unit = unit,
                text = text,
            )

            // ----- Assert -----
            result.minutes shouldBe "59"
        }

        @Test
        fun `clamps seconds above 59`() {
            // ----- Arrange -----
            val unit = ProgressTimeUnit.SECONDS
            val text = "99"

            // ----- Act -----
            val result = draft.withText(
                unit = unit,
                text = text,
            )

            // ----- Assert -----
            result.seconds shouldBe "59"
        }

        @Test
        fun `does not clamp hours to 59`() {
            // ----- Arrange -----
            val unit = ProgressTimeUnit.HOURS
            val text = "120"

            // ----- Act -----
            val result = draft.withText(
                unit = unit,
                text = text,
            )

            // ----- Assert -----
            result.hours shouldBe "120"
        }

        @Test
        fun `normalises leading zeros`() {
            // ----- Arrange -----
            val unit = ProgressTimeUnit.SECONDS
            val text = "07"

            // ----- Act -----
            val result = draft.withText(
                unit = unit,
                text = text,
            )

            // ----- Assert -----
            result.seconds shouldBe "7"
        }

        @Test
        fun `clamps a negative value to zero`() {
            // ----- Arrange -----
            val unit = ProgressTimeUnit.MINUTES
            val text = "-5"

            // ----- Act -----
            val result = draft.withText(
                unit = unit,
                text = text,
            )

            // ----- Assert -----
            result.minutes shouldBe "0"
        }

        @Test
        fun `ignores text that is not a number`() {
            // ----- Arrange -----
            val unit = ProgressTimeUnit.MINUTES
            val text = "ab"

            // ----- Act -----
            val result = draft.withText(
                unit = unit,
                text = text,
            )

            // ----- Assert -----
            result shouldBe draft
        }

        @Test
        fun `ignores a number too large for an int`() {
            // ----- Arrange -----
            val unit = ProgressTimeUnit.HOURS
            val text = "99999999999"

            // ----- Act -----
            val result = draft.withText(
                unit = unit,
                text = text,
            )

            // ----- Assert -----
            result shouldBe draft
        }
    }

    @Nested
    inner class SteppedMinutes {
        @Test
        fun `adds a positive delta`() {
            // ----- Arrange -----
            val delta = 5

            // ----- Act -----
            val result = draft.steppedMinutes(delta)

            // ----- Assert -----
            result.minutes shouldBe "7"
        }

        @Test
        fun `subtracts a negative delta`() {
            // ----- Arrange -----
            val delta = -1

            // ----- Act -----
            val result = draft.steppedMinutes(delta)

            // ----- Assert -----
            result.minutes shouldBe "1"
        }

        @Test
        fun `stops at zero`() {
            // ----- Arrange -----
            val delta = -10

            // ----- Act -----
            val result = draft.steppedMinutes(delta)

            // ----- Assert -----
            result.minutes shouldBe "0"
        }

        @Test
        fun `stops at 59 without carrying into hours`() {
            // ----- Arrange -----
            val delta = 100

            // ----- Act -----
            val result = draft.steppedMinutes(delta)

            // ----- Assert -----
            result.minutes shouldBe "59"
            result.hours shouldBe "1"
        }

        @Test
        fun `treats cleared minutes as zero`() {
            // ----- Arrange -----
            val cleared = draft.copy(minutes = "")

            // ----- Act -----
            val result = cleared.steppedMinutes(5)

            // ----- Assert -----
            result.minutes shouldBe "5"
        }
    }

    @Nested
    inner class Resolve {
        @Test
        fun `returns the stored draft when it was seeded from the current position`() {
            // ----- Arrange -----
            val stored = draft
            val currentSeconds = 100

            // ----- Act -----
            val result = ProgressTimeDraft.resolve(
                stored = stored,
                currentSeconds = currentSeconds,
            )

            // ----- Assert -----
            result shouldBe draft
        }

        @Test
        fun `seeds a fresh draft when nothing is stored`() {
            // ----- Arrange -----
            val stored = null
            val currentSeconds = 3_725

            // ----- Act -----
            val result = ProgressTimeDraft.resolve(
                stored = stored,
                currentSeconds = currentSeconds,
            )

            // ----- Assert -----
            result shouldBe ProgressTimeDraft(
                forSeconds = 3_725,
                hours = "1",
                minutes = "2",
                seconds = "5",
            )
        }

        @Test
        fun `discards a stored draft seeded from a different position`() {
            // ----- Arrange -----
            val stored = draft
            val currentSeconds = 59

            // ----- Act -----
            val result = ProgressTimeDraft.resolve(
                stored = stored,
                currentSeconds = currentSeconds,
            )

            // ----- Assert -----
            result shouldBe ProgressTimeDraft(
                forSeconds = 59,
                hours = "0",
                minutes = "0",
                seconds = "59",
            )
        }

        @Test
        fun `seeds zeros at position zero`() {
            // ----- Arrange -----
            val stored = null
            val currentSeconds = 0

            // ----- Act -----
            val result = ProgressTimeDraft.resolve(
                stored = stored,
                currentSeconds = currentSeconds,
            )

            // ----- Assert -----
            result shouldBe ProgressTimeDraft(
                forSeconds = 0,
                hours = "0",
                minutes = "0",
                seconds = "0",
            )
        }
    }
}
