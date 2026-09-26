package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import nl.rhaydus.common.currentLocalDateTime
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.component.badge.CoverOverlay
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.badge.DeadlineSummaryLine
import nl.rhaydus.softcover.core.component.badge.DeadlineSummaryUiModel
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.rememberCoverImageRequest
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.ReadingHeroBackdropForeground
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.personal.domain.model.ReadingPaceForecast
import nl.rhaydus.softcover.feature.reading.presentation.action.OnShowProgressSheetClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.ReadingAction
import nl.rhaydus.softcover.feature.reading.presentation.screen.greetingForNow

/**
 * The single full-card blurred-cover backdrop, restored to the pre-redesign treatment: ONE
 * continuous image (full-opacity blur, a black top scrim for the series-eyebrow's legibility near
 * the top edge, and a fade at the foot into [cardColor]) behind ALL of the card's content — cover,
 * meta, the page-count/percentage stat, the wavy bar, the deadline row, both pills, and the caption
 * — rather than a backdrop band handed off to a separate flat body surface below it. The content
 * column is the last (topmost) child, so it paints over every background layer including the fade.
 */
@Composable
internal fun FeaturedBackdropCard(
    book: Book,
    backdropCover: CoverUiModel?,
    heroCover: CoverUiModel?,
    deadlineCoverOverlay: CoverOverlayUiModel?,
    deadlineSummary: DeadlineSummaryUiModel?,
    mutationFailed: Boolean,
    paceForecast: ReadingPaceForecast?,
    cardColor: Color,
    runAction: (ReadingAction) -> Unit,
) {
    val isInspection = LocalInspectionMode.current
    // ReadingHeroBackdrop never renders through Cover (component-contract.md's note on
    // rememberCoverImageRequest) — it needs Crop + blur with no shimmer and no coverless rung.
    val backdropRequest = if (backdropCover != null) {
        rememberCoverImageRequest(model = backdropCover)
    } else {
        null
    }
    val foreground = ReadingHeroBackdropForeground

    Box(modifier = Modifier.fillMaxWidth()) {
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
                        radius = 64.dp,
                        edgeTreatment = BlurredEdgeTreatment.Unbounded,
                    ),
            )
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color.Black.copy(alpha = 0.45f),
                            0.35f to Color.Black.copy(alpha = 0.15f),
                            0.55f to Color.Transparent,
                            1f to Color.Transparent,
                        ),
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to cardColor.copy(alpha = 0f),
                            0.40f to cardColor.copy(alpha = 0.15f),
                            0.60f to cardColor.copy(alpha = 0.92f),
                            1f to cardColor,
                        ),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                FeaturedCover(
                    cover = heroCover,
                    deadlineCoverOverlay = deadlineCoverOverlay,
                )

                FeaturedBackdropMeta(
                    book = book,
                    mutationFailed = mutationFailed,
                    paceForecast = paceForecast,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            FeaturedProgressStat(
                book = book,
                foreground = foreground,
            )

            // The deadline readout arrives from its own collector, independently of the progress block
            // above — and a book may never carry a deadline at all — so it reveals in place rather
            // than reserving height (the same register as the pace row above; design-system.md §5
            // "Book-detail in-progress stat").
            val playMotion = playDecorativeMotion()
            val revealEnter = if (playMotion) expandVertically() + fadeIn() else EnterTransition.None
            val revealExit = if (playMotion) shrinkVertically() + fadeOut() else ExitTransition.None

            var lastDeadlineSummary by remember { mutableStateOf(deadlineSummary) }

            if (deadlineSummary != null) {
                lastDeadlineSummary = deadlineSummary
            }

            AnimatedVisibility(
                visible = deadlineSummary != null,
                enter = revealEnter,
                exit = revealExit,
            ) {
                lastDeadlineSummary?.let { summary ->
                    Column {
                        Spacer(modifier = Modifier.height(15.dp))

                        DeadlineSummaryLine(model = summary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            RhaydusButton(
                label = "Update progress",
                style = ButtonStyle.FILLED,
                size = ButtonSize.M,
                icon = drawableIconResource(
                    icon = SoftcoverIcon.Edit,
                    contentDescription = "Update progress icon",
                ),
                onClick = { runAction(OnShowProgressSheetClickAction(book = book)) },
                modifier = Modifier.fillMaxWidth(),
            )

            FeaturedSessionButton(
                book = book,
                foreground = foreground,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = timeOfDayCaption(),
                style = MaterialTheme.editorialTypography.bodySmall,
                color = foreground.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** The featured cover, 2:3 at 108dp wide, with the deadline badge overlay. */
@Composable
private fun FeaturedCover(
    cover: CoverUiModel?,
    deadlineCoverOverlay: CoverOverlayUiModel?,
) {
    CoverOverlay(model = deadlineCoverOverlay) {
        Cover(
            model = cover,
            modifier = Modifier.width(108.dp),
        )
    }
}

/** Mirrors [greetingForNow]'s hour buckets for the hero's italic time-of-day caption. */
private fun timeOfDayCaption(): String {
    val hour = currentLocalDateTime().hour

    return when (hour) {
        in 5..11 -> "Mornings are made for a few quiet pages before the day gets loud."
        in 12..17 -> "A good afternoon for picking up where you left off."
        in 18..21 -> "Evenings were made for this. Pick up where you stopped."
        else -> "The late hours are yours. Pick up where you stopped."
    }
}
