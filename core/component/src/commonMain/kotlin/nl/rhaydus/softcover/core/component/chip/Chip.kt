package nl.rhaydus.softcover.core.component.chip

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.theme.spoilerCover

/**
 * The canonical pill-shaped chip (§3.4). Fully-rounded surface with a single label. Selected swaps
 * to [secondaryContainer][androidx.compose.material3.ColorScheme.secondaryContainer]; idle sits on
 * [surfaceContainerHigh][androidx.compose.material3.ColorScheme.surfaceContainerHigh].
 *
 * [ChipUiModel.clickable] `= false` renders a read-only, inert chip (book-detail tags): the surface
 * then carries no ripple and no click role. A tap on an interactive chip (library facets, toggles)
 * reports [ChipEvent.Clicked] with [ChipUiModel.key].
 *
 * [ChipUiModel.concealed] renders the chip as a spoiler redaction (§2.1 spoiler register): the label
 * is drawn transparent so it reserves its width but cannot be read, beneath a solid
 * [spoilerCover][nl.rhaydus.softcover.core.designsystem.presentation.theme.spoilerCover] fill — the same
 * treatment `RichText` gives an inline spoiler run. Because the label still measures at
 * its real width, revealing it (re-render with `concealed = false`) does not reflow the row.
 */
@Composable
fun Chip(
    model: ChipUiModel,
    onEvent: (ChipEvent) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val container = when {
        model.concealed -> MaterialTheme.colorScheme.spoilerCover
        model.selected -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val content = when {
        model.concealed -> Color.Transparent
        model.selected -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    val shape = RoundedCornerShape(percent = 50)

    if (model.clickable) {
        Surface(
            modifier = modifier,
            color = container,
            contentColor = content,
            shape = shape,
            onClick = { onEvent(ChipEvent.Clicked(key = model.key)) },
        ) {
            PillChipLabel(
                label = model.label,
                selected = model.selected,
            )
        }
    } else {
        Surface(
            modifier = modifier,
            color = container,
            contentColor = content,
            shape = shape,
        ) {
            PillChipLabel(
                label = model.label,
                selected = model.selected,
            )
        }
    }
}

@Composable
private fun PillChipLabel(
    label: String,
    selected: Boolean,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
    )
}
