package nl.rhaydus.softcover.feature.explore.presentation.collector

import nl.rhaydus.softcover.feature.explore.presentation.event.HiddenSuggestionsEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.HiddenSuggestionsDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsUiState
import nl.rhaydus.toad.ActionScope

/**
 * Runs the one-shot metadata backfill for legacy hidden rows when the screen loads. Lives as a
 * [HiddenSuggestionsCollector] so the kick-off goes through TOAD's documented once-per-launch hook
 * rather than a raw side effect in the screen model's `init`.
 */
internal class EnrichMetadataCollector : HiddenSuggestionsCollector {
    override suspend fun onLaunch(
        scope: ActionScope<HiddenSuggestionsUiState, HiddenSuggestionsEvent, HiddenSuggestionsLocalVariables>,
        dependencies: HiddenSuggestionsDependencies,
    ) {
        dependencies.enrichDismissedContinueSeriesMetadataUseCase()
    }
}
