package nl.rhaydus.softcover.core.progresssheet.presentation

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.toad.ActionDependencies
import nl.rhaydus.toad.ActionScope
import nl.rhaydus.toad.LocalVariables
import nl.rhaydus.toad.UiEvent
import nl.rhaydus.toad.UiState

interface ProgressSheetHost<S : UiState, E : UiEvent, D : ActionDependencies, V : LocalVariables> {
    fun bookOf(state: S): Book?

    fun sheetOf(state: S): ProgressSheetState

    fun withSheet(
        state: S,
        sheet: ProgressSheetState,
    ): S

    suspend fun record(
        book: Book,
        entry: ProgressEntry,
        dependencies: D,
        scope: ActionScope<S, E, V>,
    )

    suspend fun markAsRead(
        book: Book,
        actionAt: String?,
        dependencies: D,
        scope: ActionScope<S, E, V>,
    )

    suspend fun awaitPendingWrite(
        dependencies: D,
        scope: ActionScope<S, E, V>,
    )

    suspend fun afterClose(
        dependencies: D,
        scope: ActionScope<S, E, V>,
    )
}
