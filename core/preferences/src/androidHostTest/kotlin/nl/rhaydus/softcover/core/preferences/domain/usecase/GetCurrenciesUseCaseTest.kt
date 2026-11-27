package nl.rhaydus.softcover.core.preferences.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.domain.model.Currency
import nl.rhaydus.softcover.core.preferences.data.datasource.CurrencyCatalog
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GetCurrenciesUseCaseTest {
    @Nested
    inner class Invoke {
        @Test
        fun `currencies come back sorted by name`() {
            // ----- Arrange -----
            val zloty = Currency(
                code = "PLN",
                name = "Polish Zloty",
                symbol = "PLN",
                fractionDigits = 2,
            )

            val euro = Currency(
                code = "EUR",
                name = "Euro",
                symbol = "€",
                fractionDigits = 2,
            )

            val yen = Currency(
                code = "JPY",
                name = "Japanese Yen",
                symbol = "JP¥",
                fractionDigits = 0,
            )

            val catalog = mockk<CurrencyCatalog>()

            every {
                catalog.all()
            } returns listOf(zloty, yen, euro)

            // ----- Act -----
            val result = GetCurrenciesUseCase(currencyCatalog = catalog)()

            // ----- Assert -----
            result shouldBe listOf(euro, yen, zloty)
        }
    }
}
