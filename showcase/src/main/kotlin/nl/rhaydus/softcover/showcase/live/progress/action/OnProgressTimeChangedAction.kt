package nl.rhaydus.softcover.showcase.live.progress.action

import nl.rhaydus.softcover.core.component.progress.ProgressTimeUnit
import nl.rhaydus.softcover.core.component.sheet.withModel
import nl.rhaydus.softcover.showcase.live.progress.event.ProgressLiveEvent
import nl.rhaydus.softcover.showcase.live.progress.mapper.withTime
import nl.rhaydus.softcover.showcase.live.progress.screenmodel.ProgressLiveDependencies
import nl.rhaydus.softcover.showcase.live.progress.state.ProgressLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.progress.state.ProgressLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnProgressTimeChangedAction(
    val unit: ProgressTimeUnit,
    val text: String,
) : ProgressLiveAction {
    override suspend fun execute(
        dependencies: ProgressLiveDependencies,
        scope: ActionScope<ProgressLiveUiState, ProgressLiveEvent, ProgressLiveLocalVariables>,
    ) {
        scope.setState {
            it.copy(
                sheet = it.sheet.withModel { model ->
                    model.withTime(
                        unit = unit,
                        text = text,
                    )
                },
            )
        }
    }
}
