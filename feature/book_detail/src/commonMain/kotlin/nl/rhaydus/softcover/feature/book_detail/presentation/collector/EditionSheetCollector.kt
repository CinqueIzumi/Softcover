package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.EditionSheetUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.state.editionSheetScaffoldFor
import nl.rhaydus.softcover.feature.book_detail.presentation.state.selectedEditionFor
import nl.rhaydus.toad.ActionScope

internal class EditionSheetCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map {
                EditionSheetSnapshot(
                    bookTitle = it.book?.title,
                    currentEdition = it.displayedEdition,
                    editions = it.filteredEditions,
                    selectedEditionId = it.selectedEditionId,
                    cover = it.editionSheetHeaderCover,
                    isLoading = it.loadingEditions,
                    cards = it.editionCards,
                    countHeader = it.editionCountHeader,
                    searchQuery = it.editionSheet?.searchQuery.orEmpty(),
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val bookTitle = snapshot.bookTitle
                val currentEdition = snapshot.currentEdition

                scope.setState {
                    it.copy(
                        editionSheet = if (bookTitle == null || currentEdition == null) {
                            null
                        } else {
                            EditionSheetUiModel(
                                scaffold = editionSheetScaffoldFor(
                                    bookTitle = bookTitle,
                                    currentEdition = currentEdition,
                                    selectedEdition = selectedEditionFor(
                                        editions = snapshot.editions,
                                        selectedEditionId = snapshot.selectedEditionId,
                                        currentEdition = currentEdition,
                                    ),
                                    cover = snapshot.cover,
                                    isLoading = snapshot.isLoading,
                                ),
                                cards = snapshot.cards,
                                countHeader = snapshot.countHeader,
                                searchQuery = snapshot.searchQuery,
                                searchPlaceholder = SEARCH_PLACEHOLDER,
                                emptyMessage = EMPTY_MESSAGE,
                            )
                        },
                    )
                }
            }
    }

    private data class EditionSheetSnapshot(
        val bookTitle: String?,
        val currentEdition: BookEdition?,
        val editions: List<BookEdition>,
        val selectedEditionId: Int?,
        val cover: CoverUiModel?,
        val isLoading: Boolean,
        val cards: ImmutableList<BookCardUiModel>,
        val countHeader: SectionHeaderUiModel,
        val searchQuery: String,
    )

    private companion object {
        const val SEARCH_PLACEHOLDER = "Search by ISBN or publisher"
        const val EMPTY_MESSAGE = "No editions match your search"
    }
}
