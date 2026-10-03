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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.WavyProgressIndicatorDefaults
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
import nl.rhaydus.softcover.core.component.cover.CoverDimensions
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.selection.SelectionMark
import nl.rhaydus.softcover.core.component.state.skeletonMask
import nl.rhaydus.softcover.core.component.text.SoftcoverText

@Composable
internal fun BookCardCoverLayout(
    cover: BookCardCover?,
    selection: BookCardSelection?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val selected = selection?.selected == true
    val mark = selection?.renderedMark

    val coverModel = cover?.model
    val badge = cover?.badge

    val shape = if (coverModel == null) {
        RectangleShape
    } else {
        RoundedCornerShape(CoverDimensions.forVariant(variant = coverModel.variant).cornerRadius)
    }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier.graphicsLayer {
                alpha = if (selection?.dimsUnselected == true) 0.55f else 1f
            },
        ) {
            CoverOverlay(model = badge) {
                Cover(
                    model = maskedCover(
                        cover = coverModel,
                        isLoading = isLoading,
                    ),
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

        if (mark != null) {
            SelectionMark(
                model = mark,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            )
        }
    }
}

@Composable
internal fun BookCardSeriesStack(
    front: CoverUiModel?,
    width: Dp,
    isLoading: Boolean,
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
            model = maskedCover(
                cover = front,
                isLoading = isLoading,
            ),
            modifier = Modifier.width(frontWidth),
        )
    }
}

internal fun maskedCover(
    cover: CoverUiModel?,
    isLoading: Boolean,
): CoverUiModel? = when {
    isLoading.not() -> cover
    cover == null -> null
    else -> cover.copy(
        source = null,
        isLoading = true,
        sharedTransitionKey = null,
    )
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

/**
 * @param barHeight Fixes the bar's height; `null` keeps the indicator's own height. A
 * [BookCardProgress.Reserved] slot takes the same height as a drawn bar.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun BookCardProgressBar(
    progress: BookCardProgress,
    modifier: Modifier = Modifier,
    barHeight: Dp? = null,
    isLoading: Boolean = false,
) {
    when (progress) {
        is BookCardProgress.Reserved -> Box(
            modifier = modifier
                .fillMaxWidth()
                .height(barHeight ?: WavyProgressIndicatorDefaults.LinearContainerHeight),
        )

        is BookCardProgress.Value -> {
            val fraction = progress.fraction.coerceIn(
                0f,
                1f,
            )

            val label = progress.label

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .skeletonMask(visible = isLoading),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LinearWavyProgressIndicator(
                    progress = { fraction },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.weight(1f).then(
                        if (barHeight != null) Modifier.height(barHeight) else Modifier,
                    ),
                )

                if (label != null) {
                    ProgressLabel(label = label)
                }
            }
        }
    }
}

@Composable
private fun ProgressLabel(label: String) {
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
        modifier = Modifier.width(reservedWidth),
    )
}
