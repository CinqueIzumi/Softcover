package nl.rhaydus.softcover.core.uibinding.money

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.Currency
import nl.rhaydus.softcover.core.domain.model.PricePaid
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PriceLabelTest {
    private fun currency(
        symbol: String,
        fractionDigits: Int,
    ) = Currency(
        code = "XXX",
        name = "Test",
        symbol = symbol,
        fractionDigits = fractionDigits,
    )

    private fun paid(thousandths: Long) = PricePaid(
        amountThousandths = thousandths,
        currencyCode = "XXX",
    )

    @Nested
    inner class ToLabel {
        @Test
        fun `a two digit currency shows cents`() {
            // ----- Arrange -----
            val price = paid(14_500)

            // ----- Act -----
            val label = price.toLabel(
                currency(
                    "€",
                    2,
                ),
            )

            // ----- Assert -----
            label shouldBe "€14.50"
        }

        @Test
        fun `a zero digit currency shows whole units only`() {
            // ----- Arrange -----
            val price = paid(1_450_000)

            // ----- Act -----
            val label = price.toLabel(
                currency(
                    "¥",
                    0,
                ),
            )

            // ----- Assert -----
            label shouldBe "¥1450"
        }

        @Test
        fun `a three digit currency shows all thousandths`() {
            // ----- Arrange -----
            val price = paid(14_500)

            // ----- Act -----
            val label = price.toLabel(
                currency(
                    "KD",
                    3,
                ),
            )

            // ----- Assert -----
            label shouldBe "KD14.500"
        }

        @Test
        fun `a fraction below ten is zero padded`() {
            // ----- Arrange -----
            val price = paid(5_050)

            // ----- Act -----
            val label = price.toLabel(
                currency(
                    "€",
                    2,
                ),
            )

            // ----- Assert -----
            label shouldBe "€5.05"
        }

        @Test
        fun `the amount rounds half up to the currency's digits`() {
            // ----- Arrange -----
            val up = paid(14_995)
            val down = paid(14_994)

            // ----- Act -----
            val roundedUp = up.toLabel(
                currency(
                    "€",
                    2,
                ),
            )

            val roundedDown = down.toLabel(
                currency(
                    "€",
                    2,
                ),
            )

            // ----- Assert -----
            roundedUp shouldBe "€15.00"
            roundedDown shouldBe "€14.99"
        }

        @Test
        fun `zero reads as zero`() {
            // ----- Arrange -----
            val price = paid(0)

            // ----- Act -----
            val label = price.toLabel(
                currency(
                    "€",
                    2,
                ),
            )

            // ----- Assert -----
            label shouldBe "€0.00"
        }

        @Test
        fun `a negative amount keeps its sign before the symbol`() {
            // ----- Arrange -----
            val price = paid(-14_500)

            // ----- Act -----
            val label = price.toLabel(
                currency(
                    "€",
                    2,
                ),
            )

            // ----- Assert -----
            label shouldBe "-€14.50"
        }
    }

    @Nested
    inner class FormatWholeUnits {
        @Test
        fun `rounds half up to whole units`() {
            // ----- Arrange -----
            val half = 14_500L
            val below = 14_499L

            // ----- Act -----
            val up = formatWholeUnits(
                half,
                currency(
                    "€",
                    2,
                ),
            )

            val down = formatWholeUnits(
                below,
                currency(
                    "€",
                    2,
                ),
            )

            // ----- Assert -----
            up shouldBe "€15"
            down shouldBe "€14"
        }

        @Test
        fun `zero reads as zero`() {
            // ----- Arrange -----
            val amount = 0L

            // ----- Act -----
            val label = formatWholeUnits(
                amount,
                currency(
                    "€",
                    2,
                ),
            )

            // ----- Assert -----
            label shouldBe "€0"
        }
    }
}
