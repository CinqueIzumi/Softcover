package nl.rhaydus.softcover.showcase.home.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceState
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceUiState
import nl.rhaydus.softcover.showcase.home.mapper.familyRows
import nl.rhaydus.softcover.showcase.home.mapper.homeMasthead
import nl.rhaydus.softcover.showcase.home.model.FamilyRowUiModel

@Immutable
internal data class HomeUiState(
    val masthead: PageMastheadUiModel = homeMasthead(),
    val families: ImmutableList<FamilyRowUiModel> = familyRows(),
    override val appearance: AppearanceState = AppearanceState(),
) : AppearanceUiState<HomeUiState> {
    override fun withAppearance(appearance: AppearanceState): HomeUiState = copy(appearance = appearance)
}
