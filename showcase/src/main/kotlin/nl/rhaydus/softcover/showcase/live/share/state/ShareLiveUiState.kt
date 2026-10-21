package nl.rhaydus.softcover.showcase.live.share.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.share.ShareCardSheetUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetActionRowUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceState
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceUiState
import nl.rhaydus.softcover.showcase.live.share.mapper.shareDemos
import nl.rhaydus.softcover.showcase.live.share.mapper.shareLiveTopBar
import nl.rhaydus.softcover.showcase.live.share.model.ShareDemo

@Immutable
internal data class ShareLiveUiState(
    val topBar: TopBarUiModel = shareLiveTopBar(),
    val demos: ImmutableList<SheetActionRowUiModel<ShareDemo>> = shareDemos(),
    val sheet: SheetSession<ShareCardSheetUiModel, Nothing> = SheetSession.Closed,
    override val appearance: AppearanceState = AppearanceState(),
) : AppearanceUiState<ShareLiveUiState> {
    override fun withAppearance(appearance: AppearanceState): ShareLiveUiState = copy(appearance = appearance)
}
