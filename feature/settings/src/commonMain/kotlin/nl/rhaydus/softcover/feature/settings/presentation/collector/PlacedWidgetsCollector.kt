package nl.rhaydus.softcover.feature.settings.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.feature.settings.presentation.event.WidgetsSettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.mapper.placedWidgetRowsFor
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.WidgetsSettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.WidgetsSettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.WidgetsSettingsUiState
import nl.rhaydus.toad.ActionScope

internal class PlacedWidgetsCollector : WidgetsSettingsCollector {
    override suspend fun onLaunch(
        scope: ActionScope<WidgetsSettingsUiState, WidgetsSettingsEvent, WidgetsSettingsLocalVariables>,
        dependencies: WidgetsSettingsDependencies,
    ) {
        dependencies.homeScreenWidgets.observePlaced().collectLatest { widgets ->
            scope.setState { it.copy(placedRows = placedWidgetRowsFor(widgets = widgets)) }
        }
    }
}
