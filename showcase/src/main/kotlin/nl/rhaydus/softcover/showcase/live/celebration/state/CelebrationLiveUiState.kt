package nl.rhaydus.softcover.showcase.live.celebration.state

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.celebration.MarkAsReadBurstUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceState
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceUiState

@Immutable
internal data class CelebrationLiveUiState(
    val topBar: TopBarUiModel,
    val cardBurst: MarkAsReadBurstUiModel,
    val screenBurst: MarkAsReadBurstUiModel,
    override val appearance: AppearanceState = AppearanceState(),
) : AppearanceUiState<CelebrationLiveUiState> {
    override fun withAppearance(appearance: AppearanceState): CelebrationLiveUiState = copy(appearance = appearance)
}
