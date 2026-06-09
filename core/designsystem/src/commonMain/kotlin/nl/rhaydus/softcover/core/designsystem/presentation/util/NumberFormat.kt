package nl.rhaydus.softcover.core.designsystem.presentation.util

/**
 * Formats [value] with the platform's locale-aware grouping separators (e.g. `1,234` in the US,
 * `1.234` in much of Europe).
 *
 * Android delegates to `java.text.NumberFormat`; iOS to `NSNumberFormatter` with the decimal style.
 */
expect fun formatGroupedNumber(value: Int): String
