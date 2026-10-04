package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.core.component.bookcard.toBookCardSet
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.uibinding.cover.coverUiModelFromUrl
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeries
import nl.rhaydus.softcover.feature.explore.presentation.action.OnUnblockSeriesAction
import nl.rhaydus.softcover.feature.explore.presentation.event.HiddenSuggestionsEvent
import nl.rhaydus.softcover.feature.explore.presentation.mapper.toHiddenSeriesCard
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.HiddenSuggestionsDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsUiState
import nl.rhaydus.toad.ActionScope

internal class DismissedSeriesCollector : HiddenSuggestionsCollector {
    override suspend fun onLaunch(
        scope: ActionScope<HiddenSuggestionsUiState, HiddenSuggestionsEvent, HiddenSuggestionsLocalVariables>,
        dependencies: HiddenSuggestionsDependencies,
    ) {
        dependencies.getDismissedContinueSeriesUseCase().collectLatest { series ->
            scope.setState {
                it.copy(
                    hiddenSeries = series.map { entry ->
                        val cover = coverUiModelFromUrl(
                            url = entry.coverUrl,
                            title = entry.seriesName ?: "Hidden series",
                            variant = CoverVariant.Flat,
                        )

                        entry.toHiddenSeriesCard(cover = cover) to entry.toUnblockAction()
                    }.toBookCardSet(),
                    hiddenSeriesGroupHeader = it.hiddenSeriesGroupHeader.copy(count = series.size.toString()),
                )
            }
        }
    }
}

private fun DismissedSeries.toUnblockAction() = OnUnblockSeriesAction(
    seriesId = seriesId,
    seriesName = seriesName,
    coverUrl = coverUrl,
    authorText = authorText,
    bookCount = bookCount,
)
