package nl.rhaydus.softcover.core.designsystem.presentation.theme

import androidx.compose.ui.text.TextStyle

/** Gives digits equal widths, for numbers that change in place or line up in columns. */
fun TextStyle.tabularFigures(): TextStyle = copy(fontFeatureSettings = "tnum")
