package nl.rhaydus.softcover.feature.settings.presentation.model

import nl.rhaydus.softcover.core.component.control.ToggleRowUiModel

/** A toggle row's rendered state, built off the composition so a section reads it as-is. */
internal data class SettingsToggleRowUiModel<K>(
    val key: K,
    val row: ToggleRowUiModel,
)
