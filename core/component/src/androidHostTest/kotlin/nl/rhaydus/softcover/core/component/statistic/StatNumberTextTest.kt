package nl.rhaydus.softcover.core.component.statistic

import io.kotest.matchers.shouldBe
import nl.rhaydus.common.secondsToHm
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class StatNumberTextTest {
    private val plural = StatNumberSuffix.Plural(
        one = " day",
        other = " days",
    )

    private fun plainModel(
        value: Double,
        prefix: String = "",
        suffix: StatNumberSuffix? = null,
    ) = StatNumberUiModel(
        value = value,
        format = StatNumberFormat.Plain,
        prefix = prefix,
        suffix = suffix,
    )

    @Nested
    inner class TextAt {
        @Test
        fun `no prefix and no suffix renders the bare number`() {
            // ----- Arrange -----
            val model = plainModel(value = 30.0)

            // ----- Act -----
            val text = model.textAt(displayValue = 30.0)

            // ----- Assert -----
            text shouldBe "30"
        }

        @Test
        fun `fixed suffix is appended regardless of the value`() {
            // ----- Arrange -----
            val model = plainModel(
                value = 1.0,
                suffix = StatNumberSuffix.Fixed("%"),
            )

            // ----- Act -----
            val text = model.textAt(displayValue = 1.0)

            // ----- Assert -----
            text shouldBe "1%"
        }

        @Test
        fun `prefix number and suffix are assembled in order`() {
            // ----- Arrange -----
            val model = plainModel(
                value = 331.0,
                prefix = "Page ",
                suffix = StatNumberSuffix.Fixed(" of 400"),
            )

            // ----- Act -----
            val text = model.textAt(displayValue = 331.0)

            // ----- Assert -----
            text shouldBe "Page 331 of 400"
        }

        @Test
        fun `plural suffix picks one at exactly 1`() {
            // ----- Arrange -----
            val model = plainModel(
                value = 1.0,
                suffix = plural,
            )

            // ----- Act -----
            val text = model.textAt(displayValue = 1.0)

            // ----- Assert -----
            text shouldBe "1 day"
        }

        @Test
        fun `plural suffix picks other at 0`() {
            // ----- Arrange -----
            val model = plainModel(
                value = 0.0,
                suffix = plural,
            )

            // ----- Act -----
            val text = model.textAt(displayValue = 0.0)

            // ----- Assert -----
            text shouldBe "0 days"
        }

        @Test
        fun `plural suffix picks other at 2`() {
            // ----- Arrange -----
            val model = plainModel(
                value = 2.0,
                suffix = plural,
            )

            // ----- Act -----
            val text = model.textAt(displayValue = 2.0)

            // ----- Assert -----
            text shouldBe "2 days"
        }

        @Test
        fun `plural suffix stays one mid-spring at 1 point 6`() {
            // ----- Arrange -----
            val model = plainModel(
                value = 2.0,
                suffix = plural,
            )

            // ----- Act -----
            val text = model.textAt(displayValue = 1.6)

            // ----- Assert -----
            text shouldBe "1 day"
        }

        @Test
        fun `plural suffix switches to other once the spring reaches 2`() {
            // ----- Arrange -----
            val model = plainModel(
                value = 2.0,
                suffix = plural,
            )

            // ----- Act -----
            val text = model.textAt(displayValue = 2.0)

            // ----- Assert -----
            text shouldBe "2 days"
        }

        @Test
        fun `display value drives the text rather than the model value`() {
            // ----- Arrange -----
            val model = plainModel(value = 100.0)

            // ----- Act -----
            val text = model.textAt(displayValue = 42.0)

            // ----- Assert -----
            text shouldBe "42"
        }

        @Test
        fun `decimal format renders one fraction digit`() {
            // ----- Arrange -----
            val model = StatNumberUiModel(
                value = 4.0,
                format = StatNumberFormat.Decimal,
            )

            // ----- Act -----
            val text = model.textAt(displayValue = 4.0)

            // ----- Assert -----
            text.length shouldBe 3
            text.first() shouldBe '4'
            text.last() shouldBe '0'
        }

        @Test
        fun `duration format renders seconds as hours and minutes`() {
            // ----- Arrange -----
            val model = StatNumberUiModel(
                value = 5_400.0,
                format = StatNumberFormat.Duration,
            )

            // ----- Act -----
            val text = model.textAt(displayValue = 5_400.0)

            // ----- Assert -----
            text shouldBe secondsToHm(5_400)
        }
    }

    @Nested
    inner class StandIn {
        @Test
        fun `plain stand in is two zeros with no affixes`() {
            // ----- Arrange -----
            val model = plainModel(value = 7.0)

            // ----- Act -----
            val text = model.standIn()

            // ----- Assert -----
            text shouldBe "00"
        }

        @Test
        fun `stand in keeps the prefix and uses the plural other suffix`() {
            // ----- Arrange -----
            val model = plainModel(
                value = 1.0,
                prefix = "Streak: ",
                suffix = plural,
            )

            // ----- Act -----
            val text = model.standIn()

            // ----- Assert -----
            text shouldBe "Streak: 00 days"
        }

        @Test
        fun `stand in keeps a fixed suffix`() {
            // ----- Arrange -----
            val model = plainModel(
                value = 1.0,
                suffix = StatNumberSuffix.Fixed("%"),
            )

            // ----- Act -----
            val text = model.standIn()

            // ----- Assert -----
            text shouldBe "00%"
        }
    }
}
