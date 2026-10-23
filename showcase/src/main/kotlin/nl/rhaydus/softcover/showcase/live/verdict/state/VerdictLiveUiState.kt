package nl.rhaydus.softcover.showcase.live.verdict.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.sheet.SheetActionRowUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceState
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceUiState
import nl.rhaydus.softcover.showcase.live.verdict.mapper.verdictDemos
import nl.rhaydus.softcover.showcase.live.verdict.mapper.verdictLiveTopBar
import nl.rhaydus.softcover.showcase.live.verdict.model.VerdictDemo
import nl.rhaydus.softcover.showcase.live.verdict.model.VerdictDemoUiModel

@Immutable
internal data class VerdictLiveUiState(
    val topBar: TopBarUiModel = verdictLiveTopBar(),
    val demos: ImmutableList<SheetActionRowUiModel<VerdictDemo>> = verdictDemos(),
    val sheet: SheetSession<VerdictDemoUiModel, Nothing> = SheetSession.Closed,
    override val appearance: AppearanceState = AppearanceState(),
) : AppearanceUiState<VerdictLiveUiState> {
    override fun withAppearance(appearance: AppearanceState): VerdictLiveUiState = copy(appearance = appearance)
}
