package nl.rhaydus.softcover.showcase.family.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceState
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceUiState
import nl.rhaydus.softcover.showcase.family.model.EntrySectionUiModel

@Immutable
internal data class FamilyUiState(
    val topBar: TopBarUiModel,
    val entries: ImmutableList<EntrySectionUiModel>,
    override val appearance: AppearanceState = AppearanceState(),
) : AppearanceUiState<FamilyUiState> {
    override fun withAppearance(appearance: AppearanceState): FamilyUiState = copy(appearance = appearance)
}
