package nl.rhaydus.softcover.feature.reading.presentation.flows

import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.domain.logging.AppLog
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState

internal class TrendingBooksLoader : ReadingInitializer {
    override suspend fun onLaunch(
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
        dependencies: ReadingScreenDependencies,
    ) {
        dependencies.getTrendingBooksUseCase()
            .onSuccess { books ->
                scope.setState { it.copy(trendingBooks = books) }
            }
            .onFailure {
                AppLog.e("$it")
            }
    }
}
