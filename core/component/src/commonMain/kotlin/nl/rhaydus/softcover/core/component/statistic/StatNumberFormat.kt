package nl.rhaydus.softcover.core.component.statistic

/**
 * How a [StatNumberUiModel]'s value reads once rendered.
 *
 * A descriptor rather than the `formatter: (Int) -> String` lambda this replaced: a lambda field
 * allocates fresh on every recomposition and so breaks the equality check that lets Compose skip the
 * component it was built for (R3). Three entries, because three is what the app actually asks for.
 */
sealed interface StatNumberFormat {
    /** Thousands grouped in the reader's locale — "12,481". The shape most page counts take. */
    data object Grouped : StatNumberFormat

    /** The bare integer — "30". For counts small enough that grouping would be noise. */
    data object Plain : StatNumberFormat

    /** A fixed number of decimals — "4.2" at one digit. For averages. */
    data class Decimal(val fractionDigits: Int) : StatNumberFormat
}
