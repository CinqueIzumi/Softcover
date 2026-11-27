package nl.rhaydus.softcover.core.uibinding.money

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.Currency
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CurrencyRowsTest {
    private fun currency(
        code: String,
        name: String,
    ) = Currency(
        code = code,
        name = name,
        symbol = "$",
        fractionDigits = 2,
    )

    private val currencies = listOf(
        currency(
            "USD",
            "US dollar",
        ),
        currency(
            "EUR",
            "Euro",
        ),
        currency(
            "GBP",
            "Pound sterling",
        ),
        currency(
            "AUD",
            "Australian dollar",
        ),
    )

    @Nested
    inner class Ordering {
        @Test
        fun `lists the selected currency first, then the usual one, then the rest by name`() {
            // ----- Arrange -----
            val selected = "USD"
            val usual = "GBP"

            // ----- Act -----
            val keys = currencies.toCurrencyRows(
                selected,
                usual,
                "",
            ).map { it.key }

            // ----- Assert -----
            keys shouldBe listOf("USD", "GBP", "AUD", "EUR")
        }

        @Test
        fun `a usual currency equal to the selected one is listed once`() {
            // ----- Arrange -----
            val selected = "EUR"

            // ----- Act -----
            val keys = currencies.toCurrencyRows(
                selected,
                "EUR",
                "",
            ).map { it.key }

            // ----- Assert -----
            keys shouldBe listOf("EUR", "AUD", "GBP", "USD")
        }

        @Test
        fun `a usual currency missing from the list is ignored`() {
            // ----- Arrange -----
            val usual = "JPY"

            // ----- Act -----
            val keys = currencies.toCurrencyRows(
                "EUR",
                usual,
                "",
            ).map { it.key }

            // ----- Assert -----
            keys shouldBe listOf("EUR", "AUD", "GBP", "USD")
        }

        @Test
        fun `a null usual currency leaves the rest sorted by name`() {
            // ----- Arrange -----
            val usual: String? = null

            // ----- Act -----
            val keys = currencies.toCurrencyRows(
                "ZZZ",
                usual,
                "",
            ).map { it.key }

            // ----- Assert -----
            keys shouldBe listOf("AUD", "EUR", "GBP", "USD")
        }
    }

    @Nested
    inner class Query {
        @Test
        fun `matches the name case-insensitively and ignores surrounding whitespace`() {
            // ----- Arrange -----
            val query = "  DOLLAR "

            // ----- Act -----
            val keys = currencies.toCurrencyRows(
                "USD",
                null,
                query,
            ).map { it.key }

            // ----- Assert -----
            keys shouldBe listOf("USD", "AUD")
        }

        @Test
        fun `matches the code`() {
            // ----- Arrange -----
            val query = "gb"

            // ----- Act -----
            val keys = currencies.toCurrencyRows(
                "USD",
                null,
                query,
            ).map { it.key }

            // ----- Assert -----
            keys shouldBe listOf("GBP")
        }

        @Test
        fun `a whitespace-only query keeps every currency`() {
            // ----- Arrange -----
            val query = "   "

            // ----- Act -----
            val rows = currencies.toCurrencyRows(
                "USD",
                null,
                query,
            )

            // ----- Assert -----
            rows.size shouldBe 4
        }

        @Test
        fun `a query matching nothing yields no rows`() {
            // ----- Arrange -----
            val query = "krona"

            // ----- Act -----
            val rows = currencies.toCurrencyRows(
                "USD",
                null,
                query,
            )

            // ----- Assert -----
            rows.size shouldBe 0
        }
    }

    @Nested
    inner class RowContent {
        @Test
        fun `marks only the selected row and carries the code as leading text`() {
            // ----- Arrange -----
            val selected = "EUR"

            // ----- Act -----
            val rows = currencies.toCurrencyRows(
                selected,
                null,
                "",
            )

            // ----- Assert -----
            rows.map { it.selected } shouldBe listOf(true, false, false, false)
            rows.map { it.leadingText } shouldBe listOf("EUR", "AUD", "GBP", "USD")
            rows.map { it.label } shouldBe listOf("Euro", "Australian dollar", "Pound sterling", "US dollar")
        }
    }
}
