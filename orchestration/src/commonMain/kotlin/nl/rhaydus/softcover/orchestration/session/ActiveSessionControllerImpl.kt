package nl.rhaydus.softcover.orchestration.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.book.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RecordBookProgressUseCase
import nl.rhaydus.softcover.core.designsystem.presentation.session.ActiveSession
import nl.rhaydus.softcover.core.designsystem.presentation.session.ActiveSessionController
import nl.rhaydus.softcover.core.designsystem.presentation.session.ReadingSessionLauncher
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.personal.domain.usecase.ObserveActiveSessionUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.PauseReadingSessionUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.ResumeReadingSessionUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.StartReadingSessionUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.StopReadingSessionUseCase

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
    override val activeSession: StateFlow<ActiveSession?> =
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
                    )
                }
        }.stateIn(
            scope = applicationScope.scope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    private val _pendingFocusMode = MutableStateFlow(false)

    override val pendingFocusMode: StateFlow<Boolean> = _pendingFocusMode.asStateFlow()

    override fun start(book: Book) {
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
            stopReadingSessionUseCase(
                id = current.session.id,
                endPage = current.book.userBookRead?.currentPage,
                endSeconds = current.book.userBookRead?.currentSeconds,
            )
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

    override fun requestFocusMode() {
        _pendingFocusMode.value = true
    }

    override fun consumeFocusModeRequest() {
        _pendingFocusMode.value = false
    }
}
