package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.theme.displayFontFamily

private const val DEFAULT_DROP_CAP_LINES = 3

/**
 * Renders body prose with a Fraunces drop-cap wrapping the first `dropCapLines` lines.
 * The first letter of [text] (skipping leading whitespace and punctuation) is rendered in
 * [dropCapColor] at a display-face size proportional to `bodyStyle.lineHeight`, with the
 * remaining text reflowing to its right for the first `dropCapLines` lines and resuming
 * at full width below.
 *
 * Falls back to a plain Text when the body is empty or contains no letters.
 */
@Composable
fun DropCapText(
    text: AnnotatedString,
    bodyStyle: TextStyle,
    bodyColor: Color,
    modifier: Modifier = Modifier,
    dropCapColor: Color = MaterialTheme.colorScheme.primary,
    dropCapLines: Int = DEFAULT_DROP_CAP_LINES,
    gap: Dp = 8.dp,
) {
    val firstLetterIndex = text.text.indexOfFirst { it.isLetter() }

    if (firstLetterIndex < 0) {
        Text(text = text, style = bodyStyle, color = bodyColor, modifier = modifier)
        return
    }

    val leading = text.subSequence(startIndex = 0, endIndex = firstLetterIndex)
    val firstLetter = text.text[firstLetterIndex].toString()
    val remainder = text.subSequence(startIndex = firstLetterIndex + 1, endIndex = text.length)

    val dropCapStyle = bodyStyle.copy(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = bodyStyle.lineHeight * dropCapLines * 0.92f,
        lineHeight = bodyStyle.lineHeight * dropCapLines,
        color = dropCapColor,
    )

    val measurer = rememberTextMeasurer()

    SubcomposeLayout(modifier = modifier) { constraints ->
        val totalWidth = constraints.maxWidth
        val gapPx = gap.roundToPx()

        val capMeasurement = measurer.measure(
            text = AnnotatedString(text = firstLetter),
            style = dropCapStyle,
        )

        val capWidth = capMeasurement.size.width
        val capHeight = capMeasurement.size.height
        val indentedWidth = (totalWidth - capWidth - gapPx).coerceAtLeast(minimumValue = 0)

        val combinedFirst = AnnotatedString.Builder(capacity = leading.length + remainder.length)
            .apply {
                append(text = leading)
                append(text = remainder)
            }
            .toAnnotatedString()

        val indentedMeasurement = measurer.measure(
            text = combinedFirst,
            style = bodyStyle.copy(color = bodyColor),
            constraints = Constraints(maxWidth = indentedWidth.coerceAtLeast(minimumValue = 1)),
        )

        val splitLineIndex = (dropCapLines - 1).coerceAtMost(maximumValue = indentedMeasurement.lineCount - 1)

        val splitOffset = if (splitLineIndex >= 0) {
            indentedMeasurement.getLineEnd(lineIndex = splitLineIndex, visibleEnd = false)
        } else {
            combinedFirst.length
        }

        val firstPart = combinedFirst.subSequence(
            startIndex = 0,
            endIndex = splitOffset.coerceAtMost(maximumValue = combinedFirst.length),
        )

        val secondPart = if (splitOffset < combinedFirst.length) {
            combinedFirst.subSequence(startIndex = splitOffset, endIndex = combinedFirst.length)
        } else {
            null
        }

        val capPlaceable = subcompose(slotId = "cap") {
            Text(text = firstLetter, style = dropCapStyle)
        }.first().measure(constraints = Constraints())

        val firstPlaceable = subcompose(slotId = "first") {
            Text(
                text = firstPart,
                style = bodyStyle,
                color = bodyColor,
            )
        }.first().measure(
            constraints = Constraints(
                minWidth = indentedWidth,
                maxWidth = indentedWidth,
            ),
        )

        val secondPlaceable = secondPart?.let {
            subcompose(slotId = "rest") {
                Text(
                    text = it,
                    style = bodyStyle,
                    color = bodyColor,
                )
            }.first().measure(
                constraints = Constraints(
                    minWidth = totalWidth,
                    maxWidth = totalWidth,
                ),
            )
        }

        val topRowHeight = maxOf(capHeight, firstPlaceable.height)
        val totalHeight = topRowHeight + (secondPlaceable?.height ?: 0)

        layout(width = totalWidth, height = totalHeight) {
            capPlaceable.placeRelative(x = 0, y = 0)
            firstPlaceable.placeRelative(x = capWidth + gapPx, y = 0)
            secondPlaceable?.placeRelative(x = 0, y = topRowHeight)
        }
    }
}
