package nl.rhaydus.softcover.feature.settings.presentation.action

import nl.rhaydus.softcover.feature.settings.presentation.event.CurrencySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.CurrencySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.CurrencySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.CurrencySettingsUiState
import nl.rhaydus.toad.UiAction

internal sealed interface CurrencySettingsAction : UiAction<
    CurrencySettingsDependencies,
    CurrencySettingsUiState,
    CurrencySettingsEvent,
    CurrencySettingsLocalVariables,
>
