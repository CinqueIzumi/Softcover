package nl.rhaydus.softcover.showcase.live.traits.state

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.component.traits.HowItReadBlockUiModel
import nl.rhaydus.softcover.core.component.traits.HowItReadSheetUiModel
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceState
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceUiState
import nl.rhaydus.softcover.showcase.live.traits.mapper.howItReadEmptyBlock
import nl.rhaydus.softcover.showcase.live.traits.mapper.howItReadHeader
import nl.rhaydus.softcover.showcase.live.traits.mapper.howItReadLiveTopBar

@Immutable
internal data class HowItReadLiveUiState(
    val topBar: TopBarUiModel = howItReadLiveTopBar(),
    val header: SectionHeaderUiModel = howItReadHeader(),
    val block: HowItReadBlockUiModel = howItReadEmptyBlock(),
    val sheet: SheetSession<HowItReadSheetUiModel, Nothing> = SheetSession.Closed,
    override val appearance: AppearanceState = AppearanceState(),
) : AppearanceUiState<HowItReadLiveUiState> {
    override fun withAppearance(appearance: AppearanceState): HowItReadLiveUiState = copy(appearance = appearance)
}
