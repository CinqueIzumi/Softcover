package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.koboQuery
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.searchIsbn
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toExternalLinkPills
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/**
 * The only writer of [BookDetailUiState.externalLinks]: the "Find it" pills, derived from
 * [BookDetailUiState.displayedEdition], which several collectors and actions move.
 */
internal class ExternalLinksCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map {
                ExternalLinksSnapshot(
                    hasIsbn = it.displayedEdition?.searchIsbn != null,
                    hasKoboQuery = it.displayedEdition?.koboQuery != null,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val pills: ImmutableList<PillButtonUiModel>? = toExternalLinkPills(
                    hasIsbn = snapshot.hasIsbn,
                    hasKoboQuery = snapshot.hasKoboQuery,
                )

                scope.setState { it.copy(externalLinks = pills) }
            }
    }

    private data class ExternalLinksSnapshot(
        val hasIsbn: Boolean,
        val hasKoboQuery: Boolean,
    )
}
