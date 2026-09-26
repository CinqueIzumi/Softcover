package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScale
import nl.rhaydus.designsystem.modifier.shakeOnError
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.badge.DeadlineSummaryUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.personal.domain.model.ReadingPaceForecast
import nl.rhaydus.softcover.core.presentation.prefetch.prefetchBookDetailOnPress
import nl.rhaydus.softcover.feature.reading.presentation.action.OnClearMutationFailureAction
import nl.rhaydus.softcover.feature.reading.presentation.action.ReadingAction

/**
 * The Reading screen's emotional centre (design-system.md §5 "Reading featured-hero card"): a
 * `surfaceContainerLow` card whose top band fuses the pace-nudge ribbon (when one applies) directly
 * onto the card rather than floating in-flow above it, a dark fixed-ink backdrop carrying the cover
 * + title/byline/pace meta, and a body section (page count, wavy progress, deadline readout, hero
 * actions) on the card's own surface colour.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun FeaturedBookCard(
    book: Book,
    backdropCover: CoverUiModel?,
    heroCover: CoverUiModel?,
    deadlineCoverOverlay: CoverOverlayUiModel?,
    deadlineSummary: DeadlineSummaryUiModel?,
    mutationFailed: Boolean,
    paceForecast: ReadingPaceForecast?,
    planTodayMessage: String?,
    onDismissPlanToday: () -> Unit,
    runAction: (ReadingAction) -> Unit,
    onBookClick: (Book) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(22.dp)
    val cardColor = MaterialTheme.colorScheme.surfaceContainerLow
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .prefetchBookDetailOnPress(book.id)
            .pointerHandCursor()
            .pressScale(interactionSource)
            .shakeOnError(
                trigger = mutationFailed,
                onShakeEnd = {
                    runAction(OnClearMutationFailureAction(bookId = book.id))
                },
            ),
        color = cardColor,
        shape = shape,
        onClick = { onBookClick(book) },
        interactionSource = interactionSource,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (planTodayMessage != null) {
                PaceNudgeRibbon(
                    text = planTodayMessage,
                    onDismiss = onDismissPlanToday,
                )
            }

            FeaturedBackdropCard(
                book = book,
                backdropCover = backdropCover,
                heroCover = heroCover,
                deadlineCoverOverlay = deadlineCoverOverlay,
                deadlineSummary = deadlineSummary,
                mutationFailed = mutationFailed,
                paceForecast = paceForecast,
                cardColor = cardColor,
                runAction = runAction,
            )
        }
    }
}
