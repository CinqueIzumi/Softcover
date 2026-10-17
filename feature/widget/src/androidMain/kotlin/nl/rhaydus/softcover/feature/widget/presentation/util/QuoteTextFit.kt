package nl.rhaydus.softcover.feature.widget.presentation.util

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.TypedValue

internal const val QUOTE_MAX_FONT_SIZE_SP = 18
internal const val QUOTE_MIN_FONT_SIZE_SP = 12

/** Lays the quote out at [fontSizeSp] inside [widthPx]. */
internal typealias QuoteTextMeasurer = (fontSizeSp: Int, widthPx: Int) -> QuoteTextMeasure

internal class QuoteTextFit(
    val fontSizeSp: Int,
    val maxLines: Int,
)

/**
 * The largest font size from [QUOTE_MAX_FONT_SIZE_SP] down to [QUOTE_MIN_FONT_SIZE_SP] at which the whole
 * quote fits [availableHeightPx]. When even the smallest size overflows, that size is returned with as many
 * lines as fit, so the text ends in an ellipsis.
 */
internal fun fitQuoteText(
    availableWidthPx: Int,
    availableHeightPx: Int,
    measure: QuoteTextMeasurer,
): QuoteTextFit {
    var smallest = QuoteTextFit(
        fontSizeSp = QUOTE_MIN_FONT_SIZE_SP,
        maxLines = 1,
    )

    for (fontSizeSp in QUOTE_MAX_FONT_SIZE_SP downTo QUOTE_MIN_FONT_SIZE_SP) {
        val measured = measure(
            fontSizeSp,
            availableWidthPx,
        )

        val fittingLines = (availableHeightPx / measured.lineHeightPx.coerceAtLeast(1)).coerceAtLeast(1)

        smallest = QuoteTextFit(
            fontSizeSp = fontSizeSp,
            maxLines = fittingLines,
        )

        if (measured.lineCount <= fittingLines) return smallest
    }

    return smallest
}

/** A [QuoteTextMeasurer] that matches the italic serif `TextView` Glance renders the quote into. */
internal fun androidQuoteTextMeasurer(
    context: Context,
    text: String,
): QuoteTextMeasurer {
    val metrics = context.resources.displayMetrics
    val typeface = Typeface.create(
        Typeface.SERIF,
        Typeface.ITALIC,
    )

    return { fontSizeSp, widthPx ->
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            textSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                fontSizeSp.toFloat(),
                metrics,
            )
        }

        val layout = StaticLayout.Builder
            .obtain(
                text,
                0,
                text.length,
                paint,
                widthPx.coerceAtLeast(1),
            )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setIncludePad(true)
            .build()

        QuoteTextMeasure(
            lineCount = layout.lineCount,
            lineHeightPx = layout.getLineBottom(0) - layout.getLineTop(0),
        )
    }
}

/** The height in px of one line of the default-typeface text at [fontSizeSp], as the byline renders. */
internal fun singleLineHeightPx(
    context: Context,
    fontSizeSp: Int,
): Int {
    val paint = TextPaint().apply {
        textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            fontSizeSp.toFloat(),
            context.resources.displayMetrics,
        )
    }

    val metrics = paint.fontMetricsInt

    return metrics.bottom - metrics.top
}
