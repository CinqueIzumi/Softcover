package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.callout.Banner
import nl.rhaydus.softcover.core.component.callout.BannerEvent
import nl.rhaydus.softcover.core.component.callout.BannerUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.deadline.DeadlineSummaryUiModel
import nl.rhaydus.softcover.core.component.modifier.shakeOnError
import nl.rhaydus.softcover.core.component.modifier.tactileClickable
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.presentation.prefetch.prefetchBookDetailOnPress
import nl.rhaydus.softcover.feature.reading.presentation.action.OnClearMutationFailureAction
import nl.rhaydus.softcover.feature.reading.presentation.action.ReadingAction
import nl.rhaydus.softcover.feature.reading.presentation.state.FeaturedBookUiModel
import nl.rhaydus.softcover.feature.reading.presentation.state.FeaturedProgressUiModel

/**
 * The Reading screen's emotional centre (design-system.md §5 "Reading featured-hero card"): a
 * transparent card shell whose top band fuses the pace-nudge ribbon (when one applies) onto the
 * card, over the themed backdrop that [FeaturedBackdropCard] paints.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun FeaturedBookCard(
    book: FeaturedBookUiModel,
    backdropCover: CoverUiModel?,
    heroCover: CoverUiModel?,
    progress: FeaturedProgressUiModel,
    deadlineCoverOverlay: CoverOverlayUiModel?,
    deadlineSummary: DeadlineSummaryUiModel?,
    mutationFailed: Boolean,
    paceForecast: String?,
    paceNudge: BannerUiModel?,
    onDismissPlanToday: () -> Unit,
    runAction: (ReadingAction) -> Unit,
    onClick: () -> Unit,
    onUpdateProgressClick: () -> Unit,
    onStartSession: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    val shape = Corners.xl

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.gutter)
            .then(if (isLoading) Modifier else Modifier.prefetchBookDetailOnPress(book.id))
            .tactileClickable(
                onClick = onClick,
                enabled = isLoading.not(),
                hoverShape = null,
            )
            .shakeOnError(
                trigger = mutationFailed,
                onShakeEnd = {
                    runAction(OnClearMutationFailureAction(bookId = book.id))
                },
            ),
        // FeaturedBackdropCard paints its own backdrop inside its loading mask, so the shell hides with the content.
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = shape,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (paceNudge != null) {
                Banner(
                    model = paceNudge,
                    onEvent = { event ->
                        when (event) {
                            BannerEvent.DismissClicked -> onDismissPlanToday()
                        }
                    },
                )
            }

            FeaturedBackdropCard(
                book = book,
                backdropCover = backdropCover,
                heroCover = heroCover,
                progress = progress,
                deadlineCoverOverlay = deadlineCoverOverlay,
                deadlineSummary = deadlineSummary,
                mutationFailed = mutationFailed,
                paceForecast = paceForecast,
                onUpdateProgressClick = onUpdateProgressClick,
                onStartSession = onStartSession,
                isLoading = isLoading,
            )
        }
    }
}
