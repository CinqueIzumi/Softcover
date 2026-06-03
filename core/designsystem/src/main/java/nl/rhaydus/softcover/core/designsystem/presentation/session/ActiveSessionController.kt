package nl.rhaydus.softcover.core.designsystem.presentation.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.core.book.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RecordBookProgressUseCase
import nl.rhaydus.softcover.core.domain.logging.AppLog
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.personal.domain.usecase.ObserveActiveSessionUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.PauseReadingSessionUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.ResumeReadingSessionUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.StartReadingSessionUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.StopReadingSessionUseCase

/**
 * App-scoped single source of truth for the active reading session. It exposes the running session
 * (paired with its book) as a [StateFlow] consumed by the peek bar and Focus Mode, and owns every
 * session-control action so all surfaces stay in lockstep. The lock-screen surface is rendered by
 * the foreground service behind [readingSessionLauncher], which observes [activeSession]; [start]
 * launches it.
 */
class ActiveSessionController(
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
) {
    val activeSession: StateFlow<ActiveSession?> =
        combine(
            observeActiveSessionUseCase(),
            getCurrentlyReadingBooksUseCase(),
        ) { session, books ->
            session ?: return@combine null

            books.firstOrNull { it.id == session.bookId }
                ?.let { book -> ActiveSession(
                    session = session,
                    book = book,
                ) }
        }.stateIn(
            scope = applicationScope.scope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    private val _pendingFocusMode = MutableStateFlow(false)

    /**
     * Set when a lock-screen tap asks to open Focus Mode. Held as a consumable flag (not a one-shot
     * event) so a cold start — where the notification launches the app before the root is collecting
     * — still routes into Focus Mode once the navigator is ready.
     */
    val pendingFocusMode: StateFlow<Boolean> = _pendingFocusMode.asStateFlow()

    fun start(book: Book) {
        applicationScope.scope.launch(appDispatchers.io) {
            runCatching {
                startReadingSessionUseCase(
                    bookId = book.id,
                    startPage = book.userBookRead?.currentPage,
                    startSeconds = book.userBookRead?.currentSeconds,
                )
            }.onSuccess {
                readingSessionLauncher.start()
            }.onFailure { error ->
                AppLog.e("$error")
            }
        }
    }

    fun pause() {
        val id = activeSession.value?.session?.id ?: return

        applicationScope.scope.launch(appDispatchers.io) {
            pauseReadingSessionUseCase(id = id)
        }
    }

    fun resume() {
        val id = activeSession.value?.session?.id ?: return

        applicationScope.scope.launch(appDispatchers.io) {
            resumeReadingSessionUseCase(id = id)
        }
    }

    // endPage/endSeconds are read from the latest emitted snapshot. A page write made microseconds
    // earlier may not have propagated through the room flow yet, so a stop immediately after an
    // update can record a one-revision-stale end page — an acceptable window for a reading timer.
    fun stop() {
        val current = activeSession.value ?: return

        applicationScope.scope.launch(appDispatchers.io) {
            stopReadingSessionUseCase(
                id = current.session.id,
                endPage = current.book.userBookRead?.currentPage,
                endSeconds = current.book.userBookRead?.currentSeconds,
            )
        }
    }

    fun updatePage(newPage: Int) {
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

    fun requestFocusMode() {
        _pendingFocusMode.value = true
    }

    fun consumeFocusModeRequest() {
        _pendingFocusMode.value = false
    }
}
