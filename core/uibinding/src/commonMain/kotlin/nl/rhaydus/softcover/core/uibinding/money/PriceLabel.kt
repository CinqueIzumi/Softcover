package nl.rhaydus.softcover.core.uibinding.money

import nl.rhaydus.softcover.core.domain.model.Currency
import nl.rhaydus.softcover.core.domain.model.PricePaid
import kotlin.math.abs

fun PricePaid.toLabel(currency: Currency): String {
    val digits = currency.fractionDigits.coerceIn(
        0,
        MAX_FRACTION_DIGITS,
    )

    val unit = POWERS_OF_TEN[MAX_FRACTION_DIGITS - digits]
    val sign = if (amountThousandths < 0) "-" else ""
    val scaled = (abs(amountThousandths) + unit / 2) / unit
    val divisor = POWERS_OF_TEN[digits]
    val whole = scaled / divisor

    val fraction = if (digits == 0) "" else "." + (scaled % divisor).toString().padStart(
        digits,
        '0',
    )

    return "$sign${currency.symbol}$whole$fraction"
}

fun formatWholeUnits(
    amountThousandths: Long,
    currency: Currency,
): String {
    val sign = if (amountThousandths < 0) "-" else ""
    val whole = (abs(amountThousandths) + THOUSAND / 2) / THOUSAND

    return "$sign${currency.symbol}$whole"
}

private const val MAX_FRACTION_DIGITS = 3
private const val THOUSAND = 1000L
private val POWERS_OF_TEN = longArrayOf(1L, 10L, 100L, 1000L)
