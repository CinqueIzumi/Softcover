package nl.rhaydus.softcover.showcase.live.sheet.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.sheet.SheetActionRowUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceState
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceUiState
import nl.rhaydus.softcover.showcase.live.sheet.mapper.sheetDemos
import nl.rhaydus.softcover.showcase.live.sheet.mapper.sheetLiveTopBar
import nl.rhaydus.softcover.showcase.live.sheet.model.SheetDemo
import nl.rhaydus.softcover.showcase.live.sheet.model.SheetDemoUiModel

@Immutable
internal data class SheetLiveUiState(
    val topBar: TopBarUiModel = sheetLiveTopBar(),
    val demos: ImmutableList<SheetActionRowUiModel<SheetDemo>> = sheetDemos(),
    val sheet: SheetSession<SheetDemoUiModel, Nothing> = SheetSession.Closed,
    override val appearance: AppearanceState = AppearanceState(),
) : AppearanceUiState<SheetLiveUiState> {
    override fun withAppearance(appearance: AppearanceState): SheetLiveUiState = copy(appearance = appearance)
}
