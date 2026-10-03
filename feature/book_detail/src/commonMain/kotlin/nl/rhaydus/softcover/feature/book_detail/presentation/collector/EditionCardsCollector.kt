package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.editionCountHeaderFor
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toEditionCard
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.filterByEditionSearch
import nl.rhaydus.toad.ActionScope

/**
 * Maps the edition selector's rows off the composition (R9).
 *
 * A collector because the rows read the edition list, its search filter, the in-progress pick and the
 * tracked edition, each written by a different action.
 */
internal class EditionCardsCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map {
                EditionCardsSnapshot(
                    editions = it.editions.filterByEditionSearch(it.editionSearchQuery),
                    fallbackEdition = it.book?.defaultEdition ?: it.initialCover?.defaultEdition,
                    selectedEditionId = it.selectedEditionId,
                    trackedEditionId = it.displayedEdition?.id,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val cards = snapshot.editions
                    .map { edition ->
                        edition.toEditionCard(
                            fallbackEdition = snapshot.fallbackEdition,
                            selected = edition.id == snapshot.selectedEditionId,
                            tracked = edition.id == snapshot.trackedEditionId,
                        )
                    }
                    .toImmutableList()

                scope.setState {
                    it.copy(
                        editionCards = cards,
                        editionCountHeader = editionCountHeaderFor(count = cards.size),
                    )
                }
            }
    }

    private data class EditionCardsSnapshot(
        val editions: List<BookEdition>,
        val fallbackEdition: BookEdition?,
        val selectedEditionId: Int?,
        val trackedEditionId: Int?,
    )
}
