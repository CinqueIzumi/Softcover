package nl.rhaydus.softcover.core.designsystem.presentation.util

/**
 * Formats [value] with the platform's locale-aware grouping separators (e.g. `1,234` in the US,
 * `1.234` in much of Europe).
 *
 * Android delegates to `java.text.NumberFormat`; iOS to `NSNumberFormatter` with the decimal style.
 */
expect fun formatGroupedNumber(value: Int): String

/**
 * Formats [value] with the platform's locale-aware decimal separator and a fixed number of
 * [fractionDigits] (e.g. `4.2` in the US, `4,2` in much of Europe).
 *
 * Android delegates to `java.text.NumberFormat`; iOS to `NSNumberFormatter` with the decimal style.
 */
expect fun formatDecimalNumber(
    value: Double,
    fractionDigits: Int,
): String
