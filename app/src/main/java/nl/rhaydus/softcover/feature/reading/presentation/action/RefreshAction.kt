package nl.rhaydus.softcover.feature.reading.presentation.action

import nl.rhaydus.softcover.core.domain.model.RefreshScope
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import timber.log.Timber

data object RefreshAction : ReadingAction {
    override suspend fun execute(
        dependencies: ReadingScreenDependencies,
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
    ) {
        scope.setState {
            it.copy(isLoading = true)
        }

        dependencies
            .refreshUserBooksUseCase(
                scope = RefreshScope.ByStatus(status = UserBookStatus.CURRENTLY_READING),
            )
            .onFailure {
                Timber.e("-=- Something went wrong refreshing currently reading books! $it")
            }

        scope.setState {
            it.copy(isLoading = false)
        }
    }
}