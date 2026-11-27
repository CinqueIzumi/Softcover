package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.component.feeling.MoodMomentSheetEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailMoodMomentSheetHost
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal data class OnMoodMomentSheetEventAction(val event: MoodMomentSheetEvent) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        dependencies.moodMomentSheetSlice.onEvent(
            event = event,
            host = BookDetailMoodMomentSheetHost,
            dependencies = dependencies,
            scope = scope,
        )
    }
}
