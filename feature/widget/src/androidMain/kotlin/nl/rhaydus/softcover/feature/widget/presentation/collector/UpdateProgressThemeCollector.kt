package nl.rhaydus.softcover.feature.widget.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.feature.widget.presentation.event.UpdateProgressEvent
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.UpdateProgressDependencies
import nl.rhaydus.softcover.feature.widget.presentation.state.UpdateProgressLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.UpdateProgressUiState
import nl.rhaydus.toad.ActionScope

internal class UpdateProgressThemeCollector : UpdateProgressCollector {
    override suspend fun onLaunch(
        scope: ActionScope<UpdateProgressUiState, UpdateProgressEvent, UpdateProgressLocalVariables>,
        dependencies: UpdateProgressDependencies,
    ) {
        dependencies.getThemeConfiguration().collectLatest { configuration ->
            scope.setState { it.copy(themeConfiguration = configuration) }
        }
    }
}
