package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import nl.rhaydus.softcover.feature.explore.presentation.event.HiddenSuggestionsEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.HiddenSuggestionsDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsUiState
import nl.rhaydus.toad.ActionScope

internal class DismissedBooksCollector : HiddenSuggestionsCollector {
    override suspend fun onLaunch(
        scope: ActionScope<HiddenSuggestionsUiState, HiddenSuggestionsEvent, HiddenSuggestionsLocalVariables>,
        dependencies: HiddenSuggestionsDependencies,
    ) {
        dependencies.getDismissedContinueSeriesBooksUseCase().collectLatest { books ->
            scope.setState {
                it.copy(
                    hiddenBooks = books,
                    hiddenBookCovers = books.associate { book -> book.bookId to book.toCoverUiModel() },
                    initialized = true,
                )
            }
        }
    }
}

// No edition to resolve against — a hidden row is URL-only (component-contract.md § 7.2 R9's mapper
// call, kept here since [DismissedSeriesBook] has no edition of its own).
private fun DismissedSeriesBook.toCoverUiModel() = (null as BookEdition?).toCoverUiModel(
    defaultEdition = null,
    coverlessTitle = title ?: "Hidden book",
    variant = CoverVariant.HiddenBookRow,
    fallbackCoverUrl = coverUrl,
)
