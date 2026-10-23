package nl.rhaydus.softcover.showcase.live.chrome.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceState
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceUiState
import nl.rhaydus.softcover.showcase.live.chrome.model.ChromeButtonUiModel

@Immutable
internal data class ChromeLiveUiState(
    val cover: CoverUiModel,
    val buttons: ImmutableList<ChromeButtonUiModel>,
    val plainHeader: SectionHeaderUiModel,
    override val appearance: AppearanceState = AppearanceState(),
) : AppearanceUiState<ChromeLiveUiState> {
    override fun withAppearance(appearance: AppearanceState): ChromeLiveUiState = copy(appearance = appearance)
}
