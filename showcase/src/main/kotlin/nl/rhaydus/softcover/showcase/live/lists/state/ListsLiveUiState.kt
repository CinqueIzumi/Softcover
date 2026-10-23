package nl.rhaydus.softcover.showcase.live.lists.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.lists.ChooseListsUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetActionRowUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceState
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceUiState
import nl.rhaydus.softcover.showcase.live.lists.mapper.listsDemos
import nl.rhaydus.softcover.showcase.live.lists.mapper.listsLiveTopBar
import nl.rhaydus.softcover.showcase.live.lists.model.ListsDemo

@Immutable
internal data class ListsLiveUiState(
    val topBar: TopBarUiModel = listsLiveTopBar(),
    val demos: ImmutableList<SheetActionRowUiModel<ListsDemo>> = listsDemos(),
    val sheet: SheetSession<ChooseListsUiModel, Nothing> = SheetSession.Closed,
    override val appearance: AppearanceState = AppearanceState(),
) : AppearanceUiState<ListsLiveUiState> {
    override fun withAppearance(appearance: AppearanceState): ListsLiveUiState = copy(appearance = appearance)
}
