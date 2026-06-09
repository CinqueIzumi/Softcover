package nl.rhaydus.softcover.core.designsystem.presentation.util

import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle

private val groupedNumberFormatter = NSNumberFormatter().apply {
    numberStyle = NSNumberFormatterDecimalStyle
}

actual fun formatGroupedNumber(value: Int): String =
    groupedNumberFormatter.stringFromNumber(NSNumber(int = value)) ?: value.toString()
