package nl.rhaydus.softcover.orchestration.session

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.book.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RecordBookProgressUseCase
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.personal.domain.usecase.ObserveActiveSessionUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.PauseReadingSessionUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.ResumeReadingSessionUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.StartReadingSessionUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.StopReadingSessionUseCase
import nl.rhaydus.softcover.core.presentation.session.ActiveSession
import nl.rhaydus.softcover.core.presentation.session.ActiveSessionController
import nl.rhaydus.softcover.core.presentation.session.ReadingSessionLauncher
import nl.rhaydus.softcover.core.presentation.session.SessionOutcome
import kotlin.coroutines.cancellation.CancellationException

internal class ActiveSessionControllerImpl(
    private val observeActiveSessionUseCase: ObserveActiveSessionUseCase,
    private val getCurrentlyReadingBooksUseCase: GetCurrentlyReadingUserBooksUseCase,
    private val startReadingSessionUseCase: StartReadingSessionUseCase,
    private val stopReadingSessionUseCase: StopReadingSessionUseCase,
    private val pauseReadingSessionUseCase: PauseReadingSessionUseCase,
    private val resumeReadingSessionUseCase: ResumeReadingSessionUseCase,
    private val recordBookProgressUseCase: RecordBookProgressUseCase,
    private val applicationScope: ApplicationScope,
    private val appDispatchers: AppDispatchers,
    private val readingSessionLauncher: ReadingSessionLauncher,
) : ActiveSessionController {
    private val _activeSession = MutableStateFlow<ActiveSession?>(null)

    override val activeSession: StateFlow<ActiveSession?> = _activeSession.asStateFlow()

    private val _sessionLoaded = MutableStateFlow(false)

    override val sessionLoaded: StateFlow<Boolean> = _sessionLoaded.asStateFlow()

    init {
        applicationScope.scope.launch {
            observeSession().collect { session ->
                _activeSession.value = session

                _sessionLoaded.value = true
            }
        }
    }

    private fun observeSession(): Flow<ActiveSession?> =
        combine(
            observeActiveSessionUseCase(),
            getCurrentlyReadingBooksUseCase(),
        ) { session, books ->
            session ?: return@combine null

            books.firstOrNull { it.id == session.bookId }
                ?.let { book ->
                    ActiveSession(
                        session = session,
                        book = book,
                        ui = toActiveSessionUiModel(
                            session = session,
                            book = book,
                        ),
                    )
                }
        }

    private val _outcomes = MutableSharedFlow<SessionOutcome>(extraBufferCapacity = 1)

    override val outcomes: SharedFlow<SessionOutcome> = _outcomes

    override fun start(book: Book) {
        applicationScope.scope.launch(appDispatchers.io) {
            runCatching {
                startReadingSessionUseCase(
                    bookId = book.id,
                    startPage = book.userBookRead?.currentPage,
                    startSeconds = book.userBookRead?.currentSeconds,
                )

                readingSessionLauncher.start()
            }.onSuccess {
                _outcomes.emit(SessionOutcome.Started)
            }.onFailure { error ->
                if (error is CancellationException) throw error

                AppLog.e("$error")

                _outcomes.emit(SessionOutcome.StartFailed)
            }
        }
    }

    override fun pause() {
        val id = activeSession.value?.session?.id ?: return

        applicationScope.scope.launch(appDispatchers.io) {
            pauseReadingSessionUseCase(id = id)
        }
    }

    override fun resume() {
        val id = activeSession.value?.session?.id ?: return

        applicationScope.scope.launch(appDispatchers.io) {
            resumeReadingSessionUseCase(id = id)
        }
    }

    // endPage/endSeconds are read from the latest emitted snapshot. A page write made microseconds
    // earlier may not have propagated through the room flow yet, so a stop immediately after an
    // update can record a one-revision-stale end page — an acceptable window for a reading timer.
    override fun stop() {
        val current = activeSession.value ?: return

        applicationScope.scope.launch(appDispatchers.io) {
            runCatching {
                stopReadingSessionUseCase(
                    id = current.session.id,
                    endPage = current.book.userBookRead?.currentPage,
                    endSeconds = current.book.userBookRead?.currentSeconds,
                )
            }.onSuccess {
                _outcomes.emit(SessionOutcome.Stopped)
            }.onFailure { error ->
                if (error is CancellationException) throw error

                AppLog.e("$error")

                _outcomes.emit(SessionOutcome.StopFailed)
            }
        }
    }

    override fun updatePage(newPage: Int) {
        val book = activeSession.value?.book ?: return

        applicationScope.scope.launch(appDispatchers.io) {
            recordBookProgressUseCase(
                book = book,
                newPage = newPage,
            ).onFailure { error ->
                AppLog.e("$error")
            }
        }
    }
}
