package nl.rhaydus.softcover.core.preferences.data.datasource

import nl.rhaydus.softcover.core.domain.model.Currency
import platform.Foundation.NSLocale
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.Foundation.commonISOCurrencyCodes
import platform.Foundation.currencyCode
import platform.Foundation.currentLocale
import platform.Foundation.localeWithLocaleIdentifier

internal class IosCurrencyCatalog : CurrencyCatalog {
    private val displayLocale = NSLocale.localeWithLocaleIdentifier("en_GB")

    override fun all(): List<Currency> = NSLocale.commonISOCurrencyCodes
        .filterIsInstance<String>()
        .map { code ->
            val formatter = NSNumberFormatter().apply {
                numberStyle = NSNumberFormatterCurrencyStyle
                locale = displayLocale
                currencyCode = code
            }

            Currency(
                code = code,
                name = displayLocale.displayNameForKey(
                    key = "currency",
                    value = code,
                ) ?: code,
                symbol = formatter.currencySymbol ?: code,
                fractionDigits = formatter.maximumFractionDigits.toInt(),
            )
        }
        .supportedOnly()

    override fun deviceDefaultCode(): String? = NSLocale.currentLocale.currencyCode
}
