package nl.rhaydus.softcover.core.preferences.data.datasource

import nl.rhaydus.softcover.core.domain.model.Currency

private const val MAX_STORABLE_FRACTION_DIGITS = 3
private val REAL_X_CODES = setOf("XAF", "XCD", "XOF", "XPF")

internal fun List<Currency>.supportedOnly(): List<Currency> = filter { currency ->
    currency.fractionDigits in 0..MAX_STORABLE_FRACTION_DIGITS &&
        (currency.code.startsWith("X").not() || currency.code in REAL_X_CODES)
}
