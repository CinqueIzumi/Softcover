package nl.rhaydus.softcover.feature.profile.presentation.state

import nl.rhaydus.softcover.core.component.control.ToggleRowUiModel

internal fun hideUntaggedAuthorsToggleFor(hide: Boolean): ToggleRowUiModel<String> = ToggleRowUiModel(
    key = "hide-untagged-authors",
    title = "Hide untagged authors",
    gloss = "Only count authors with data for each bar.",
    checked = hide,
)
