package nl.rhaydus.softcover.feature.settings.presentation.state

import nl.rhaydus.softcover.core.presentation.toad.UiState

data class SettingsScreenUiState(
    val useFloatingBarChecked: Boolean = true,
) : UiState