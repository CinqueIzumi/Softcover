package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.badge.CoverOverlay
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.cover.CoverDimensions
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.selection.SelectionMark
import nl.rhaydus.softcover.core.component.text.SoftcoverText

@Composable
internal fun BookCardCover(
    cover: CoverUiModel?,
    decorations: BookCardDecorations,
    modifier: Modifier = Modifier,
) {
    val selection = decorations.selection
    val selected = selection?.selected == true

    val shape = if (cover == null) {
        RectangleShape
    } else {
        RoundedCornerShape(CoverDimensions.forVariant(variant = cover.variant).cornerRadius)
    }

    CoverOverlay(
        model = decorations.coverBadge,
        modifier = modifier,
    ) {
        Box {
            Cover(
                model = cover,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = if (selection != null && selected.not()) 0.55f else 1f
                    },
            )

            if (selected) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                            shape = shape,
                        )
                        .border(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = shape,
                        ),
                )
            }

            if (selection != null) {
                SelectionMark(
                    model = selection,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                )
            }
        }
    }
}

@Composable
internal fun BookCardSeriesStack(
    front: CoverUiModel?,
    width: Dp,
) {
    val frontWidth = width - 12.dp
    val plateVariant = front?.variant ?: CoverVariant.HiddenSeriesStack
    val plateShape = RoundedCornerShape(CoverDimensions.forVariant(variant = plateVariant).cornerRadius)

    Box(modifier = Modifier.width(width)) {
        SeriesStackPlate(
            width = frontWidth,
            shape = plateShape,
            offset = 12.dp,
            alpha = 0.4f,
        )

        SeriesStackPlate(
            width = frontWidth,
            shape = plateShape,
            offset = 6.dp,
            alpha = 0.7f,
        )

        Cover(
            model = front,
            modifier = Modifier.width(frontWidth),
        )
    }
}

@Composable
private fun SeriesStackPlate(
    width: Dp,
    shape: Shape,
    offset: Dp,
    alpha: Float,
) {
    Box(
        modifier = Modifier
            .offset(x = offset)
            .width(width)
            .aspectRatio(2f / 3f)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = alpha)),
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun BookCardProgressBar(
    progress: BookCardProgress,
    modifier: Modifier = Modifier,
) {
    val fraction = progress.fraction.coerceIn(
        0f,
        1f,
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LinearWavyProgressIndicator(
            progress = { fraction },
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.weight(1f),
        )

        SoftcoverText(
            text = progress.label,
            style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
