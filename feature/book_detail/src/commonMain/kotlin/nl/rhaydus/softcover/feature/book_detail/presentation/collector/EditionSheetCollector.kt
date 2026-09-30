package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
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
                            editionSheetScaffoldFor(
                                bookTitle = bookTitle,
                                currentEdition = currentEdition,
                                selectedEdition = selectedEditionFor(
                                    editions = snapshot.editions,
                                    selectedEditionId = snapshot.selectedEditionId,
                                    currentEdition = currentEdition,
                                ),
                                cover = snapshot.cover,
                                isLoading = snapshot.isLoading,
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
    )
}
