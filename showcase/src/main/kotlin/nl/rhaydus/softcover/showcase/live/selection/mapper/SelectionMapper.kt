package nl.rhaydus.softcover.showcase.live.selection.mapper

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.showcase.live.selection.model.SelectableCoverUiModel
import nl.rhaydus.softcover.showcase.live.selection.model.SelectableRowUiModel
import nl.rhaydus.softcover.showcase.live.selection.state.SelectionLiveUiState

internal fun selectionLiveState(): SelectionLiveUiState = SelectionLiveUiState(
    topBar = TopBarUiModel(title = "Selection"),
    coversHeader = SectionHeaderUiModel.Section(
        eyebrow = "Selection mark",
        description = "A mark over a cover, filled when the book is picked.",
    ),
    covers = listOf(
        "Piranesi",
        "The Name of the Wind",
        "Station Eleven",
        "Middlemarch",
        "Gilead",
        "Kindred",
    )
        .mapIndexed { index, title ->
            SelectableCoverUiModel(
                key = title,
                cover = CoverUiModel(
                    source = null,
                    coverlessTitle = title,
                    variant = CoverVariant.Shelf,
                ),
                selected = index == 0,
            )
        }
        .toImmutableList(),
    shelvesHeader = SectionHeaderUiModel.Section(
        eyebrow = "Selection mark, on a surface",
        description = "The same mark leading a row.",
    ),
    shelves = rows(titles = listOf("Currently reading", "Want to read", "Read", "Did not finish")),
    formatsHeader = SectionHeaderUiModel.Section(
        eyebrow = "Selection check",
        description = "A bare check, for lists that have no room for a mark.",
    ),
    formats = rows(titles = listOf("Paperback", "Hardback", "Audiobook", "E-book")),
)

private fun rows(titles: List<String>) = titles
    .mapIndexed { index, title ->
        SelectableRowUiModel(
            key = title,
            title = title,
            selected = index == 0,
        )
    }
    .toImmutableList()
