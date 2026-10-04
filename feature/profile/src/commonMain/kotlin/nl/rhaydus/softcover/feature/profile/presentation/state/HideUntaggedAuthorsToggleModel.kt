package nl.rhaydus.softcover.feature.profile.presentation.state

import nl.rhaydus.softcover.core.component.control.ToggleUiModel
import nl.rhaydus.softcover.core.component.control.ToggleVariant

internal fun hideUntaggedAuthorsToggleFor(hide: Boolean): ToggleUiModel = ToggleUiModel(
    key = "hide-untagged-authors",
    checked = hide,
    description = "Hide untagged authors",
    variant = ToggleVariant.Switch,
)
