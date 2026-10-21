package nl.rhaydus.softcover.showcase.live.verdict.action

import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.showcase.live.verdict.event.VerdictLiveEvent
import nl.rhaydus.softcover.showcase.live.verdict.screenmodel.VerdictLiveDependencies
import nl.rhaydus.softcover.showcase.live.verdict.state.VerdictLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.verdict.state.VerdictLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnVerdictSavedAction : VerdictLiveAction {
    override suspend fun execute(
        dependencies: VerdictLiveDependencies,
        scope: ActionScope<VerdictLiveUiState, VerdictLiveEvent, VerdictLiveLocalVariables>,
    ) {
        scope.setState { it.copy(sheet = it.sheet.requestClose()) }

        SnackBarManager.showSnackbar(title = "Verdict noted. Nothing was saved.")
    }
}
