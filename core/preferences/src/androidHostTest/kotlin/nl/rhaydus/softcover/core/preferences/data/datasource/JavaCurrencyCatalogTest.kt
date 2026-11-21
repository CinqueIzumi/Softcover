package nl.rhaydus.softcover.core.preferences.data.datasource

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class JavaCurrencyCatalogTest {
    private val catalog = JavaCurrencyCatalog()

    @Nested
    inner class All {
        @Test
        fun `real currencies are listed with their fraction digits`() {
            // ----- Arrange -----
            val expected = mapOf(
                "EUR" to 2,
                "JPY" to 0,
                "BHD" to 3,
            )

            // ----- Act -----
            val result = catalog.all().associate { it.code to it.fractionDigits }

            // ----- Assert -----
            expected.forEach { (code, digits) -> result[code] shouldBe digits }
        }

        @Test
        fun `names are in British English`() {
            // ----- Arrange -----
            val catalogue = catalog

            // ----- Act -----
            val result = catalogue.all().first { it.code == "USD" }

            // ----- Assert -----
            result.name shouldBe "US Dollar"
        }
    }
}
