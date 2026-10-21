package nl.rhaydus.softcover.showcase.live.share.action

import nl.rhaydus.softcover.core.component.sheet.open
import nl.rhaydus.softcover.showcase.live.share.event.ShareLiveEvent
import nl.rhaydus.softcover.showcase.live.share.mapper.shareDemoModel
import nl.rhaydus.softcover.showcase.live.share.model.ShareDemo
import nl.rhaydus.softcover.showcase.live.share.screenmodel.ShareLiveDependencies
import nl.rhaydus.softcover.showcase.live.share.state.ShareLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.share.state.ShareLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnDemoSelectedAction(val demo: ShareDemo) : ShareLiveAction {
    override suspend fun execute(
        dependencies: ShareLiveDependencies,
        scope: ActionScope<ShareLiveUiState, ShareLiveEvent, ShareLiveLocalVariables>,
    ) {
        scope.setState { it.copy(sheet = it.sheet.open(model = shareDemoModel(demo = demo))) }
    }
}
