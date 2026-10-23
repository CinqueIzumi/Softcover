package nl.rhaydus.softcover.showcase.live.chip.action

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.showcase.live.chip.event.ChipLiveEvent
import nl.rhaydus.softcover.showcase.live.chip.mapper.afterDismiss
import nl.rhaydus.softcover.showcase.live.chip.screenmodel.ChipLiveDependencies
import nl.rhaydus.softcover.showcase.live.chip.state.ChipLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.chip.state.ChipLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnChipDismissedAction(
    val sectionKey: String,
    val chipKey: String,
) : ChipLiveAction {
    override suspend fun execute(
        dependencies: ChipLiveDependencies,
        scope: ActionScope<ChipLiveUiState, ChipLiveEvent, ChipLiveLocalVariables>,
    ) {
        scope.setState { state ->
            state.copy(
                sections = state.sections
                    .map { if (it.key == sectionKey) it.afterDismiss(chipKey = chipKey) else it }
                    .toImmutableList(),
            )
        }
    }
}
