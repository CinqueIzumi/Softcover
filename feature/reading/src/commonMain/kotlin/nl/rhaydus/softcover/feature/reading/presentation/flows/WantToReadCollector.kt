package nl.rhaydus.softcover.feature.reading.presentation.flows

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

internal class WantToReadCollector : ReadingInitializer {
    override suspend fun onLaunch(
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
        dependencies: ReadingScreenDependencies,
    ) {
        dependencies.getWantToReadUserBooksUseCase().collectLatest { books ->
            scope.setState { it.copy(wantToReadBooks = books) }
        }
    }
}
