package nl.rhaydus.softcover.feature.profile.presentation.state

import nl.rhaydus.softcover.core.component.control.ToggleRowUiModel

internal fun hideBooksWithoutDetailToggleFor(hide: Boolean): ToggleRowUiModel<String> = ToggleRowUiModel(
    key = "hide-books-without-detail",
    title = "Hide books without this detail",
    gloss = "Only count books with data for each bar.",
    checked = hide,
)
