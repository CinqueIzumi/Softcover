package nl.rhaydus.softcover.feature.reading.presentation.action

import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.softcover.feature.reading.presentation.viewmodel.ReadingScreenDependencies
import timber.log.Timber

data object RefreshAction : ReadingAction {
    override suspend fun execute(
        dependencies: ReadingScreenDependencies,
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
    ) {
        scope.setState {
            it.copy(isLoading = true)
        }

        dependencies.refreshUserBooksUseCase().onFailure {
            Timber.e("-=- Something went wrong refreshing currently reading books! $it")
        }

        scope.setState {
            it.copy(isLoading = false)
        }
    }
}