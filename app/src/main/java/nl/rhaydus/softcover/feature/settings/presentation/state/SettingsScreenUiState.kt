package nl.rhaydus.softcover.feature.settings.presentation.state

import nl.rhaydus.softcover.core.presentation.toad.UiState
import nl.rhaydus.softcover.feature.settings.domain.model.DateStyle

data class SettingsScreenUiState(
    val useFloatingBarChecked: Boolean = true,
    val useDynamicColorChecked: Boolean = false,
    val userDateStyle: DateStyle = DateStyle.DAY_MONTH_YEAR,
    val dropDownExpanded: Boolean = false,
) : UiState