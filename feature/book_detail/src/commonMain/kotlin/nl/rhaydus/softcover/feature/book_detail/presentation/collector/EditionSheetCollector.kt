package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.button.PillButtonContent
import nl.rhaydus.softcover.core.component.button.PillButtonSize
import nl.rhaydus.softcover.core.component.button.PillButtonTone
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.openOrUpdate
import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.core.component.state.EmptyStateUiModel
import nl.rhaydus.softcover.core.component.state.EmptyStateVariant
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
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
                    searchQuery = it.editionSearchQuery,
                    isRequested = it.editionSheetRequested,
                    error = it.editionsError,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val bookTitle = snapshot.bookTitle
                val currentEdition = snapshot.currentEdition

                scope.setState {
                    val sheet = it.editionSheet
                    val model = if (bookTitle == null || currentEdition == null) {
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
                            emptyState = noMatchingEditionEmptyState().takeIf {
                                snapshot.isLoading.not() &&
                                    snapshot.error == null &&
                                    snapshot.editions.isEmpty()
                            },
                            error = snapshot.error,
                        )
                    }

                    when {
                        snapshot.isRequested.not() -> it.copy(editionSheet = sheet.requestClose())
                        model == null -> it
                        else -> it.copy(editionSheet = sheet.openOrUpdate(model = model))
                    }
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
        val isRequested: Boolean,
        val error: String?,
    )

    private companion object {
        const val SEARCH_PLACEHOLDER = "Search by ISBN or publisher"

        fun noMatchingEditionEmptyState() = EmptyStateUiModel(
            title = "Not among these editions",
            body = "Hardcover may not have it yet. Look it up by ISBN to find it or add it.",
            variant = EmptyStateVariant.Centered(
                button = PillButtonUiModel(
                    key = "look-up-by-isbn",
                    content = PillButtonContent.Label(
                        text = "Look up by ISBN",
                        leadingIcon = SoftcoverIcon.BarcodeScanner,
                    ),
                    tone = PillButtonTone.Filled,
                    size = PillButtonSize.Large,
                ),
            ),
        )
    }
}
