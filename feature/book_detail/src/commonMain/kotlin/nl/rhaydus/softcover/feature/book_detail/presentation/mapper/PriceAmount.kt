package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import nl.rhaydus.softcover.core.domain.model.Currency

internal sealed interface PriceAmount {
    data object Empty : PriceAmount

    data class Valid(val thousandths: Long) : PriceAmount

    data object Invalid : PriceAmount
}

internal fun parsePriceAmount(
    text: String,
    fractionDigits: Int,
): PriceAmount {
    val trimmed = text.trim()

    if (trimmed.isEmpty()) return PriceAmount.Empty

    val marks = trimmed.count { it == '.' || it == ',' }

    val parts = trimmed.split(
        '.',
        ',',
    )

    val whole = parts.first()
    val fraction = parts.getOrElse(1) { "" }

    val valid = marks <= 1 &&
        whole.all { it in '0'..'9' } &&
        fraction.all { it in '0'..'9' } &&
        (whole.isNotEmpty() || fraction.isNotEmpty()) &&
        whole.length <= MAX_WHOLE_DIGITS &&
        fraction.length <= fractionDigits

    if (valid.not()) return PriceAmount.Invalid

    val wholeThousandths = (whole.ifEmpty { "0" }.toLong()) * THOUSAND

    val fractionThousandths = fraction.padEnd(
        MAX_FRACTION_DIGITS,
        '0',
    ).toLong()

    return PriceAmount.Valid(thousandths = wholeThousandths + fractionThousandths)
}

internal fun priceAmountError(
    text: String,
    fractionDigits: Int,
): String? = if (parsePriceAmount(
        text = text,
        fractionDigits = fractionDigits,
    ) == PriceAmount.Invalid) {
    val example = if (fractionDigits <= 0) "1450" else "14." + "5000".take(fractionDigits)

    "Enter a price like $example."
} else {
    null
}

internal fun formatPriceAmount(
    thousandths: Long,
    fractionDigits: Int,
): String {
    val digits = fractionDigits.coerceIn(
        0,
        MAX_FRACTION_DIGITS,
    )

    val unit = POWERS_OF_TEN[MAX_FRACTION_DIGITS - digits]
    val scaled = (thousandths + unit / 2) / unit
    val divisor = POWERS_OF_TEN[digits]

    val fraction = if (digits == 0) "" else "." + (scaled % divisor).toString().padStart(
        digits,
        '0',
    )

    return "${scaled / divisor}$fraction"
}

internal fun List<Currency>.fractionDigitsOf(code: String): Int =
    firstOrNull { it.code == code }?.fractionDigits ?: DEFAULT_FRACTION_DIGITS

private const val MAX_WHOLE_DIGITS = 12
private const val MAX_FRACTION_DIGITS = 3
private const val DEFAULT_FRACTION_DIGITS = 2
private const val THOUSAND = 1000L
private val POWERS_OF_TEN = longArrayOf(1L, 10L, 100L, 1000L)
