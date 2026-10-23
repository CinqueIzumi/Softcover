package nl.rhaydus.softcover.showcase.live.topbar.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.sidebar.SidebarUiModel
import nl.rhaydus.softcover.core.component.topbar.SearchTopBarUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceState
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceUiState
import nl.rhaydus.softcover.showcase.live.topbar.model.TopbarMode
import nl.rhaydus.softcover.showcase.live.topbar.model.TopbarRow

@Immutable
internal data class TopbarLiveUiState(
    val mode: TopbarMode,
    val topBar: TopBarUiModel,
    val editorialBar: TopBarUiModel,
    val search: SearchTopBarUiModel,
    val sidebar: SidebarUiModel<TopbarRow>,
    val lines: ImmutableList<String>,
    override val appearance: AppearanceState = AppearanceState(),
) : AppearanceUiState<TopbarLiveUiState> {
    override fun withAppearance(appearance: AppearanceState): TopbarLiveUiState = copy(appearance = appearance)
}
