package nl.rhaydus.softcover.showcase.live.progress.mapper

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import nl.rhaydus.softcover.core.component.progress.ProgressSheetMedium
import nl.rhaydus.softcover.core.component.progress.ProgressSheetTab
import nl.rhaydus.softcover.core.component.progress.ProgressSheetUiModel
import nl.rhaydus.softcover.core.component.progress.ProgressTimeUnit
import nl.rhaydus.softcover.showcase.live.progress.model.ProgressDemo
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProgressLiveMapperTest {
    private fun timed(model: ProgressSheetUiModel): ProgressSheetMedium.Timed =
        model.medium.shouldBeInstanceOf<ProgressSheetMedium.Timed>()

    @Nested
    inner class SelectingTab {
        @Test
        fun `shows the chosen tab when the medium offers it`() {
            // ----- Arrange -----
            val model = progressDemoModel(demo = ProgressDemo.PAGES)

            // ----- Act -----
            val result = model.selectingTab(tab = ProgressSheetTab.PERCENTAGE)

            // ----- Assert -----
            result.tabs.selected shouldBe ProgressSheetTab.PERCENTAGE
        }

        @Test
        fun `falls back to the primary tab when the medium does not offer the chosen one`() {
            // ----- Arrange -----
            val model = progressDemoModel(demo = ProgressDemo.NO_TOTAL)

            // ----- Act -----
            val result = model.selectingTab(tab = ProgressSheetTab.PERCENTAGE)

            // ----- Assert -----
            result.tabs.selected shouldBe ProgressSheetTab.PAGE
        }
    }

    @Nested
    inner class WithTime {
        @Test
        fun `leaves a paged sheet untouched`() {
            // ----- Arrange -----
            val model = progressDemoModel(demo = ProgressDemo.PAGES)

            // ----- Act -----
            val result = model.withTime(
                unit = ProgressTimeUnit.HOURS,
                text = "4",
            )

            // ----- Assert -----
            result shouldBeSameInstanceAs model
        }

        @Test
        fun `sets the hours field without touching the minute flags`() {
            // ----- Arrange -----
            val model = progressDemoModel(demo = ProgressDemo.AUDIOBOOK)
            val before = timed(model)

            // ----- Act -----
            val result = timed(
                model.withTime(
                    unit = ProgressTimeUnit.HOURS,
                    text = "9",
                ),
            )

            // ----- Assert -----
            result.hours.text shouldBe "9"
            result.canDecreaseMinutes shouldBe before.canDecreaseMinutes
            result.canIncreaseMinutes shouldBe before.canIncreaseMinutes
        }

        @Test
        fun `sets the seconds field without touching the minute flags`() {
            // ----- Arrange -----
            val model = progressDemoModel(demo = ProgressDemo.AUDIOBOOK)
            val before = timed(model)

            // ----- Act -----
            val result = timed(
                model.withTime(
                    unit = ProgressTimeUnit.SECONDS,
                    text = "30",
                ),
            )

            // ----- Assert -----
            result.seconds.text shouldBe "30"
            result.canDecreaseMinutes shouldBe before.canDecreaseMinutes
            result.canIncreaseMinutes shouldBe before.canIncreaseMinutes
        }

        @Test
        fun `allows both directions for minutes in the middle of the range`() {
            // ----- Arrange -----
            val model = progressDemoModel(demo = ProgressDemo.AUDIOBOOK)

            // ----- Act -----
            val result = timed(
                model.withTime(
                    unit = ProgressTimeUnit.MINUTES,
                    text = "30",
                ),
            )

            // ----- Assert -----
            result.minutes.text shouldBe "30"
            result.canDecreaseMinutes shouldBe true
            result.canIncreaseMinutes shouldBe true
        }

        @Test
        fun `blocks decreasing at zero minutes`() {
            // ----- Arrange -----
            val model = progressDemoModel(demo = ProgressDemo.AUDIOBOOK)

            // ----- Act -----
            val result = timed(
                model.withTime(
                    unit = ProgressTimeUnit.MINUTES,
                    text = "0",
                ),
            )

            // ----- Assert -----
            result.canDecreaseMinutes shouldBe false
            result.canIncreaseMinutes shouldBe true
        }

        @Test
        fun `blocks increasing at fifty-nine minutes`() {
            // ----- Arrange -----
            val model = progressDemoModel(demo = ProgressDemo.AUDIOBOOK)

            // ----- Act -----
            val result = timed(
                model.withTime(
                    unit = ProgressTimeUnit.MINUTES,
                    text = "59",
                ),
            )

            // ----- Assert -----
            result.canDecreaseMinutes shouldBe true
            result.canIncreaseMinutes shouldBe false
        }

        @Test
        fun `treats blank minutes as zero`() {
            // ----- Arrange -----
            val model = progressDemoModel(demo = ProgressDemo.AUDIOBOOK)

            // ----- Act -----
            val result = timed(
                model.withTime(
                    unit = ProgressTimeUnit.MINUTES,
                    text = "",
                ),
            )

            // ----- Assert -----
            result.minutes.text shouldBe ""
            result.canDecreaseMinutes shouldBe false
            result.canIncreaseMinutes shouldBe true
        }
    }

    @Nested
    inner class SteppingMinutes {
        @Test
        fun `leaves a paged sheet untouched`() {
            // ----- Arrange -----
            val model = progressDemoModel(demo = ProgressDemo.PAGES)

            // ----- Act -----
            val result = model.steppingMinutes(delta = 1)

            // ----- Assert -----
            result shouldBeSameInstanceAs model
        }

        @Test
        fun `adds the delta to the current minutes`() {
            // ----- Arrange -----
            val model = progressDemoModel(demo = ProgressDemo.AUDIOBOOK)

            // ----- Act -----
            val result = timed(model.steppingMinutes(delta = 5))

            // ----- Assert -----
            result.minutes.text shouldBe "25"
        }

        @Test
        fun `subtracts a negative delta`() {
            // ----- Arrange -----
            val model = progressDemoModel(demo = ProgressDemo.AUDIOBOOK)

            // ----- Act -----
            val result = timed(model.steppingMinutes(delta = -5))

            // ----- Assert -----
            result.minutes.text shouldBe "15"
        }

        @Test
        fun `clamps at fifty-nine and blocks further increase`() {
            // ----- Arrange -----
            val model = progressDemoModel(demo = ProgressDemo.AUDIOBOOK)

            // ----- Act -----
            val result = timed(model.steppingMinutes(delta = 100))

            // ----- Assert -----
            result.minutes.text shouldBe "59"
            result.canIncreaseMinutes shouldBe false
            result.canDecreaseMinutes shouldBe true
        }

        @Test
        fun `clamps at zero and blocks further decrease`() {
            // ----- Arrange -----
            val model = progressDemoModel(demo = ProgressDemo.AUDIOBOOK)

            // ----- Act -----
            val result = timed(model.steppingMinutes(delta = -100))

            // ----- Assert -----
            result.minutes.text shouldBe "0"
            result.canDecreaseMinutes shouldBe false
            result.canIncreaseMinutes shouldBe true
        }

        @Test
        fun `steps from blank minutes as if they were zero`() {
            // ----- Arrange -----
            val model = progressDemoModel(demo = ProgressDemo.AUDIOBOOK).withTime(
                unit = ProgressTimeUnit.MINUTES,
                text = "",
            )

            // ----- Act -----
            val result = timed(model.steppingMinutes(delta = 1))

            // ----- Assert -----
            result.minutes.text shouldBe "1"
        }
    }
}
