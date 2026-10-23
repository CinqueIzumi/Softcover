package nl.rhaydus.softcover.showcase.live.progress.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.progress.ProgressSheetUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetActionRowUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceState
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceUiState
import nl.rhaydus.softcover.showcase.live.progress.mapper.progressDemos
import nl.rhaydus.softcover.showcase.live.progress.mapper.progressLiveTopBar
import nl.rhaydus.softcover.showcase.live.progress.model.ProgressDemo

@Immutable
internal data class ProgressLiveUiState(
    val topBar: TopBarUiModel = progressLiveTopBar(),
    val demos: ImmutableList<SheetActionRowUiModel<ProgressDemo>> = progressDemos(),
    val sheet: SheetSession<ProgressSheetUiModel, Nothing> = SheetSession.Closed,
    override val appearance: AppearanceState = AppearanceState(),
) : AppearanceUiState<ProgressLiveUiState> {
    override fun withAppearance(appearance: AppearanceState): ProgressLiveUiState = copy(appearance = appearance)
}
