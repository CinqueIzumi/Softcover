package nl.rhaydus.softcover.core.preferences.data.datasource

import nl.rhaydus.softcover.core.domain.model.Currency
import java.util.Locale
import java.util.Currency as JavaCurrency

internal class JavaCurrencyCatalog : CurrencyCatalog {
    override fun all(): List<Currency> = JavaCurrency.getAvailableCurrencies()
        .map { currency ->
            Currency(
                code = currency.currencyCode,
                name = currency.getDisplayName(Locale.UK),
                symbol = currency.getSymbol(Locale.UK),
                fractionDigits = currency.defaultFractionDigits,
            )
        }
        .supportedOnly()

    override fun deviceDefaultCode(): String? = try {
        JavaCurrency.getInstance(Locale.getDefault()).currencyCode
    } catch (_: IllegalArgumentException) {
        null
    }
}
