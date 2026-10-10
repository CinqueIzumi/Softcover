package nl.rhaydus.softcover.feature.explore.presentation.action

import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.model.ContinueSeriesDismissalKind
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

internal class OnContinueSeriesMenuDismissedAction : ExploreAction {
    override suspend fun execute(
        dependencies: ExploreDependencies,
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
    ) {
        val pending = scope.currentState.pendingContinueSeriesDismissal ?: return

        scope.setState {
            it.copy(pendingContinueSeriesDismissal = null)
        }

        val book = scope.currentState.continueSeriesBooks.find { it.id == pending.bookId } ?: return

        val authorText = book.authorString.takeIf { it.isNotBlank() }

        val dismissal: ExploreAction = when (pending.kind) {
            ContinueSeriesDismissalKind.THIS_BOOK -> OnDismissContinueSeriesBookAction(
                book = DismissedSeriesBook(
                    bookId = book.id,
                    title = book.title,
                    coverUrl = book.coverUrl,
                    authorText = authorText,
                    seriesName = book.bookSeries?.name,
                    seriesId = book.bookSeries?.id,
                    // The series cursor moves past this book's *last* position, so an omnibus
                    // spanning several positions can't re-match on the next fetch.
                    seriesPosition = book.positionsInSeries.lastOrNull(),
                ),
            )

            ContinueSeriesDismissalKind.WHOLE_SERIES -> {
                val series = book.bookSeries ?: return

                OnDismissContinueSeriesAction(
                    seriesId = series.id,
                    seriesName = series.name,
                    coverUrl = book.coverUrl,
                    authorText = authorText,
                    bookCount = series.amountOfBooks,
                )
            }
        }

        dismissal.execute(
            dependencies = dependencies,
            scope = scope,
        )
    }
}
