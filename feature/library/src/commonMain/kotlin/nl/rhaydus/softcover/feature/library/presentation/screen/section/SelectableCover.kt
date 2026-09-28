package nl.rhaydus.softcover.feature.library.presentation.screen.section

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.selection.SelectionMark
import nl.rhaydus.softcover.core.component.selection.SelectionMarkUiModel

/**
 * The cover radius the selection-ring overlay ([SelectableCover]) hard-codes so its clip shape
 * matches the cover underneath. `Cover` itself now sources its corner radius from
 * `CoverDimensions.forVariant(CoverVariant.LibraryShelfItem)` rather than a parameter here — this
 * constant is **not** wired to that table, so if `LibraryShelfItem`'s radius in `CoverDimensions`
 * ever changes, this must be updated to match or the overlay will silently drift from the cover's
 * clip.
 */
private val LIBRARY_COVER_CORNER_RADIUS = 10.dp

@Composable
internal fun SelectableCover(
    selectionMark: SelectionMarkUiModel?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val coverShape = RoundedCornerShape(LIBRARY_COVER_CORNER_RADIUS)
    val isSelected = selectionMark?.selected == true

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    alpha = if (selectionMark != null && isSelected.not()) UNSELECTED_COVER_ALPHA else 1f
                },
        ) {
            content()
        }

        if (isSelected) {
            // The redesign's selected-cover treatment: an inset primary ring + a 16% primary wash,
            // on top of the (undimmed, per the alpha branch above) cover.
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                        shape = coverShape,
                    )
                    .border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = coverShape,
                    ),
            )
        }

        if (selectionMark != null) {
            SelectionMark(
                model = selectionMark,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            )
        }
    }
}

private const val UNSELECTED_COVER_ALPHA = 0.55f
