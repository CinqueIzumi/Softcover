package nl.rhaydus.softcover.showcase.live.topbar.mapper

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sidebar.SidebarRowUiModel
import nl.rhaydus.softcover.core.component.sidebar.SidebarRowVariant
import nl.rhaydus.softcover.core.component.sidebar.SidebarSectionUiModel
import nl.rhaydus.softcover.core.component.sidebar.SidebarUiModel
import nl.rhaydus.softcover.core.component.topbar.SearchTopBarUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.showcase.live.topbar.model.TopbarMode
import nl.rhaydus.softcover.showcase.live.topbar.model.TopbarRow
import nl.rhaydus.softcover.showcase.live.topbar.state.TopbarLiveUiState

internal fun topbarLiveState(): TopbarLiveUiState = TopbarLiveUiState(
    mode = TopbarMode.Standard,
    topBar = TopBarUiModel(title = "Top bars"),
    editorialBar = TopBarUiModel(title = "Editorial"),
    search = SearchTopBarUiModel(
        query = "",
        active = false,
        focused = false,
        isLoading = false,
    ),
    sidebar = topbarSidebar(mode = TopbarMode.Standard),
    lines = (1..40).map { "Chapter $it, still scrolling" }.toImmutableList(),
)

internal fun topbarSidebar(mode: TopbarMode): SidebarUiModel<TopbarRow> = SidebarUiModel(
    sections = persistentListOf(
        SidebarSectionUiModel(
            label = SectionHeaderUiModel.Label(eyebrow = "The bar above"),
            rows = persistentListOf(
                barRow(
                    mode = TopbarMode.Standard,
                    label = "Standard bar",
                    selected = mode,
                ),
                barRow(
                    mode = TopbarMode.Search,
                    label = "Search bar",
                    selected = mode,
                ),
                barRow(
                    mode = TopbarMode.Editorial,
                    label = "Editorial bar",
                    selected = mode,
                ),
            ),
        ),
        SidebarSectionUiModel(
            label = null,
            rows = persistentListOf(
                SidebarRowUiModel(
                    key = TopbarRow.Appearance,
                    label = "Appearance",
                    variant = SidebarRowVariant.Push,
                    icon = SoftcoverIcon.Palette,
                ),
            ),
        ),
    ),
)

private fun barRow(
    mode: TopbarMode,
    label: String,
    selected: TopbarMode,
): SidebarRowUiModel<TopbarRow> = SidebarRowUiModel(
    key = TopbarRow.Bar(mode = mode),
    label = label,
    variant = SidebarRowVariant.Destination(selected = mode == selected),
)
