package nl.rhaydus.softcover.feature.settings.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.mapper.dateStyleRowsFor
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.toad.ActionScope
import kotlin.time.Clock

internal class DateStyleCollector : SettingsCollector {
    override suspend fun onLaunch(
        scope: ActionScope<SettingsScreenUiState, SettingsScreenEvent, SettingsLocalVariables>,
        dependencies: SettingsScreenDependencies,
    ) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val examples = DateStyle.entries.associateWith { it.format(today) }

        dependencies.getDateStyleAsFlowUseCase().collectLatest { style ->
            scope.setState {
                it.copy(
                    dateStyleRows = dateStyleRowsFor(
                        examples = examples,
                        selected = style,
                    ),
                )
            }
        }
    }
}
