package nl.rhaydus.softcover.feature.reading.presentation.collector

import kotlinx.coroutines.flow.first
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

internal class ReadingReadinessCollector : ReadingCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
        dependencies: ReadingScreenDependencies,
    ) {
        scope.state.first { it.isFirstLoadReady() }

        scope.setState { it.copy(isLoading = false) }
    }
}

private fun ReadingScreenUiState.isFirstLoadReady(): Boolean {
    if (hasLoadedBooks.not() || hasLoadedReadingActivity.not()) return false

    if (books.isEmpty()) return true

    return featuredCover != null &&
        featuredBackdropCover != null &&
        featuredProgress.isLoading.not() &&
        alsoReading.cards.none { it.isLoading } &&
        masthead != null &&
        compactMasthead != null
}
