package nl.rhaydus.softcover.showcase.live.quote.mapper

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import nl.rhaydus.softcover.core.component.progress.ProgressTimeUnit
import nl.rhaydus.softcover.core.component.quote.QuotePositionInputUiModel
import nl.rhaydus.softcover.core.component.quote.QuotePositionTab
import nl.rhaydus.softcover.core.component.quote.QuoteSheetUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.showcase.live.quote.model.QuoteDemo
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class QuoteLiveMapperTest {
    private fun canSave(model: QuoteSheetUiModel): Boolean = model.scaffold.footer
        .shouldBeInstanceOf<SheetFooterUiModel.Stacked>()
        .primary
        .shouldNotBeNull()
        .enabled

    @Nested
    inner class WithText {
        @Test
        fun `stores the text and enables Save when there are words`() {
            // ----- Arrange -----
            val model = quoteDemoModel(demo = QuoteDemo.EMPTY)

            // ----- Act -----
            val result = model.withText(text = "My name is Piranesi.")

            // ----- Assert -----
            result.text.text shouldBe "My name is Piranesi."
            canSave(result) shouldBe true
        }

        @Test
        fun `keeps Save disabled for whitespace only`() {
            // ----- Arrange -----
            val model = quoteDemoModel(demo = QuoteDemo.EMPTY)

            // ----- Act -----
            val result = model.withText(text = "   \n ")

            // ----- Assert -----
            canSave(result) shouldBe false
        }

        @Test
        fun `disables Save again when the text is cleared`() {
            // ----- Arrange -----
            val model = quoteDemoModel(demo = QuoteDemo.PAGE)

            // ----- Act -----
            val result = model.withText(text = "")

            // ----- Assert -----
            canSave(model) shouldBe true
            canSave(result) shouldBe false
        }
    }

    @Nested
    inner class SelectingTab {
        @Test
        fun `switches to a blank page entry with the page total`() {
            // ----- Arrange -----
            val model = quoteDemoModel(demo = QuoteDemo.TIME)

            // ----- Act -----
            val result = model.selectingTab(tab = QuotePositionTab.PAGE)

            // ----- Assert -----
            result.positionTabs.selected shouldBe QuotePositionTab.PAGE
            val page = result.position.shouldBeInstanceOf<QuotePositionInputUiModel.Page>()
            page.field.text shouldBe ""
            page.suffix shouldBe "of 437 pages"
        }

        @Test
        fun `switches to a blank percentage entry and drops the earlier page`() {
            // ----- Arrange -----
            val model = quoteDemoModel(demo = QuoteDemo.PAGE)

            // ----- Act -----
            val result = model.selectingTab(tab = QuotePositionTab.PERCENTAGE)

            // ----- Assert -----
            result.positionTabs.selected shouldBe QuotePositionTab.PERCENTAGE
            result.position.shouldBeInstanceOf<QuotePositionInputUiModel.Percentage>().field.text shouldBe ""
        }

        @Test
        fun `switches to a blank time entry`() {
            // ----- Arrange -----
            val model = quoteDemoModel(demo = QuoteDemo.PAGE)

            // ----- Act -----
            val result = model.selectingTab(tab = QuotePositionTab.TIME)

            // ----- Assert -----
            result.positionTabs.selected shouldBe QuotePositionTab.TIME
            val time = result.position.shouldBeInstanceOf<QuotePositionInputUiModel.Time>()
            listOf(time.hours.text, time.minutes.text, time.seconds.text) shouldBe listOf("", "", "")
        }
    }

    @Nested
    inner class WithTime {
        @Test
        fun `leaves a non-time position untouched`() {
            // ----- Arrange -----
            val model = quoteDemoModel(demo = QuoteDemo.PAGE)

            // ----- Act -----
            val result = model.withTime(
                unit = ProgressTimeUnit.HOURS,
                text = "3",
            )

            // ----- Assert -----
            result shouldBeSameInstanceAs model
        }

        @Test
        fun `sets only the field of the given unit`() {
            // ----- Arrange -----
            val model = quoteDemoModel(demo = QuoteDemo.PAGE).selectingTab(tab = QuotePositionTab.TIME)

            // ----- Act -----
            val result = model
                .withTime(
                    unit = ProgressTimeUnit.HOURS,
                    text = "2",
                )
                .withTime(
                    unit = ProgressTimeUnit.SECONDS,
                    text = "15",
                )

            // ----- Assert -----
            val time = result.position.shouldBeInstanceOf<QuotePositionInputUiModel.Time>()
            time.hours.text shouldBe "2"
            time.minutes.text shouldBe ""
            time.seconds.text shouldBe "15"
        }
    }
}
