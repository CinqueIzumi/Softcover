package nl.rhaydus.softcover.feature.settings.presentation.state

import nl.rhaydus.softcover.core.designsystem.presentation.toad.UiState
import nl.rhaydus.softcover.core.domain.model.DateStyle

internal data class SettingsScreenUiState(
    val useFloatingBarChecked: Boolean = true,
    val useDynamicColorChecked: Boolean = false,
    val userDateStyle: DateStyle = DateStyle.DAY_MONTH_YEAR,
    val dropDownExpanded: Boolean = false,
    val appVersionName: String = "",
    val appVersionCode: Int = 0,
) : UiState
