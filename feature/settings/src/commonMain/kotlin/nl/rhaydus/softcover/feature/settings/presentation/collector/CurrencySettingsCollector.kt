package nl.rhaydus.softcover.feature.settings.presentation.collector

import nl.rhaydus.softcover.feature.settings.presentation.event.CurrencySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.CurrencySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.CurrencySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.CurrencySettingsUiState
import nl.rhaydus.toad.Collector

internal sealed interface CurrencySettingsCollector : Collector<
    CurrencySettingsUiState,
    CurrencySettingsEvent,
    CurrencySettingsDependencies,
    CurrencySettingsLocalVariables,
>
