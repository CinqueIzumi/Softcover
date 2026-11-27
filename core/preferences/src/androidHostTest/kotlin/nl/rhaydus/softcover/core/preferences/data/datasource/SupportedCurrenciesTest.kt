package nl.rhaydus.softcover.core.preferences.data.datasource

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.Currency
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SupportedCurrenciesTest {
    private fun currency(
        code: String,
        digits: Int,
    ) = Currency(
        code = code,
        name = code,
        symbol = code,
        fractionDigits = digits,
    )

    @Nested
    inner class SupportedOnly {
        @Test
        fun `zero to three fraction digits are kept`() {
            // ----- Arrange -----
            val currencies = listOf(
                currency(
                    "JPY",
                    0,
                ),
                currency(
                    "EUR",
                    2,
                ),
                currency(
                    "BHD",
                    3,
                ),
            )

            // ----- Act -----
            val result = currencies.supportedOnly()

            // ----- Assert -----
            result shouldBe currencies
        }

        @Test
        fun `four fraction digits are dropped`() {
            // ----- Arrange -----
            val currencies = listOf(
                currency(
                    "CLF",
                    4,
                ),
                currency(
                    "EUR",
                    2,
                ),
            )

            // ----- Act -----
            val result = currencies.supportedOnly()

            // ----- Assert -----
            result.map { it.code } shouldBe listOf("EUR")
        }

        @Test
        fun `minus one fraction digits are dropped`() {
            // ----- Arrange -----
            val currencies = listOf(
                currency(
                    "ZZZ",
                    -1,
                ),
                currency(
                    "EUR",
                    2,
                ),
            )

            // ----- Act -----
            val result = currencies.supportedOnly()

            // ----- Assert -----
            result.map { it.code } shouldBe listOf("EUR")
        }

        @Test
        fun `X codes are dropped except the four real ones`() {
            // ----- Arrange -----
            val currencies = listOf(
                currency(
                    "XAF",
                    0,
                ),
                currency(
                    "XCD",
                    2,
                ),
                currency(
                    "XOF",
                    0,
                ),
                currency(
                    "XPF",
                    0,
                ),
                currency(
                    "XAU",
                    2,
                ),
                currency(
                    "XXX",
                    2,
                ),
            )

            // ----- Act -----
            val result = currencies.supportedOnly()

            // ----- Assert -----
            result.map { it.code } shouldBe listOf("XAF", "XCD", "XOF", "XPF")
        }

        @Test
        fun `an empty list stays empty`() {
            // ----- Arrange -----
            val currencies = emptyList<Currency>()

            // ----- Act -----
            val result = currencies.supportedOnly()

            // ----- Assert -----
            result shouldBe emptyList()
        }
    }
}
