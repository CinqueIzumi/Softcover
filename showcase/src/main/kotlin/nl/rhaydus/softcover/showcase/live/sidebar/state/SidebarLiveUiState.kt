package nl.rhaydus.softcover.showcase.live.sidebar.state

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.sidebar.SidebarUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceState
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceUiState

@Immutable
internal data class SidebarLiveUiState(
    val topBar: TopBarUiModel,
    val sidebar: SidebarUiModel<String>,
    override val appearance: AppearanceState = AppearanceState(),
) : AppearanceUiState<SidebarLiveUiState> {
    override fun withAppearance(appearance: AppearanceState): SidebarLiveUiState = copy(appearance = appearance)
}
