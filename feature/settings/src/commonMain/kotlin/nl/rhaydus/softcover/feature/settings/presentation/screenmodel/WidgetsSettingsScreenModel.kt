package nl.rhaydus.softcover.feature.settings.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.presentation.widget.HomeScreenWidgets
import nl.rhaydus.softcover.feature.settings.presentation.action.WidgetsSettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.collector.WidgetsSettingsCollector
import nl.rhaydus.softcover.feature.settings.presentation.event.WidgetsSettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.state.WidgetsSettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.WidgetsSettingsUiState
import nl.rhaydus.toad.ToadScreenModel

internal class WidgetsSettingsScreenModel(
    homeScreenWidgets: HomeScreenWidgets,
    appDispatchers: AppDispatchers,
    flows: List<WidgetsSettingsCollector>,
) : ToadScreenModel<
    WidgetsSettingsUiState,
    WidgetsSettingsEvent,
    WidgetsSettingsDependencies,
    WidgetsSettingsCollector,
    WidgetsSettingsLocalVariables,
    >(
    initialState = WidgetsSettingsUiState(),
    initialLocalVariables = WidgetsSettingsLocalVariables(),
    initializers = flows,
) {
    override val dependencies = WidgetsSettingsDependencies(
        homeScreenWidgets = homeScreenWidgets,
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
    )

    init {
        startInitializers()
    }

    fun runAction(action: WidgetsSettingsAction) = dispatch(action = action)
}
