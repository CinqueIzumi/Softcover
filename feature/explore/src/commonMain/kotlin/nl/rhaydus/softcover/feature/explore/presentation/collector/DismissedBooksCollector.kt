package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.core.component.bookcard.toBookCardSet
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.uibinding.cover.coverUiModelFromUrl
import nl.rhaydus.softcover.feature.explore.presentation.action.OnUnblockBookAction
import nl.rhaydus.softcover.feature.explore.presentation.event.HiddenSuggestionsEvent
import nl.rhaydus.softcover.feature.explore.presentation.mapper.toHiddenBookCard
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
                    hiddenBooks = books.map { book ->
                        val cover = coverUiModelFromUrl(
                            url = book.coverUrl,
                            title = book.title ?: "Hidden book",
                            variant = CoverVariant.Flat,
                        )

                        book.toHiddenBookCard(cover = cover) to OnUnblockBookAction(book = book)
                    }.toBookCardSet(),
                    hiddenBooksGroupHeader = it.hiddenBooksGroupHeader.copy(count = books.size.toString()),
                )
            }
        }
    }
}
