package nl.rhaydus.softcover.feature.settings.presentation.state

import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.toad.UiState

internal data class SettingsScreenUiState(
    val useFloatingBarChecked: Boolean = true,
    val useDynamicColorChecked: Boolean = false,
    val readingStreakEnabledChecked: Boolean = true,
    val userDateStyle: DateStyle = DateStyle.DAY_MONTH_YEAR,
    val dropDownExpanded: Boolean = false,
    val appVersionName: String = "",
    val appVersionCode: Int = 0,
) : UiState
