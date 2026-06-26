package nl.rhaydus.softcover.feature.reading.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.error.onApiFailure
import nl.rhaydus.softcover.core.domain.model.RefreshScope
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

internal data object RefreshAction : ReadingAction {
    override suspend fun execute(
        dependencies: ReadingScreenDependencies,
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
    ) {
        scope.setState {
            it.copy(isLoading = true)
        }

        dependencies
            .refreshLibraryUseCase(
                scope = RefreshScope.ByStatus(status = UserBookStatus.CURRENTLY_READING),
            )
            .onApiFailure(logContext = "Something went wrong refreshing currently reading books!")

        scope.setState {
            it.copy(isLoading = false)
        }
    }
}
