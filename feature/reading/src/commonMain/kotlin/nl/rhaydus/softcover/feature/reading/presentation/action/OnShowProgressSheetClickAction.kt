package nl.rhaydus.softcover.feature.reading.presentation.action

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

internal data class OnShowProgressSheetClickAction(val book: Book) : ReadingAction {
    override suspend fun execute(
        dependencies: ReadingScreenDependencies,
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
    ) {
        // The active unit is kept current by LastUsedProgressUnitCollector, so opening the sheet just
        // reveals it — no one-shot preference read here (a suspending read on open could fail/stall).
        scope.setState {
            it.copy(
                bookToUpdate = book,
                showProgressSheet = true,
            )
        }
    }
}
