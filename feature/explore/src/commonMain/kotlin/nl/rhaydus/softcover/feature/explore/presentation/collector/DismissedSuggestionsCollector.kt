package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import nl.rhaydus.softcover.core.component.bookcard.toBookCardSet
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.uibinding.cover.coverUiModelFromUrl
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeries
import nl.rhaydus.softcover.feature.explore.presentation.action.OnUnblockBookAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnUnblockSeriesAction
import nl.rhaydus.softcover.feature.explore.presentation.event.HiddenSuggestionsEvent
import nl.rhaydus.softcover.feature.explore.presentation.mapper.toHiddenBookCard
import nl.rhaydus.softcover.feature.explore.presentation.mapper.toHiddenSeriesCard
import nl.rhaydus.softcover.feature.explore.presentation.model.HiddenGroupHeaderUiModel
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.HiddenSuggestionsDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsUiState
import nl.rhaydus.toad.ActionScope

/** Writes both hidden lists and both group headers in one state change, so the surface leaves loading at once. */
internal class DismissedSuggestionsCollector : HiddenSuggestionsCollector {
    override suspend fun onLaunch(
        scope: ActionScope<HiddenSuggestionsUiState, HiddenSuggestionsEvent, HiddenSuggestionsLocalVariables>,
        dependencies: HiddenSuggestionsDependencies,
    ) {
        combine(
            dependencies.getDismissedContinueSeriesBooksUseCase(),
            dependencies.getDismissedContinueSeriesUseCase(),
        ) { books, series -> books to series }.collectLatest { (books, series) ->
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
                    hiddenSeries = series.map { entry ->
                        val cover = coverUiModelFromUrl(
                            url = entry.coverUrl,
                            title = entry.seriesName ?: "Hidden series",
                            variant = CoverVariant.Flat,
                        )

                        entry.toHiddenSeriesCard(cover = cover) to entry.toUnblockAction()
                    }.toBookCardSet(),
                    hiddenBooksGroupHeader = it.hiddenBooksGroupHeader.loaded(count = books.size),
                    hiddenSeriesGroupHeader = it.hiddenSeriesGroupHeader.loaded(count = series.size),
                )
            }
        }
    }
}

private fun HiddenGroupHeaderUiModel.loaded(count: Int): HiddenGroupHeaderUiModel {
    val inline = header as? SectionHeaderUiModel.Inline

    return copy(
        header = inline?.copy(isLoading = false) ?: header,
        count = count.toString(),
    )
}

private fun DismissedSeries.toUnblockAction() = OnUnblockSeriesAction(
    seriesId = seriesId,
    seriesName = seriesName,
    coverUrl = coverUrl,
    authorText = authorText,
    bookCount = bookCount,
)
