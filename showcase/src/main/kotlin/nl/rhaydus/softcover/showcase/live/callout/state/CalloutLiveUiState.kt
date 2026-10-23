package nl.rhaydus.softcover.showcase.live.callout.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.callout.BannerUiModel
import nl.rhaydus.softcover.core.component.callout.CalloutUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceState
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceUiState

@Immutable
internal data class CalloutLiveUiState(
    val topBar: TopBarUiModel,
    val banners: ImmutableList<BannerUiModel>,
    val callouts: ImmutableList<CalloutUiModel>,
    override val appearance: AppearanceState = AppearanceState(),
) : AppearanceUiState<CalloutLiveUiState> {
    override fun withAppearance(appearance: AppearanceState): CalloutLiveUiState = copy(appearance = appearance)
}
