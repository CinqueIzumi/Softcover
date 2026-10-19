package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.modifier.shakeOnError
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnClearMutationFailureAction
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

@Composable
internal fun BookHero(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
    onCoverClick: () -> Unit,
) {
    val failedEditionId = state.failedHeroEditionId

    Box(
        modifier = Modifier.shakeOnError(
            trigger = failedEditionId != null,
            onShakeEnd = {
                if (failedEditionId != null) {
                    runAction(OnClearMutationFailureAction(editionId = failedEditionId))
                }
            },
        ),
    ) {
        GeneralBookInfoSection(
            heroEdition = state.heroEdition,
            heroCover = state.heroCover,
            backdropCover = state.heroBackdropCover,
            isLoading = state.loadingBookDetails && state.book == null,
            isExpired = state.deadlineRow?.isExpired == true,
            rating = state.heroRating,
            title = state.heroTitle,
            titleTransitionKey = state.heroTitleTransitionKey,
            ratingTransitionKey = state.heroRatingTransitionKey,
            seriesText = state.heroSeriesText,
            releaseYear = state.heroReleaseYear,
            unreleasedBadge = state.unreleasedBadge,
            releaseCaption = state.releaseCaption,
            ownedCoverBadge = state.ownedCoverBadge,
            onCoverClick = onCoverClick,
        )
    }
}
