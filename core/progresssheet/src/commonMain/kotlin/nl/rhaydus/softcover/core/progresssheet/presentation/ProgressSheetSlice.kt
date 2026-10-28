package nl.rhaydus.softcover.core.progresssheet.presentation

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import nl.rhaydus.common.currentLocalDateTime
import nl.rhaydus.softcover.core.component.progress.ProgressSheetEvent
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.openOrUpdate
import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetLastUsedProgressUnitAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetLastUsedProgressUnitUseCase
import nl.rhaydus.softcover.core.uibinding.progress.ProgressTimeDraft
import nl.rhaydus.softcover.core.uibinding.progress.progressCurrentSeconds
import nl.rhaydus.softcover.core.uibinding.progress.shiftedBy
import nl.rhaydus.softcover.core.uibinding.progress.toActionAt
import nl.rhaydus.softcover.core.uibinding.progress.toProgressSheetTab
import nl.rhaydus.softcover.core.uibinding.progress.toProgressSheetUiModel
import nl.rhaydus.softcover.core.uibinding.progress.toProgressUnit
import nl.rhaydus.toad.ActionDependencies
import nl.rhaydus.toad.ActionScope
import nl.rhaydus.toad.LocalVariables
import nl.rhaydus.toad.UiEvent
import nl.rhaydus.toad.UiState

class ProgressSheetSlice internal constructor(
    private val getLastUsedProgressUnitAsFlowUseCase: GetLastUsedProgressUnitAsFlowUseCase,
    private val setLastUsedProgressUnitUseCase: SetLastUsedProgressUnitUseCase,
) {
    suspend fun <S : UiState, E : UiEvent, D : ActionDependencies, V : LocalVariables> collect(
        host: ProgressSheetHost<S, E, D, V>,
        scope: ActionScope<S, E, V>,
    ) {
        coroutineScope {
            launch {
                collectLastUsedUnit(
                    host = host,
                    scope = scope,
                )
            }

            launch {
                collectSheetModel(
                    host = host,
                    scope = scope,
                )
            }
        }
    }

    suspend fun <S : UiState, E : UiEvent, D : ActionDependencies, V : LocalVariables> onEvent(
        event: ProgressSheetEvent,
        host: ProgressSheetHost<S, E, D, V>,
        dependencies: D,
        scope: ActionScope<S, E, V>,
    ) {
        when (event) {
            is ProgressSheetEvent.TabSelected -> {
                updateSheet(host, scope) { it.copy(tab = event.tab) }

                dependencies.launch {
                    setLastUsedProgressUnitUseCase(unit = event.tab.toProgressUnit())
                }
            }

            is ProgressSheetEvent.TimeChanged -> editTimeDraft(host, scope) { draft ->
                draft.withText(
                    unit = event.unit,
                    text = event.text,
                )
            }

            is ProgressSheetEvent.MinutesStepped -> editTimeDraft(host, scope) { draft ->
                draft.steppedMinutes(delta = event.delta)
            }

            is ProgressSheetEvent.WhenReadShifted -> {
                val now = currentLocalDateTime()

                updateSheet(host, scope) {
                    it.copy(
                        readAt = it.readAt.shiftedBy(
                            minutes = event.minutes,
                            now = now,
                        ),
                    )
                }
            }

            ProgressSheetEvent.WhenReadReset -> updateSheet(host, scope) { it.copy(readAt = null) }

            is ProgressSheetEvent.PagesSubmitted -> submit(host, dependencies, scope) { _, actionAt ->
                pageEntry(
                    page = event.page,
                    actionAt = actionAt,
                )
            }

            is ProgressSheetEvent.PercentageSubmitted -> submit(host, dependencies, scope) { book, actionAt ->
                percentageEntry(
                    book = book,
                    percentage = event.percentage,
                    actionAt = actionAt,
                )
            }

            is ProgressSheetEvent.TimeSubmitted -> submit(host, dependencies, scope) { book, actionAt ->
                timeEntry(
                    book = book,
                    hours = event.hours,
                    minutes = event.minutes,
                    seconds = event.seconds,
                    actionAt = actionAt,
                )
            }

            ProgressSheetEvent.MarkAsReadRequested -> {
                val book = host.bookOf(scope.currentState) ?: return

                host.markAsRead(
                    book = book,
                    actionAt = host.sheetOf(scope.currentState).readAt.toActionAt(),
                    dependencies = dependencies,
                    scope = scope,
                )
            }

            ProgressSheetEvent.Dismissed -> {
                host.awaitPendingWrite(
                    dependencies = dependencies,
                    scope = scope,
                )

                updateSheet(host, scope) {
                    it.copy(
                        requested = false,
                        session = SheetSession.Closed,
                        timeDraft = null,
                    )
                }

                host.afterClose(
                    dependencies = dependencies,
                    scope = scope,
                )
            }
        }
    }

    private suspend fun <S : UiState, E : UiEvent, D : ActionDependencies, V : LocalVariables> collectLastUsedUnit(
        host: ProgressSheetHost<S, E, D, V>,
        scope: ActionScope<S, E, V>,
    ) {
        getLastUsedProgressUnitAsFlowUseCase().collectLatest { unit ->
            updateSheet(host, scope) { it.copy(tab = unit.toProgressSheetTab()) }
        }
    }

    private suspend fun <S : UiState, E : UiEvent, D : ActionDependencies, V : LocalVariables> collectSheetModel(
        host: ProgressSheetHost<S, E, D, V>,
        scope: ActionScope<S, E, V>,
    ) {
        scope.state
            .map { state ->
                val sheet = host.sheetOf(state)

                ProgressSheetSnapshot(
                    book = host.bookOf(state).takeIf { sheet.requested },
                    tab = sheet.tab,
                    timeDraft = sheet.timeDraft,
                    readAt = sheet.readAt,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val book = snapshot.book

                scope.setState { state ->
                    val sheet = host.sheetOf(state)

                    val session = if (book == null || sheet.requested.not() || host.bookOf(state) == null) {
                        sheet.session.requestClose()
                    } else {
                        sheet.session.openOrUpdate(
                            model = book.toProgressSheetUiModel(
                                selectedTab = snapshot.tab,
                                timeDraft = snapshot.timeDraft,
                                readAt = snapshot.readAt,
                            ),
                        )
                    }

                    host.withSheet(
                        state = state,
                        sheet = sheet.copy(session = session),
                    )
                }
            }
    }

    private fun <S : UiState, E : UiEvent, D : ActionDependencies, V : LocalVariables> updateSheet(
        host: ProgressSheetHost<S, E, D, V>,
        scope: ActionScope<S, E, V>,
        transform: (ProgressSheetState) -> ProgressSheetState,
    ) {
        scope.setState { state ->
            host.withSheet(
                state = state,
                sheet = transform(host.sheetOf(state)),
            )
        }
    }

    private fun <S : UiState, E : UiEvent, D : ActionDependencies, V : LocalVariables> editTimeDraft(
        host: ProgressSheetHost<S, E, D, V>,
        scope: ActionScope<S, E, V>,
        edit: (ProgressTimeDraft) -> ProgressTimeDraft,
    ) {
        val book = host.bookOf(scope.currentState) ?: return

        updateSheet(host, scope) { sheet ->
            sheet.copy(
                timeDraft = edit(
                    ProgressTimeDraft.resolve(
                        stored = sheet.timeDraft,
                        currentSeconds = book.progressCurrentSeconds,
                    ),
                ),
            )
        }
    }

    private suspend fun <S : UiState, E : UiEvent, D : ActionDependencies, V : LocalVariables> submit(
        host: ProgressSheetHost<S, E, D, V>,
        dependencies: D,
        scope: ActionScope<S, E, V>,
        entryOf: (book: Book, actionAt: String?) -> ProgressEntry?,
    ) {
        val book = host.bookOf(scope.currentState) ?: return

        val actionAt = host.sheetOf(scope.currentState).readAt.toActionAt()

        val entry = entryOf(
            book,
            actionAt,
        ) ?: return

        host.record(
            book = book,
            entry = entry,
            dependencies = dependencies,
            scope = scope,
        )
    }
}
