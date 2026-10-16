package nl.rhaydus.softcover.feature.widget.progress

import kotlinx.coroutines.flow.collectLatest
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
