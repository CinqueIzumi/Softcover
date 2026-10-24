package nl.rhaydus.softcover.core.progresssheet.presentation

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.toad.ActionDependencies
import nl.rhaydus.toad.ActionScope

internal typealias HostScope = ActionScope<TestState, TestEvent, TestLocalVariables>

internal class RecordingHost : ProgressSheetHost<TestState, TestEvent, ActionDependencies, TestLocalVariables> {
    data class Recorded(
        val book: Book,
        val entry: ProgressEntry,
    )

    data class MarkedRead(
        val book: Book,
        val actionAt: String?,
    )

    val records = mutableListOf<Recorded>()
    val markedRead = mutableListOf<MarkedRead>()
    val calls = mutableListOf<String>()
    val sheetAtAwait = mutableListOf<ProgressSheetState>()
    val sheetAtAfterClose = mutableListOf<ProgressSheetState>()

    override fun bookOf(state: TestState): Book? = state.book

    override fun sheetOf(state: TestState): ProgressSheetState = state.progressSheet

    override fun withSheet(
        state: TestState,
        sheet: ProgressSheetState,
    ): TestState = state.copy(progressSheet = sheet)

    override suspend fun record(
        book: Book,
        entry: ProgressEntry,
        dependencies: ActionDependencies,
        scope: HostScope,
    ) {
        records += Recorded(
            book = book,
            entry = entry,
        )
    }

    override suspend fun markAsRead(
        book: Book,
        actionAt: String?,
        dependencies: ActionDependencies,
        scope: HostScope,
    ) {
        markedRead += MarkedRead(
            book = book,
            actionAt = actionAt,
        )
    }

    override suspend fun awaitPendingWrite(
        dependencies: ActionDependencies,
        scope: HostScope,
    ) {
        calls += "awaitPendingWrite"
        sheetAtAwait += scope.currentState.progressSheet
    }

    override suspend fun afterClose(
        dependencies: ActionDependencies,
        scope: HostScope,
    ) {
        calls += "afterClose"
        sheetAtAfterClose += scope.currentState.progressSheet
    }
}
