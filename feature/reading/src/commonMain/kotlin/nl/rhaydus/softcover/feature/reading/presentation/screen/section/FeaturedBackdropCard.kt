package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import nl.rhaydus.common.currentLocalDateTime
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.softcover.core.component.badge.CoverOverlay
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.rememberCoverImageRequest
import nl.rhaydus.softcover.core.component.deadline.DeadlineSummaryLine
import nl.rhaydus.softcover.core.component.deadline.DeadlineSummaryUiModel
import nl.rhaydus.softcover.core.component.state.RevealedWhenPresent
import nl.rhaydus.softcover.core.component.state.skeletonMask
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.modifier.supportsBlur
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.reading.presentation.collector.greetingForNow
import nl.rhaydus.softcover.feature.reading.presentation.state.FeaturedBookUiModel
import nl.rhaydus.softcover.feature.reading.presentation.state.FeaturedProgressUiModel

/**
 * The featured hero's single backdrop: one blurred cover image on a `surfaceContainerHigh` base, under
 * a veil of the same colour that spans the full card height and thickens toward the foot. The
 * `onSurface` text of every content row stays legible over any cover, and a missing cover still
 * leaves a themed slab. The content column is the topmost child, so it paints over every background layer.
 */
@Composable
internal fun FeaturedBackdropCard(
    book: FeaturedBookUiModel,
    backdropCover: CoverUiModel?,
    heroCover: CoverUiModel?,
    progress: FeaturedProgressUiModel,
    deadlineCoverOverlay: CoverOverlayUiModel?,
    deadlineSummary: DeadlineSummaryUiModel?,
    mutationFailed: Boolean,
    paceForecast: String?,
    onUpdateProgressClick: () -> Unit,
    onStartSession: () -> Unit,
    isLoading: Boolean = false,
) {
    val isInspection = LocalInspectionMode.current
    // The backdrop never renders through Cover (component-contract.md's note on
    // rememberCoverImageRequest) — it needs Crop + blur with no shimmer and no coverless rung.
    val backdropRequest = if (backdropCover != null) {
        rememberCoverImageRequest(model = backdropCover)
    } else {
        null
    }
    val foreground = MaterialTheme.colorScheme.onSurface
    val veil = MaterialTheme.colorScheme.surfaceContainerHigh
    val lightVeilLift = if (veil.luminance() > 0.5f) 0.15f else 0f
    val blurSupported = supportsBlur()

    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .skeletonMask(visible = isLoading)
                .background(veil),
        ) {
            if (isInspection) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.30f),
                                ),
                            ),
                        ),
                )
            } else if (backdropRequest != null) {
                AsyncImage(
                    model = backdropRequest,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .blur(
                            radius = 6.dp,
                            edgeTreatment = BlurredEdgeTreatment.Unbounded,
                        ),
                )
            }

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = if (blurSupported) {
                                arrayOf(
                                    0f to veil.copy(alpha = 0.50f + lightVeilLift),
                                    0.4f to veil.copy(alpha = 0.58f + lightVeilLift),
                                    0.55f to veil.copy(alpha = 0.82f),
                                    1f to veil.copy(alpha = 0.92f),
                                )
                            } else {
                                arrayOf(
                                    0f to veil.copy(alpha = 0.70f + lightVeilLift),
                                    0.4f to veil.copy(alpha = 0.75f + lightVeilLift),
                                    0.55f to veil.copy(alpha = 0.90f),
                                    1f to veil.copy(alpha = 0.95f),
                                )
                            },
                        ),
                    ),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                FeaturedCover(
                    cover = heroCover,
                    deadlineCoverOverlay = deadlineCoverOverlay,
                    isLoading = isLoading,
                )

                FeaturedBackdropMeta(
                    book = book,
                    mutationFailed = mutationFailed,
                    paceForecast = paceForecast,
                    isLoading = isLoading,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            FeaturedProgressStat(model = progress)

            RevealedWhenPresent(value = deadlineSummary) { summary ->
                Column {
                    Spacer(modifier = Modifier.height(Spacing.md))

                    DeadlineSummaryLine(model = summary)
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            RhaydusButton(
                label = "Update progress",
                style = ButtonStyle.TONAL,
                size = ButtonSize.M,
                icon = drawableIconResource(
                    icon = SoftcoverIcon.Edit,
                    contentDescription = "Update progress",
                ),
                onClick = onUpdateProgressClick,
                enabled = isLoading.not(),
                modifier = Modifier
                    .fillMaxWidth()
                    .skeletonMask(visible = isLoading),
            )

            FeaturedSessionButton(
                bookId = book.id,
                isAudiobook = book.isAudiobook,
                onStartSession = onStartSession,
                isLoading = isLoading,
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            SoftcoverText(
                text = timeOfDayCaption(),
                style = MaterialTheme.editorialTypography.bodySmall,
                color = foreground.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .skeletonMask(visible = isLoading),
            )
        }
    }
}

/** The featured cover, 2:3 at 120dp wide, with the deadline badge overlay. */
@Composable
private fun FeaturedCover(
    cover: CoverUiModel?,
    deadlineCoverOverlay: CoverOverlayUiModel?,
    isLoading: Boolean,
) {
    CoverOverlay(model = deadlineCoverOverlay) {
        Cover(
            model = cover,
            modifier = Modifier
                .width(120.dp)
                .skeletonMask(visible = isLoading),
        )
    }
}

/** Mirrors [greetingForNow]'s hour buckets for the hero's italic time-of-day caption. */
private fun timeOfDayCaption(): String {
    val hour = currentLocalDateTime().hour

    return when (hour) {
        in 5..11 -> "A chapter with your coffee?"
        in 12..17 -> "Time for a few pages this afternoon."
        in 18..21 -> "Settle in with a chapter tonight."
        else -> "One more chapter before bed?"
    }
}
