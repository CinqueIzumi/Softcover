package nl.rhaydus.softcover.feature.reading.presentation.action

import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.softcover.feature.reading.presentation.viewmodel.ReadingScreenDependencies

data class OnUpdatePageProgressClickAction(val newPage: String) : ReadingAction {
    override suspend fun execute(
        dependencies: ReadingScreenDependencies,
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent>,
    ) {
        val bookToUpdate: BookWithProgress = scope.currentState.bookToUpdate ?: return

        val newPageValue = newPage.toIntOrNull() ?: 0

        dependencies.updateBookProgressUtil(
            book = bookToUpdate,
            newPage = newPageValue,
            scope = scope,
            dependencies = dependencies
        )
    }
}