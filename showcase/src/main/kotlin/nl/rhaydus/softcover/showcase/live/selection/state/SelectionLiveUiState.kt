package nl.rhaydus.softcover.showcase.live.selection.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceState
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceUiState
import nl.rhaydus.softcover.showcase.live.selection.model.SelectableCoverUiModel
import nl.rhaydus.softcover.showcase.live.selection.model.SelectableRowUiModel

@Immutable
internal data class SelectionLiveUiState(
    val topBar: TopBarUiModel,
    val coversHeader: SectionHeaderUiModel,
    val covers: ImmutableList<SelectableCoverUiModel>,
    val shelvesHeader: SectionHeaderUiModel,
    val shelves: ImmutableList<SelectableRowUiModel>,
    val formatsHeader: SectionHeaderUiModel,
    val formats: ImmutableList<SelectableRowUiModel>,
    override val appearance: AppearanceState = AppearanceState(),
) : AppearanceUiState<SelectionLiveUiState> {
    override fun withAppearance(appearance: AppearanceState): SelectionLiveUiState = copy(appearance = appearance)
}
