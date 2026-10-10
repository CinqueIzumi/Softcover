package nl.rhaydus.softcover.core.component.statistic

/**
 * How a [StatNumberUiModel]'s value reads once rendered.
 */
sealed interface StatNumberFormat {
    /** Thousands grouped in the reader's locale — "12,481". The shape most page counts take. */
    data object Grouped : StatNumberFormat

    /** The bare integer — "30". For counts small enough that grouping would be noise. */
    data object Plain : StatNumberFormat

    /** One decimal — "4.2". For averages. */
    data object Decimal : StatNumberFormat

    /** [StatNumberUiModel.value] in seconds, shown as hours and minutes. */
    data object Duration : StatNumberFormat
}
