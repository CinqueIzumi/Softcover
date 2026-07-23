package nl.rhaydus.softcover.feature.library.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * The rail's own height, reserved whether or not both neighbours exist. 48dp rather than the ~36dp
 * the labels need: each half of the row *is* the tap target, and a navigation control has to clear
 * the minimum touch target.
 */
private val RAIL_HEIGHT = 48.dp

/**
 * Names the shelves either side of the current one, so a swipe is never blind: with the pill tab row
 * retired, nothing else on the Library screen says what the next shelf is. Hairline-bounded, one
 * eyebrow-caps label per end, each end its own tap target that moves one shelf over.
 *
 * The row keeps [RAIL_HEIGHT] regardless of how many neighbours exist, and a missing neighbour
 * leaves its end empty rather than letting the other label drift towards the middle — so reaching
 * the first or last shelf never shifts the grid below.
 *
 * Mobile only. Desktop switches shelves from a permanent sidebar, so it has neither a pager nor
 * this rail.
 */
@Composable
internal fun ShelfNeighbourRail(
    previousLabel: String?,
    nextLabel: String?,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(RAIL_HEIGHT)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NeighbourEnd(
                label = previousLabel,
                icon = SoftcoverIcon.KeyboardArrowLeft,
                iconLeading = true,
                alignment = Alignment.CenterStart,
                onClick = onPreviousClick,
                modifier = Modifier.weight(1f),
            )

            Spacer(modifier = Modifier.width(12.dp))

            NeighbourEnd(
                label = nextLabel,
                icon = SoftcoverIcon.KeyboardArrowRight,
                iconLeading = false,
                alignment = Alignment.CenterEnd,
                onClick = onNextClick,
                modifier = Modifier.weight(1f),
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

/**
 * One end of the rail. A null [label] renders as an empty weighted slot, which is what keeps the
 * opposite end anchored to its own edge on the first and last shelf.
 */
@Composable
private fun NeighbourEnd(
    label: String?,
    icon: SoftcoverIcon,
    iconLeading: Boolean,
    alignment: Alignment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (label == null) {
        Spacer(modifier = modifier)

        return
    }

    val chevron = drawableIconResource(
        icon = icon,
        contentDescription = "Go to $label",
    )

    // The whole half is the tap target — a label plus a 16dp chevron is too small a thing to aim at,
    // and the empty half of a rail with one neighbour never reaches here (it renders as the Spacer
    // above), so no inert region is ever clickable. The Box keeps the content anchored to its edge.
    Box(
        modifier = modifier
            .fillMaxHeight()
            .pointerHandCursor()
            .pressScaleClickable(onClick = onClick),
        contentAlignment = alignment,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (iconLeading) {
                Icon(
                    painter = chevron.getIconPainter(),
                    contentDescription = chevron.contentDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )

                Spacer(modifier = Modifier.width(6.dp))
            }

            Text(
                text = label.uppercase(),
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(
                    weight = 1f,
                    fill = false,
                ),
            )

            if (iconLeading.not()) {
                Spacer(modifier = Modifier.width(6.dp))

                Icon(
                    painter = chevron.getIconPainter(),
                    contentDescription = chevron.contentDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
