package nl.rhaydus.softcover.core.uibinding.progress

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.progress.ProgressSheetMedium
import nl.rhaydus.softcover.core.component.progress.ProgressSheetUiModel
import nl.rhaydus.softcover.core.component.progress.ProgressTimeUnit
import io.kotest.matchers.types.shouldBeInstanceOf
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
            // ----- Act -----
            val hours = draft.withText(
                unit = ProgressTimeUnit.HOURS,
                text = "9",
            )
            val minutes = draft.withText(
                unit = ProgressTimeUnit.MINUTES,
                text = "9",
            )
            val seconds = draft.withText(
                unit = ProgressTimeUnit.SECONDS,
                text = "9",
            )

            // ----- Assert -----
            hours shouldBe draft.copy(hours = "9")
            minutes shouldBe draft.copy(minutes = "9")
            seconds shouldBe draft.copy(seconds = "9")
        }

        @Test
        fun `keeps empty text so the field can be cleared`() {
            // ----- Act -----
            val result = draft.withText(
                unit = ProgressTimeUnit.MINUTES,
                text = "",
            )

            // ----- Assert -----
            result.minutes shouldBe ""
        }

        @Test
        fun `clamps minutes above 59`() {
            // ----- Act -----
            val result = draft.withText(
                unit = ProgressTimeUnit.MINUTES,
                text = "75",
            )

            // ----- Assert -----
            result.minutes shouldBe "59"
        }

        @Test
        fun `clamps seconds above 59`() {
            // ----- Act -----
            val result = draft.withText(
                unit = ProgressTimeUnit.SECONDS,
                text = "99",
            )

            // ----- Assert -----
            result.seconds shouldBe "59"
        }

        @Test
        fun `does not clamp hours to 59`() {
            // ----- Act -----
            val result = draft.withText(
                unit = ProgressTimeUnit.HOURS,
                text = "120",
            )

            // ----- Assert -----
            result.hours shouldBe "120"
        }

        @Test
        fun `normalises leading zeros`() {
            // ----- Act -----
            val result = draft.withText(
                unit = ProgressTimeUnit.SECONDS,
                text = "07",
            )

            // ----- Assert -----
            result.seconds shouldBe "7"
        }

        @Test
        fun `clamps a negative value to zero`() {
            // ----- Act -----
            val result = draft.withText(
                unit = ProgressTimeUnit.MINUTES,
                text = "-5",
            )

            // ----- Assert -----
            result.minutes shouldBe "0"
        }

        @Test
        fun `ignores text that is not a number`() {
            // ----- Act -----
            val result = draft.withText(
                unit = ProgressTimeUnit.MINUTES,
                text = "ab",
            )

            // ----- Assert -----
            result shouldBe draft
        }

        @Test
        fun `ignores a number too large for an int`() {
            // ----- Act -----
            val result = draft.withText(
                unit = ProgressTimeUnit.HOURS,
                text = "99999999999",
            )

            // ----- Assert -----
            result shouldBe draft
        }

        @Test
        fun `preserves the seed position`() {
            // ----- Act -----
            val result = draft.withText(
                unit = ProgressTimeUnit.HOURS,
                text = "4",
            )

            // ----- Assert -----
            result.forSeconds shouldBe 100
        }
    }

    @Nested
    inner class SteppedMinutes {
        @Test
        fun `adds a positive delta`() {
            // ----- Act -----
            val result = draft.steppedMinutes(5)

            // ----- Assert -----
            result.minutes shouldBe "7"
        }

        @Test
        fun `subtracts a negative delta`() {
            // ----- Act -----
            val result = draft.steppedMinutes(-1)

            // ----- Assert -----
            result.minutes shouldBe "1"
        }

        @Test
        fun `stops at zero`() {
            // ----- Act -----
            val result = draft.steppedMinutes(-10)

            // ----- Assert -----
            result.minutes shouldBe "0"
        }

        @Test
        fun `stops at 59 without carrying into hours`() {
            // ----- Act -----
            val result = draft.steppedMinutes(100)

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

        @Test
        fun `leaves hours and seconds untouched`() {
            // ----- Act -----
            val result = draft.steppedMinutes(1)

            // ----- Assert -----
            result.hours shouldBe "1"
            result.seconds shouldBe "3"
        }
    }

    @Nested
    inner class Resolve {
        @Test
        fun `returns the stored draft when it was seeded from the current position`() {
            // ----- Act -----
            val result = ProgressTimeDraft.resolve(
                stored = draft,
                currentSeconds = 100,
            )

            // ----- Assert -----
            result shouldBe draft
        }

        @Test
        fun `seeds a fresh draft when nothing is stored`() {
            // ----- Act -----
            val result = ProgressTimeDraft.resolve(
                stored = null,
                currentSeconds = 3_725,
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
            // ----- Act -----
            val result = ProgressTimeDraft.resolve(
                stored = draft,
                currentSeconds = 59,
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
            // ----- Act -----
            val result = ProgressTimeDraft.resolve(
                stored = null,
                currentSeconds = 0,
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

    @Nested
    inner class WithTimeDraft {
        private val timedModel = ProgressSheetUiModel.previews.first { it.medium is ProgressSheetMedium.Timed }
        private val pagedModel = ProgressSheetUiModel.previews.first { it.medium is ProgressSheetMedium.Paged }

        @Test
        fun `writes the draft text onto the three timed fields`() {
            // ----- Act -----
            val result = timedModel.withTimeDraft(draft)

            // ----- Assert -----
            val medium = result.medium.shouldBeInstanceOf<ProgressSheetMedium.Timed>()
            medium.hours.text shouldBe "1"
            medium.minutes.text shouldBe "2"
            medium.seconds.text shouldBe "3"
        }

        @Test
        fun `leaves the totals and the field keys alone`() {
            // ----- Arrange -----
            val before = timedModel.medium.shouldBeInstanceOf<ProgressSheetMedium.Timed>()

            // ----- Act -----
            val result = timedModel.withTimeDraft(draft)

            // ----- Assert -----
            val after = result.medium.shouldBeInstanceOf<ProgressSheetMedium.Timed>()
            after.totalSeconds shouldBe before.totalSeconds
            after.currentSeconds shouldBe before.currentSeconds
            after.hours.key shouldBe before.hours.key
            after.minutes.key shouldBe before.minutes.key
            after.seconds.key shouldBe before.seconds.key
        }

        @Test
        fun `leaves everything outside the medium alone`() {
            // ----- Act -----
            val result = timedModel.withTimeDraft(draft)

            // ----- Assert -----
            result.tabs shouldBe timedModel.tabs
            result.scaffold shouldBe timedModel.scaffold
            result.progressPercent shouldBe timedModel.progressPercent
        }

        @Test
        fun `returns a paged model unchanged`() {
            // ----- Act -----
            val result = pagedModel.withTimeDraft(draft)

            // ----- Assert -----
            result shouldBe pagedModel
        }

        @Test
        fun `keeps cleared text so the field can be emptied`() {
            // ----- Arrange -----
            val cleared = draft.withText(
                unit = ProgressTimeUnit.HOURS,
                text = "",
            )

            // ----- Act -----
            val result = timedModel.withTimeDraft(cleared)

            // ----- Assert -----
            val medium = result.medium.shouldBeInstanceOf<ProgressSheetMedium.Timed>()
            medium.hours.text shouldBe ""
        }
    }
}
