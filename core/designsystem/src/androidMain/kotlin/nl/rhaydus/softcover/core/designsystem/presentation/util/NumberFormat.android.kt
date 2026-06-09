package nl.rhaydus.softcover.core.designsystem.presentation.util

import java.text.NumberFormat

actual fun formatGroupedNumber(value: Int): String = NumberFormat.getIntegerInstance().format(value.toLong())
