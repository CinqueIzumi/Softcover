package nl.rhaydus.softcover.feature.profile.presentation.action

import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.statsScopeKey
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope

/**
 * Picks the scope every scoped section answers for. The pick is recorded before the key changes, so the
 * collector reacting to the key never sees the history's default scope win over it.
 */
internal class OnScopeSelectedAction(val statsScope: StatsScope) : StatsAtlasAction {
    override suspend fun execute(
        dependencies: StatsAtlasDependencies,
        scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>,
    ) {
        val selectedKey = statsScopeKey(statsScope)

        scope.setLocalVariables { it.copy(hasUserPickedScope = true) }
        scope.setState { it.copy(selectedScopeKey = selectedKey) }
    }
}
