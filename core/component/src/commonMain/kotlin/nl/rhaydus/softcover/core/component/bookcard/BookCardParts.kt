package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.badge.CoverOverlay
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.cornerRadius
import nl.rhaydus.softcover.core.component.progress.ProgressBar
import nl.rhaydus.softcover.core.component.selection.SelectionMark
import nl.rhaydus.softcover.core.component.state.skeletonMask
import nl.rhaydus.softcover.core.component.text.SoftcoverText

@Composable
internal fun BookCardCoverLayout(
    cover: BookCardCover?,
    selection: BookCardSelection?,
    modifier: Modifier = Modifier,
) {
    val selected = selection?.selected == true
    val markSelected = selection?.markSelected

    val coverModel = cover?.model
    val badge = cover?.badge

    val shape = if (coverModel == null) {
        RectangleShape
    } else {
        RoundedCornerShape(coverModel.variant.cornerRadius())
    }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier.graphicsLayer {
                alpha = if (selection?.dimsUnselected == true) 0.55f else 1f
            },
        ) {
            CoverOverlay(model = badge) {
                Cover(
                    model = coverModel,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

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

        if (markSelected != null) {
            SelectionMark(
                selected = markSelected,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            )
        }
    }
}

@Composable
internal fun BookCardSeriesStack(
    front: CoverUiModel,
    width: Dp,
    isLoading: Boolean,
) {
    val frontWidth = width - 12.dp
    val plateShape = RoundedCornerShape(front.variant.cornerRadius())

    Box(modifier = Modifier.width(width)) {
        SeriesStackPlate(
            width = frontWidth,
            shape = plateShape,
            offset = 12.dp,
            alpha = 0.4f,
            isLoading = isLoading,
        )

        SeriesStackPlate(
            width = frontWidth,
            shape = plateShape,
            offset = 6.dp,
            alpha = 0.7f,
            isLoading = isLoading,
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
    isLoading: Boolean,
) {
    Box(
        modifier = Modifier
            .offset(x = offset)
            .width(width)
            .aspectRatio(2f / 3f)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = alpha))
            .skeletonMask(visible = isLoading),
    )
}

@Composable
internal fun BookCardProgressBar(
    progress: BookCardProgress,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    when (progress) {
        is BookCardProgress.Reserved -> Box(
            modifier = modifier
                .fillMaxWidth()
                .height(12.dp),
        )

        is BookCardProgress.Value -> {
            val label = progress.label

            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProgressBar(
                    fraction = progress.fraction,
                    modifier = Modifier.weight(1f),
                    isLoading = isLoading,
                )

                if (label != null) {
                    ProgressLabel(
                        label = label,
                        isLoading = isLoading,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressLabel(
    label: String,
    isLoading: Boolean,
) {
    val style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum")
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()

    val reservedWidth = remember(style, density) {
        with(density) {
            measurer.measure(
                text = "100%",
                style = style,
            ).size.width.toDp()
        }
    }

    SoftcoverText(
        text = label,
        style = style,
        textAlign = TextAlign.End,
        color = color,
        maxLines = 1,
        softWrap = false,
        modifier = Modifier
            .width(reservedWidth)
            .skeletonMask(visible = isLoading),
    )
}
