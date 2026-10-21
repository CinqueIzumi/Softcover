package nl.rhaydus.softcover.showcase.live.chrome.action

import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.showcase.live.chrome.event.ChromeLiveEvent
import nl.rhaydus.softcover.showcase.live.chrome.mapper.chromeButtonMessage
import nl.rhaydus.softcover.showcase.live.chrome.model.ChromeButtonKey
import nl.rhaydus.softcover.showcase.live.chrome.screenmodel.ChromeLiveDependencies
import nl.rhaydus.softcover.showcase.live.chrome.state.ChromeLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.chrome.state.ChromeLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnChromeButtonClickedAction(val key: ChromeButtonKey) : ChromeLiveAction {
    override suspend fun execute(
        dependencies: ChromeLiveDependencies,
        scope: ActionScope<ChromeLiveUiState, ChromeLiveEvent, ChromeLiveLocalVariables>,
    ) {
        SnackBarManager.showSnackbar(
            title = chromeButtonMessage(key = key),
        )
    }
}
