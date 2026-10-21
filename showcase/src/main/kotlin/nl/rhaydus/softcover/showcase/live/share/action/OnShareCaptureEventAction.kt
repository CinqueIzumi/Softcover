package nl.rhaydus.softcover.showcase.live.share.action

import nl.rhaydus.softcover.core.component.share.ShareCardSheetEvent
import nl.rhaydus.softcover.core.component.sheet.withModel
import nl.rhaydus.softcover.showcase.live.share.event.ShareLiveEvent
import nl.rhaydus.softcover.showcase.live.share.mapper.reducing
import nl.rhaydus.softcover.showcase.live.share.screenmodel.ShareLiveDependencies
import nl.rhaydus.softcover.showcase.live.share.state.ShareLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.share.state.ShareLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnShareCaptureEventAction(val event: ShareCardSheetEvent) : ShareLiveAction {
    override suspend fun execute(
        dependencies: ShareLiveDependencies,
        scope: ActionScope<ShareLiveUiState, ShareLiveEvent, ShareLiveLocalVariables>,
    ) {
        scope.setState { it.copy(sheet = it.sheet.withModel { model -> model.reducing(event = event) }) }
    }
}
