package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.reading.presentation.action.OnDismissPlanTodayAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnShowProgressSheetClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.ReadingAction
import nl.rhaydus.softcover.feature.reading.presentation.screen.MarkAsReadController
import nl.rhaydus.softcover.feature.reading.presentation.screen.markAsReadSlide
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState

@Composable
internal fun ReadingFeaturedSection(
    state: ReadingScreenUiState,
    runAction: (ReadingAction) -> Unit,
    onBookClick: (Book) -> Unit,
    onStartSession: (Book) -> Unit,
    controller: MarkAsReadController,
    modifier: Modifier = Modifier,
) {
    val featured = state.featuredBook

    FeaturedBookCard(
        book = state.visibleFeaturedBookModel,
        backdropCover = state.featuredBackdropCover,
        heroCover = state.featuredCover,
        progress = state.visibleFeaturedProgress,
        deadlineCoverOverlay = state.deadlineCoverOverlays[featured.id],
        deadlineSummary = state.visibleFeaturedDeadlineSummary,
        mutationFailed = featured.id in state.failedMutationBookIds,
        paceForecast = state.featuredBookPace,
        paceNudge = state.visibleFeaturedPaceNudge,
        onDismissPlanToday = {
            runAction(OnDismissPlanTodayAction(bookId = featured.id))
        },
        runAction = runAction,
        onClick = {
            onBookClick(featured)
        },
        onUpdateProgressClick = {
            runAction(OnShowProgressSheetClickAction(book = featured))
        },
        onStartSession = {
            onStartSession(featured)
        },
        modifier = modifier.markAsReadSlide(
            controller,
            featured.id,
        ),
        isLoading = state.isLoadingFeatured,
    )
}
