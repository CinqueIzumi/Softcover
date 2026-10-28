package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Constraints
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing

@Composable
internal fun BookCardTagStripRow(
    strip: BookCardTagStrip,
    modifier: Modifier = Modifier,
) {
    Layout(
        content = {
            strip.chips.forEach { chip -> Chip(model = chip) }
            strip.overflowChips.forEach { chip -> Chip(model = chip) }
        },
        modifier = modifier.clearAndSetSemantics { contentDescription = strip.contentDescription },
    ) { measurables, constraints ->
        val loose = Constraints(maxWidth = constraints.maxWidth)
        val placeables = measurables.map { it.measure(loose) }
        val chips = placeables.take(strip.chips.size)
        val overflows = placeables.drop(strip.chips.size)
        val gap = Spacing.xxs.roundToPx()

        val shown = fittingChipCount(
            chipWidths = chips.map(Placeable::width),
            overflowWidths = overflows.map(Placeable::width),
            gap = gap,
            available = constraints.maxWidth,
        )
        val overflow = overflows.getOrNull(chips.size - shown - 1)?.takeIf { shown < chips.size }
        val line = chips.take(shown) + listOfNotNull(overflow)
        val height = line.maxOfOrNull(Placeable::height) ?: 0
        val width = line.sumOf(Placeable::width) + gap * (line.size - 1).coerceAtLeast(0)

        layout(width, height) {
            var x = 0

            line.forEach { placeable ->
                placeable.placeRelative(
                    x = x,
                    y = (height - placeable.height) / 2,
                )
                x += placeable.width + gap
            }
        }
    }
}
