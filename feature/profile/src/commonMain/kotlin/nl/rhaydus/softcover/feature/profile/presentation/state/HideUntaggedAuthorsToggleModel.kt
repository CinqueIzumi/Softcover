package nl.rhaydus.softcover.feature.profile.presentation.state

import nl.rhaydus.softcover.core.component.control.ToggleRowUiModel

internal fun hideUntaggedAuthorsToggleFor(hide: Boolean): ToggleRowUiModel = ToggleRowUiModel(
    key = "hide-untagged-authors",
    title = "Hide untagged authors",
    gloss = "Read each bar over the authors it has data for.",
    checked = hide,
)
