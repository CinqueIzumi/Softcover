package nl.rhaydus.softcover.feature.profile.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope

internal class OnHideBooksWithoutDetailToggledAction(val newValue: Boolean) : StatsAtlasAction {
    override suspend fun execute(
        dependencies: StatsAtlasDependencies,
        scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>,
    ) {
        dependencies.setHideBooksWithoutDetailUseCase(enabled = newValue).onFailure {
            AppLog.e("$it")
        }
    }
}
