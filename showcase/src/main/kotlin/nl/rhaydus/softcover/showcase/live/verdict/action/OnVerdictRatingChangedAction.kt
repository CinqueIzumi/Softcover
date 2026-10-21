package nl.rhaydus.softcover.showcase.live.verdict.action

import nl.rhaydus.softcover.core.component.sheet.withModel
import nl.rhaydus.softcover.showcase.live.verdict.event.VerdictLiveEvent
import nl.rhaydus.softcover.showcase.live.verdict.mapper.withRating
import nl.rhaydus.softcover.showcase.live.verdict.screenmodel.VerdictLiveDependencies
import nl.rhaydus.softcover.showcase.live.verdict.state.VerdictLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.verdict.state.VerdictLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnVerdictRatingChangedAction(val rating: Double?) : VerdictLiveAction {
    override suspend fun execute(
        dependencies: VerdictLiveDependencies,
        scope: ActionScope<VerdictLiveUiState, VerdictLiveEvent, VerdictLiveLocalVariables>,
    ) {
        scope.setState { it.copy(sheet = it.sheet.withModel { model -> model.withRating(rating = rating) }) }
    }
}
