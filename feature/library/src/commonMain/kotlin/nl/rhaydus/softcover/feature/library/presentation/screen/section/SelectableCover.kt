package nl.rhaydus.softcover.feature.library.presentation.screen.section

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource

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
    isSelectionMode: Boolean,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val coverShape = RoundedCornerShape(LIBRARY_COVER_CORNER_RADIUS)

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    alpha = if (isSelectionMode && isSelected.not()) UNSELECTED_COVER_ALPHA else 1f
                },
        ) {
            content()
        }

        if (isSelectionMode && isSelected) {
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

        if (isSelectionMode) {
            SelectionCircleIndicator(
                isSelected = isSelected,
                unselectedContainer = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                size = 26.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            )
        }
    }
}

@Composable
internal fun SelectionLeadingIcon(isSelected: Boolean) {
    SelectionCircleIndicator(
        isSelected = isSelected,
        unselectedContainer = MaterialTheme.colorScheme.surfaceContainerHigh,
    )
}

@Composable
private fun SelectionCircleIndicator(
    isSelected: Boolean,
    unselectedContainer: Color,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
) {
    val container = if (isSelected) MaterialTheme.colorScheme.primary else unselectedContainer

    val content = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = container,
        contentColor = content,
        shape = RoundedCornerShape(percent = 50),
        modifier = modifier.size(size),
    ) {
        if (isSelected) {
            val checkIcon = drawableIconResource(
                icon = SoftcoverIcon.Check,
                contentDescription = "",
            )

            Icon(
                painter = checkIcon.getIconPainter(),
                contentDescription = checkIcon.contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
            )
        }
    }
}

private const val UNSELECTED_COVER_ALPHA = 0.55f
