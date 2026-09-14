package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeries
import nl.rhaydus.softcover.feature.explore.presentation.event.HiddenSuggestionsEvent
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
                    hiddenSeries = series,
                    hiddenSeriesCovers = series.associate { entry -> entry.seriesId to entry.toCoverUiModel() },
                    initialized = true,
                )
            }
        }
    }
}

// No edition to resolve against — a hidden row is URL-only (component-contract.md § 7.2 R9's mapper
// call, kept here since [DismissedSeries] has no edition of its own). 3dp radius, not the usual 4 —
// today's behaviour (CoverDimensions.forVariant), preserved deliberately.
private fun DismissedSeries.toCoverUiModel() = (null as BookEdition?).toCoverUiModel(
    defaultEdition = null,
    coverlessTitle = seriesName ?: "Hidden series",
    variant = CoverVariant.HiddenSeriesStack,
    fallbackCoverUrl = coverUrl,
)
