package nl.rhaydus.softcover.showcase.live.field.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceState
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceUiState
import nl.rhaydus.softcover.showcase.live.field.model.FieldSectionUiModel

@Immutable
internal data class FieldLiveUiState(
    val topBar: TopBarUiModel,
    val fields: ImmutableList<FieldSectionUiModel>,
    override val appearance: AppearanceState = AppearanceState(),
) : AppearanceUiState<FieldLiveUiState> {
    override fun withAppearance(appearance: AppearanceState): FieldLiveUiState = copy(appearance = appearance)
}
