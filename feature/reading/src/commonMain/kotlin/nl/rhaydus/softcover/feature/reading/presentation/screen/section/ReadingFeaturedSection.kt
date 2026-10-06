package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.reading.presentation.action.OnDismissPlanTodayAction
import nl.rhaydus.softcover.feature.reading.presentation.action.ReadingAction
import nl.rhaydus.softcover.feature.reading.presentation.screen.MarkAsReadController
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState

@Composable
internal fun ReadingFeaturedSection(
    state: ReadingScreenUiState,
    runAction: (ReadingAction) -> Unit,
    onBookClick: (Book) -> Unit,
    controller: MarkAsReadController,
    modifier: Modifier = Modifier,
) {
    val featured = state.featuredBook

    FeaturedBookCard(
        book = featured,
        backdropCover = state.featuredBackdropCover,
        heroCover = state.featuredCover,
        progress = state.featuredProgress,
        deadlineCoverOverlay = state.deadlineCoverOverlays[featured.id],
        deadlineSummary = state.featuredDeadlineSummary,
        mutationFailed = featured.id in state.failedMutationBookIds,
        paceForecast = state.featuredBookPace,
        paceNudge = state.featuredPaceNudge,
        onDismissPlanToday = {
            runAction(OnDismissPlanTodayAction(bookId = featured.id))
        },
        runAction = runAction,
        onBookClick = onBookClick,
        modifier = modifier.then(controller.slideModifier(featured.id)),
        isLoading = state.isLoadingFeatured,
    )
}
