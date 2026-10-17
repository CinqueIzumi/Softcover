package nl.rhaydus.softcover.feature.widget.presentation.util

/** The result of laying a quote out at one font size: how many lines it wraps to and how tall each line is. */
internal class QuoteTextMeasure(
    val lineCount: Int,
    val lineHeightPx: Int,
)
