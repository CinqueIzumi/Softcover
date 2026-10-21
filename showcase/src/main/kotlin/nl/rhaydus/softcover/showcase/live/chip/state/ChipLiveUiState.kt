package nl.rhaydus.softcover.showcase.live.chip.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceState
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceUiState
import nl.rhaydus.softcover.showcase.live.chip.model.ChipSectionUiModel

@Immutable
internal data class ChipLiveUiState(
    val topBar: TopBarUiModel,
    val sections: ImmutableList<ChipSectionUiModel>,
    override val appearance: AppearanceState = AppearanceState(),
) : AppearanceUiState<ChipLiveUiState> {
    override fun withAppearance(appearance: AppearanceState): ChipLiveUiState = copy(appearance = appearance)
}
