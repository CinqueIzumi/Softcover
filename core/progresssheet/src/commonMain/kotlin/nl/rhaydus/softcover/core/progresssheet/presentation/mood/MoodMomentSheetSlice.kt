package nl.rhaydus.softcover.core.progresssheet.presentation.mood

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import nl.rhaydus.common.AppLog
import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.component.feeling.MoodMomentSheetEvent
import nl.rhaydus.softcover.core.component.sheet.openOrUpdate
import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.core.corpus.domain.usecase.AddMoodMomentUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.DeleteMoodMomentUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveMoodMomentsUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveOwnMoodWordsUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.UpdateMoodMomentUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetAskForMoodAfterProgressAsFlowUseCase
import nl.rhaydus.toad.ActionDependencies
import nl.rhaydus.toad.ActionScope
import nl.rhaydus.toad.LocalVariables
import nl.rhaydus.toad.UiEvent
import nl.rhaydus.toad.UiState

class MoodMomentSheetSlice internal constructor(
    private val getAskForMoodAfterProgressAsFlowUseCase: GetAskForMoodAfterProgressAsFlowUseCase,
    private val observeMoodMomentsUseCase: ObserveMoodMomentsUseCase,
    private val observeOwnMoodWordsUseCase: ObserveOwnMoodWordsUseCase,
    private val addMoodMomentUseCase: AddMoodMomentUseCase,
    private val updateMoodMomentUseCase: UpdateMoodMomentUseCase,
    private val deleteMoodMomentUseCase: DeleteMoodMomentUseCase,
) {
    suspend fun <S : UiState, E : UiEvent, V : LocalVariables> collect(
        host: MoodMomentSheetHost<S>,
        scope: ActionScope<S, E, V>,
    ) {
        combine(
            scope.state.map { host.momentSheetOf(it).draft }.distinctUntilChanged(),
            observeOwnMoodWordsUseCase(),
        ) { _, ownWords -> ownWords }
            .collectLatest { ownWords ->
                scope.setState { state ->
                    val sheet = host.momentSheetOf(state)
                    val draft = sheet.draft

                    host.withMomentSheet(
                        state = state,
                        sheet = sheet.copy(
                            session = if (draft == null) {
                                sheet.session.requestClose()
                            } else {
                                sheet.session.openOrUpdate(model = draft.toMoodMomentSheet(ownWords = ownWords))
                            },
                        ),
                    )
                }
            }
    }

    suspend fun <S : UiState, E : UiEvent, V : LocalVariables> openAfterProgress(
        pending: PendingMoodMoment,
        host: MoodMomentSheetHost<S>,
        scope: ActionScope<S, E, V>,
    ) {
        val askForMood = runCatchingCancellable {
            getAskForMoodAfterProgressAsFlowUseCase().firstOrNull()
        }.getOrNull() ?: true

        if (askForMood.not()) return

        setDraft(
            host = host,
            scope = scope,
            draft = MoodMomentDraft.adding(pending),
        )
    }

    suspend fun <S : UiState, E : UiEvent, V : LocalVariables> openToEdit(
        bookId: Int,
        momentId: Long,
        host: MoodMomentSheetHost<S>,
        scope: ActionScope<S, E, V>,
    ) {
        val moments = runCatchingCancellable {
            observeMoodMomentsUseCase(bookId = bookId).firstOrNull()
        }.getOrNull() ?: return

        val moment = moments.firstOrNull { it.id == momentId } ?: return

        setDraft(
            host = host,
            scope = scope,
            draft = MoodMomentDraft.editing(moment),
        )
    }

    suspend fun <S : UiState, E : UiEvent, D : ActionDependencies, V : LocalVariables> onEvent(
        event: MoodMomentSheetEvent,
        host: MoodMomentSheetHost<S>,
        dependencies: D,
        scope: ActionScope<S, E, V>,
    ) {
        val draft = host.momentSheetOf(scope.currentState).draft

        when (event) {
            is MoodMomentSheetEvent.ChipClicked -> draft?.let {
                setDraft(
                    host,
                    scope,
                    it.withChipClicked(key = event.key),
                )
            }

            is MoodMomentSheetEvent.OwnWordChanged -> draft?.let {
                setDraft(
                    host,
                    scope,
                    it.withOwnWordEvent(event = event.event),
                )
            }

            is MoodMomentSheetEvent.StrengthChanged -> draft?.let {
                setDraft(
                    host,
                    scope,
                    it.withStrength(value = event.strength),
                )
            }

            MoodMomentSheetEvent.SaveRequested -> draft?.let {
                setDraft(
                    host,
                    scope,
                    null,
                )

                save(
                    draft = it,
                    dependencies = dependencies,
                )
            }

            MoodMomentSheetEvent.SecondaryRequested -> draft?.let {
                setDraft(
                    host,
                    scope,
                    null,
                )

                it.editing?.let { moment ->
                    dependencies.launch {
                        deleteMoodMomentUseCase(id = moment.id).onFailure { error -> reportFailure(error = error) }
                    }
                }
            }

            MoodMomentSheetEvent.Dismissed -> scope.setState { state ->
                host.withMomentSheet(
                    state = state,
                    sheet = MoodMomentSheetState(),
                )
            }
        }
    }

    private fun <S : UiState, E : UiEvent, V : LocalVariables> setDraft(
        host: MoodMomentSheetHost<S>,
        scope: ActionScope<S, E, V>,
        draft: MoodMomentDraft?,
    ) {
        scope.setState { state ->
            host.withMomentSheet(
                state = state,
                sheet = host.momentSheetOf(state).copy(draft = draft),
            )
        }
    }

    private fun <D : ActionDependencies> save(
        draft: MoodMomentDraft,
        dependencies: D,
    ) {
        val word = draft.committedWord ?: return

        dependencies.launch {
            val result = draft.editing?.let { moment ->
                updateMoodMomentUseCase(
                    moment.copy(
                        word = word,
                        strength = draft.strength,
                    ),
                )
            } ?: addMoodMomentUseCase(
                bookId = draft.bookId,
                word = word,
                strength = draft.strength,
                percent = draft.percent,
                page = draft.page,
                seconds = draft.seconds,
            )

            result.onFailure { reportFailure(error = it) }
        }
    }

    private fun reportFailure(error: Throwable) {
        AppLog.e("Failed to save the mood moment: $error")

        SnackBarManager.showSnackbar(title = "Couldn't save that mood. Try again.")
    }
}
