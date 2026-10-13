package nl.rhaydus.softcover.feature.explore.presentation.mapper

import nl.rhaydus.softcover.core.component.button.PillButtonContent
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.feature.explore.domain.model.ExploreSortMode

internal val ExploreSortMode.displayLabel: String
    get() = when (this) {
        ExploreSortMode.RELEVANCE -> "Relevance"
        ExploreSortMode.POPULARITY -> "Popularity"
    }

internal fun ExploreSortMode.toSortPillButton(): PillButtonUiModel = PillButtonUiModel(
    key = "sort",
    content = PillButtonContent.Label(text = "Sort · $displayLabel"),
)
