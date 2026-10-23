package nl.rhaydus.softcover.showcase.live.quote.action

import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.showcase.live.quote.event.QuoteLiveEvent
import nl.rhaydus.softcover.showcase.live.quote.screenmodel.QuoteLiveDependencies
import nl.rhaydus.softcover.showcase.live.quote.state.QuoteLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.quote.state.QuoteLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnQuoteSavedAction : QuoteLiveAction {
    override suspend fun execute(
        dependencies: QuoteLiveDependencies,
        scope: ActionScope<QuoteLiveUiState, QuoteLiveEvent, QuoteLiveLocalVariables>,
    ) {
        SnackBarManager.showSnackbar(title = "Quote noted. Nothing was saved.")
    }
}
