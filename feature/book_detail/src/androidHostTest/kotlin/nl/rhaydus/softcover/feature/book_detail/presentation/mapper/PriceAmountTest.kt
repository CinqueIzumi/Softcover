package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.feature.book_detail.presentation.DINAR
import nl.rhaydus.softcover.feature.book_detail.presentation.EURO
import nl.rhaydus.softcover.feature.book_detail.presentation.TEST_CURRENCIES
import nl.rhaydus.softcover.feature.book_detail.presentation.YEN
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PriceAmountTest {
    @Nested
    inner class ParsePriceAmount {
        @Test
        fun `reads blank text as no price`() {
            // ----- Arrange -----
            val text = " \t "

            // ----- Act -----
            val result = parsePriceAmount(
                text = text,
                fractionDigits = 2,
            )

            // ----- Assert -----
            result shouldBe PriceAmount.Empty
        }

        @Test
        fun `reads a dot as the decimal mark`() {
            // ----- Arrange -----
            val text = "14.50"

            // ----- Act -----
            val result = parsePriceAmount(
                text = text,
                fractionDigits = 2,
            )

            // ----- Assert -----
            result shouldBe PriceAmount.Valid(thousandths = 14_500)
        }

        @Test
        fun `reads a comma as the decimal mark`() {
            // ----- Arrange -----
            val text = "14,5"

            // ----- Act -----
            val result = parsePriceAmount(
                text = text,
                fractionDigits = 2,
            )

            // ----- Assert -----
            result shouldBe PriceAmount.Valid(thousandths = 14_500)
        }

        @Test
        fun `reads a whole amount and trims surrounding whitespace`() {
            // ----- Arrange -----
            val text = "  1450 "

            // ----- Act -----
            val result = parsePriceAmount(
                text = text,
                fractionDigits = 0,
            )

            // ----- Assert -----
            result shouldBe PriceAmount.Valid(thousandths = 1_450_000)
        }

        @Test
        fun `keeps three fraction digits for a three-digit currency`() {
            // ----- Arrange -----
            val text = "14.505"

            // ----- Act -----
            val result = parsePriceAmount(
                text = text,
                fractionDigits = 3,
            )

            // ----- Assert -----
            result shouldBe PriceAmount.Valid(thousandths = 14_505)
        }

        @Test
        fun `accepts a missing whole part or a trailing mark`() {
            // ----- Arrange -----
            val leading = ".5"
            val trailing = "7."

            // ----- Act -----
            val leadingResult = parsePriceAmount(
                text = leading,
                fractionDigits = 2,
            )

            val trailingResult = parsePriceAmount(
                text = trailing,
                fractionDigits = 2,
            )

            // ----- Assert -----
            leadingResult shouldBe PriceAmount.Valid(thousandths = 500)
            trailingResult shouldBe PriceAmount.Valid(thousandths = 7_000)
        }

        @Test
        fun `reads zero as a free price`() {
            // ----- Arrange -----
            val text = "0"

            // ----- Act -----
            val result = parsePriceAmount(
                text = text,
                fractionDigits = 2,
            )

            // ----- Assert -----
            result shouldBe PriceAmount.Valid(thousandths = 0)
        }

        @Test
        fun `rejects more fraction digits than the currency allows`() {
            // ----- Arrange -----
            val text = "14.505"

            // ----- Act -----
            val result = parsePriceAmount(
                text = text,
                fractionDigits = 2,
            )

            // ----- Assert -----
            result shouldBe PriceAmount.Invalid
        }

        @Test
        fun `rejects any fraction for a currency without minor units`() {
            // ----- Arrange -----
            val text = "1450.5"

            // ----- Act -----
            val result = parsePriceAmount(
                text = text,
                fractionDigits = 0,
            )

            // ----- Assert -----
            result shouldBe PriceAmount.Invalid
        }

        @Test
        fun `rejects two decimal marks`() {
            // ----- Arrange -----
            val text = "1.4.5"

            // ----- Act -----
            val result = parsePriceAmount(
                text = text,
                fractionDigits = 2,
            )

            // ----- Assert -----
            result shouldBe PriceAmount.Invalid
        }

        @Test
        fun `rejects thousands grouping`() {
            // ----- Arrange -----
            val dotThenComma = "1.234,50"
            val commaThenDot = "1,234.50"

            // ----- Act -----
            val first = parsePriceAmount(
                text = dotThenComma,
                fractionDigits = 2,
            )

            val second = parsePriceAmount(
                text = commaThenDot,
                fractionDigits = 2,
            )

            // ----- Assert -----
            first shouldBe PriceAmount.Invalid
            second shouldBe PriceAmount.Invalid
        }

        @Test
        fun `rejects a lone mark, letters, a sign and inner spaces`() {
            // ----- Arrange -----
            val inputs = listOf(".", ",", "abc", "-5", "+5", "1 000", "12e3", "1.5x")

            // ----- Act -----
            val results = inputs.map {
                parsePriceAmount(
                    text = it,
                    fractionDigits = 2,
                )
            }

            // ----- Assert -----
            results.forEach { it shouldBe PriceAmount.Invalid }
        }

        @Test
        fun `rejects a whole part too long to store`() {
            // ----- Arrange -----
            val text = "1".repeat(13)

            // ----- Act -----
            val result = parsePriceAmount(
                text = text,
                fractionDigits = 2,
            )

            // ----- Assert -----
            result shouldBe PriceAmount.Invalid
        }

        @Test
        fun `accepts the longest whole part`() {
            // ----- Arrange -----
            val text = "9".repeat(12)

            // ----- Act -----
            val result = parsePriceAmount(
                text = text,
                fractionDigits = 2,
            )

            // ----- Assert -----
            result shouldBe PriceAmount.Valid(thousandths = 999_999_999_999_000)
        }
    }

    @Nested
    inner class PriceAmountError {
        @Test
        fun `gives no error for blank or valid text`() {
            // ----- Arrange -----
            val inputs = listOf("", "  ", "14.50", "14,5")

            // ----- Act -----
            val results = inputs.map {
                priceAmountError(
                    text = it,
                    fractionDigits = 2,
                )
            }

            // ----- Assert -----
            results.forEach { it.shouldBeNull() }
        }

        @Test
        fun `shows a whole-number example for a currency without minor units`() {
            // ----- Arrange -----
            val text = "abc"

            // ----- Act -----
            val result = priceAmountError(
                text = text,
                fractionDigits = 0,
            )

            // ----- Assert -----
            result shouldBe "Enter a price like 1450."
        }

        @Test
        fun `shows a two-digit example for a two-digit currency`() {
            // ----- Arrange -----
            val text = "abc"

            // ----- Act -----
            val result = priceAmountError(
                text = text,
                fractionDigits = 2,
            )

            // ----- Assert -----
            result shouldBe "Enter a price like 14.50."
        }

        @Test
        fun `shows a three-digit example for a three-digit currency`() {
            // ----- Arrange -----
            val text = "abc"

            // ----- Act -----
            val result = priceAmountError(
                text = text,
                fractionDigits = 3,
            )

            // ----- Assert -----
            result shouldBe "Enter a price like 14.500."
        }
    }

    @Nested
    inner class FormatPriceAmount {
        @Test
        fun `pads the fraction to the currency's digits`() {
            // ----- Arrange -----
            val thousandths = 14_500L

            // ----- Act -----
            val result = formatPriceAmount(
                thousandths = thousandths,
                fractionDigits = 2,
            )

            // ----- Assert -----
            result shouldBe "14.50"
        }

        @Test
        fun `drops the mark for a currency without minor units`() {
            // ----- Arrange -----
            val thousandths = 1_450_000L

            // ----- Act -----
            val result = formatPriceAmount(
                thousandths = thousandths,
                fractionDigits = 0,
            )

            // ----- Assert -----
            result shouldBe "1450"
        }

        @Test
        fun `keeps three digits for a three-digit currency`() {
            // ----- Arrange -----
            val thousandths = 14_505L

            // ----- Act -----
            val result = formatPriceAmount(
                thousandths = thousandths,
                fractionDigits = 3,
            )

            // ----- Assert -----
            result shouldBe "14.505"
        }

        @Test
        fun `rounds half up when the stored amount has finer digits than the currency`() {
            // ----- Arrange -----
            val thousandths = 14_505L

            // ----- Act -----
            val result = formatPriceAmount(
                thousandths = thousandths,
                fractionDigits = 2,
            )

            // ----- Assert -----
            result shouldBe "14.51"
        }

        @Test
        fun `formats zero`() {
            // ----- Arrange -----
            val thousandths = 0L

            // ----- Act -----
            val result = formatPriceAmount(
                thousandths = thousandths,
                fractionDigits = 2,
            )

            // ----- Assert -----
            result shouldBe "0.00"
        }
    }

    @Nested
    inner class FractionDigitsOf {
        @Test
        fun `reads the digits of a known currency`() {
            // ----- Arrange -----
            val currencies = TEST_CURRENCIES

            // ----- Act -----
            val result = currencies.map { currencies.fractionDigitsOf(code = it.code) }

            // ----- Assert -----
            result shouldBe listOf(EURO.fractionDigits, YEN.fractionDigits, DINAR.fractionDigits)
        }

        @Test
        fun `falls back to two digits for an unknown code`() {
            // ----- Arrange -----
            val currencies = TEST_CURRENCIES

            // ----- Act -----
            val result = currencies.fractionDigitsOf(code = "XXX")

            // ----- Assert -----
            result shouldBe 2
        }
    }
}
