package nl.rhaydus.softcover.orchestration.session

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RecordBookProgressUseCase
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.transition.BookTransitionElement
import nl.rhaydus.softcover.core.component.transition.bookTransitionKey
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ReadingSession
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.personal.domain.usecase.ObserveActiveSessionUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.PauseReadingSessionUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.ResumeReadingSessionUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.StartReadingSessionUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.StopReadingSessionUseCase
import nl.rhaydus.softcover.core.presentation.session.ActiveSessionController
import nl.rhaydus.softcover.core.presentation.session.ReadingSessionLauncher
import nl.rhaydus.softcover.core.presentation.session.SessionOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Instant

class ActiveSessionControllerImplTest {
    private val sessionFlow = MutableStateFlow<ReadingSession?>(null)
    private val booksFlow = MutableStateFlow<List<Book>>(emptyList())

    private lateinit var observeActiveSessionUseCase: ObserveActiveSessionUseCase
    private lateinit var getCurrentlyReadingBooksUseCase: GetCurrentlyReadingUserBooksUseCase
    private lateinit var startReadingSessionUseCase: StartReadingSessionUseCase
    private lateinit var stopReadingSessionUseCase: StopReadingSessionUseCase
    private lateinit var pauseReadingSessionUseCase: PauseReadingSessionUseCase
    private lateinit var resumeReadingSessionUseCase: ResumeReadingSessionUseCase
    private lateinit var recordBookProgressUseCase: RecordBookProgressUseCase

    private fun buildController(
        testScope: TestScope,
        readingSessionLauncher: ReadingSessionLauncher = mockk(relaxed = true),
    ): ActiveSessionController {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)

        // backgroundScope is cancelled automatically when runTest exits, so the eager
        // stateIn collector does not leak past the test body.  We replace its inherited
        // StandardTestDispatcher with the UnconfinedTestDispatcher so combine emissions
        // are processed synchronously when upstream MutableStateFlows are mutated.
        val appScope = CoroutineScope(testScope.backgroundScope.coroutineContext + dispatcher)

        return ActiveSessionControllerImpl(
            observeActiveSessionUseCase = observeActiveSessionUseCase,
            getCurrentlyReadingBooksUseCase = getCurrentlyReadingBooksUseCase,
            startReadingSessionUseCase = startReadingSessionUseCase,
            stopReadingSessionUseCase = stopReadingSessionUseCase,
            pauseReadingSessionUseCase = pauseReadingSessionUseCase,
            resumeReadingSessionUseCase = resumeReadingSessionUseCase,
            recordBookProgressUseCase = recordBookProgressUseCase,
            applicationScope = ApplicationScope(appScope),
            appDispatchers = AppDispatchers(
                main = dispatcher,
                io = dispatcher,
                default = dispatcher,
            ),
            readingSessionLauncher = readingSessionLauncher,
        )
    }

    private fun stubSession(
        id: Long = 1L,
        bookId: Int = 42,
    ): ReadingSession = ReadingSession(
        id = id,
        bookId = bookId,
        startedAt = Instant.fromEpochMilliseconds(0),
        endedAt = null,
        startPage = null,
        endPage = null,
        startSeconds = null,
        endSeconds = null,
    )

    private fun stubBook(
        id: Int = 42,
        currentPage: Int? = 100,
        currentSeconds: Int? = null,
    ): Book = mockk<Book>().also { book ->
        every {
            book.id
        } returns id
        every {
            book.title
        } returns "Stub Book"
        every {
            book.authors
        } returns emptyList()
        every {
            book.currentEdition
        } returns null
        every {
            book.defaultEdition
        } returns null
        every {
            book.coverUrl
        } returns ""

        val userBookRead = mockk<UserBookRead> {
            every {
                this@mockk.currentPage
            } returns currentPage
            every {
                this@mockk.currentSeconds
            } returns currentSeconds
        }

        every {
            book.userBookRead
        } returns userBookRead
    }

    @BeforeEach
    fun setUp() {
        observeActiveSessionUseCase = mockk()
        getCurrentlyReadingBooksUseCase = mockk()

        every {
            observeActiveSessionUseCase()
        } returns sessionFlow
        every {
            getCurrentlyReadingBooksUseCase()
        } returns booksFlow

        startReadingSessionUseCase = mockk(relaxed = true)
        stopReadingSessionUseCase = mockk(relaxed = true)
        pauseReadingSessionUseCase = mockk(relaxed = true)
        resumeReadingSessionUseCase = mockk(relaxed = true)
        recordBookProgressUseCase = mockk(relaxed = true)
    }

    @Nested
    inner class ActiveSessionFlow {
        @Test
        fun `emits null when session bookId is not found in the books list`() = runTest {
            // ----- Arrange -----
            val controller = buildController(this)
            val book = stubBook(id = 99)

            // ----- Act -----
            booksFlow.value = listOf(book)
            sessionFlow.value = stubSession(bookId = 42)

            // ----- Assert -----
            controller.activeSession.value shouldBe null
        }

        @Test
        fun `emits an ActiveSession pairing session and book when bookId matches`() = runTest {
            // ----- Arrange -----
            val controller = buildController(this)
            val book = stubBook(id = 42)
            val session = stubSession(bookId = 42)

            // ----- Act -----
            booksFlow.value = listOf(book)
            sessionFlow.value = session

            // ----- Assert -----
            val active = controller.activeSession.value

            active?.session shouldBe session
            active?.book shouldBe book
            active?.ui?.peekBarCover?.variant shouldBe CoverVariant.Flat
            active?.ui?.focusCover?.variant shouldBe CoverVariant.Spotlight
        }

        @Test
        fun `emits null when session is cleared after having been active`() = runTest {
            // ----- Arrange -----
            val controller = buildController(this)
            val book = stubBook(id = 42)

            booksFlow.value = listOf(book)
            sessionFlow.value = stubSession(bookId = 42)

            // ----- Act -----
            sessionFlow.value = null

            // ----- Assert -----
            controller.activeSession.value shouldBe null
        }

        @Test
        fun `peek bar and focus covers share one non-null transition key so the cover morphs`() = runTest {
            // ----- Arrange -----
            val controller = buildController(this)

            // ----- Act -----
            booksFlow.value = listOf(stubBook(id = 42))
            sessionFlow.value = stubSession(bookId = 42)

            // ----- Assert -----
            val active = controller.activeSession.value.shouldNotBeNull()
            val peekKey = active.ui.peekBarCover.sharedTransitionKey.shouldNotBeNull()

            peekKey shouldBe active.ui.focusCover.sharedTransitionKey
        }

        @Test
        fun `session cover key differs from the un-surfaced key of the same book`() = runTest {
            // ----- Arrange -----
            val controller = buildController(this)
            val unsurfacedKey = bookTransitionKey(
                element = BookTransitionElement.Cover,
                bookId = 42,
            )

            // ----- Act -----
            booksFlow.value = listOf(stubBook(id = 42))
            sessionFlow.value = stubSession(bookId = 42)

            // ----- Assert -----
            val active = controller.activeSession.value.shouldNotBeNull()

            active.ui.peekBarCover.sharedTransitionKey shouldNotBe unsurfacedKey
            active.ui.focusCover.sharedTransitionKey shouldNotBe unsurfacedKey
        }
    }

    @Nested
    inner class Pause {
        @Test
        fun `invokes pauseReadingSessionUseCase with the active session id`() = runTest {
            // ----- Arrange -----
            val controller = buildController(this)
            val session = stubSession(
                id = 7L,
                bookId = 42,
            )

            booksFlow.value = listOf(stubBook(id = 42))
            sessionFlow.value = session

            // ----- Act -----
            controller.pause()

            // ----- Assert -----
            coVerify(exactly = 1) { pauseReadingSessionUseCase(id = 7L) }
        }

        @Test
        fun `does not invoke pauseReadingSessionUseCase when no session is active`() = runTest {
            // ----- Arrange -----
            val controller = buildController(this)

            // ----- Act -----
            controller.pause()

            // ----- Assert -----
            coVerify(exactly = 0) { pauseReadingSessionUseCase(any()) }
        }
    }

    @Nested
    inner class Resume {
        @Test
        fun `invokes resumeReadingSessionUseCase with the active session id`() = runTest {
            // ----- Arrange -----
            val controller = buildController(this)
            val session = stubSession(
                id = 11L,
                bookId = 42,
            )

            booksFlow.value = listOf(stubBook(id = 42))
            sessionFlow.value = session

            // ----- Act -----
            controller.resume()

            // ----- Assert -----
            coVerify(exactly = 1) { resumeReadingSessionUseCase(id = 11L) }
        }
    }

    @Nested
    inner class Stop {
        @Test
        fun `invokes stopReadingSessionUseCase with id and book progress`() = runTest {
            // ----- Arrange -----
            val controller = buildController(this)
            val book = stubBook(
                id = 42,
                currentPage = 150,
                currentSeconds = null,
            )
            val session = stubSession(
                id = 3L,
                bookId = 42,
            )

            booksFlow.value = listOf(book)
            sessionFlow.value = session

            // ----- Act -----
            controller.stop()

            // ----- Assert -----
            coVerify(exactly = 1) {
                stopReadingSessionUseCase(
                    id = 3L,
                    endPage = 150,
                    endSeconds = null,
                )
            }
        }

        @Test
        fun `does not invoke stopReadingSessionUseCase when no session is active`() = runTest {
            // ----- Arrange -----
            val controller = buildController(this)

            // ----- Act -----
            controller.stop()

            // ----- Assert -----
            coVerify(exactly = 0) {
                stopReadingSessionUseCase(
                    any(),
                    any(),
                    any(),
                )
            }
        }
    }

    @Nested
    inner class UpdatePage {
        @Test
        fun `invokes recordBookProgressUseCase with the active book and newPage`() = runTest {
            // ----- Arrange -----
            val controller = buildController(this)
            val book = stubBook(id = 42)
            val session = stubSession(bookId = 42)

            booksFlow.value = listOf(book)
            sessionFlow.value = session

            // ----- Act -----
            controller.updatePage(newPage = 200)

            // ----- Assert -----
            coVerify(exactly = 1) {
                recordBookProgressUseCase(
                    book = book,
                    newPage = 200,
                )
            }
        }

        @Test
        fun `does not invoke recordBookProgressUseCase when no session is active`() = runTest {
            // ----- Arrange -----
            val controller = buildController(this)

            // ----- Act -----
            controller.updatePage(newPage = 50)

            // ----- Assert -----
            coVerify(exactly = 0) {
                recordBookProgressUseCase(
                    any(),
                    any(),
                )
            }
        }
    }

    @Nested
    inner class FocusMode {
        @Test
        fun `a requested focus mode is pending until it is consumed`() = runTest {
            // ----- Arrange -----
            val controller = buildController(this)

            // ----- Act -----
            controller.requestFocusMode()
            val pendingAfterRequest = controller.pendingFocusMode.value
            controller.consumeFocusModeRequest()

            // ----- Assert -----
            pendingAfterRequest shouldBe true
            controller.pendingFocusMode.value shouldBe false
        }
    }

    @Nested
    inner class Outcomes {
        private fun collectOutcomes(
            testScope: TestScope,
            controller: ActiveSessionController,
        ): List<SessionOutcome> {
            val emitted = mutableListOf<SessionOutcome>()

            testScope.backgroundScope.launch(UnconfinedTestDispatcher(testScope.testScheduler)) {
                controller.outcomes.collect { emitted.add(it) }
            }

            return emitted
        }

        @Test
        fun `start emits Started when the use case succeeds`() = runTest {
            // ----- Arrange -----
            val controller = buildController(this)
            val emitted = collectOutcomes(
                testScope = this,
                controller = controller,
            )

            coEvery {
                startReadingSessionUseCase(
                    any(),
                    any(),
                    any(),
                )
            } returns 1L

            // ----- Act -----
            controller.start(stubBook())

            // ----- Assert -----
            emitted.shouldContainExactly(SessionOutcome.Started)
        }

        @Test
        fun `start emits StartFailed when the use case throws`() = runTest {
            // ----- Arrange -----
            val controller = buildController(this)
            val emitted = collectOutcomes(
                testScope = this,
                controller = controller,
            )

            coEvery {
                startReadingSessionUseCase(
                    any(),
                    any(),
                    any(),
                )
            } throws IllegalStateException("boom")

            // ----- Act -----
            controller.start(stubBook())

            // ----- Assert -----
            emitted.shouldContainExactly(SessionOutcome.StartFailed)
        }

        @Test
        fun `start emits StartFailed and not Started when the launcher throws`() = runTest {
            // ----- Arrange -----
            val launcher = mockk<ReadingSessionLauncher>()
            val controller = buildController(
                testScope = this,
                readingSessionLauncher = launcher,
            )
            val emitted = collectOutcomes(
                testScope = this,
                controller = controller,
            )

            coEvery {
                startReadingSessionUseCase(
                    any(),
                    any(),
                    any(),
                )
            } returns 1L
            every {
                launcher.start()
            } throws IllegalStateException("boom")

            // ----- Act -----
            controller.start(stubBook())

            // ----- Assert -----
            emitted.shouldContainExactly(SessionOutcome.StartFailed)
        }

        @Test
        fun `stop emits Stopped when the use case succeeds`() = runTest {
            // ----- Arrange -----
            val controller = buildController(this)
            val emitted = collectOutcomes(
                testScope = this,
                controller = controller,
            )

            booksFlow.value = listOf(stubBook(id = 42))
            sessionFlow.value = stubSession(
                id = 3L,
                bookId = 42,
            )

            // ----- Act -----
            controller.stop()

            // ----- Assert -----
            emitted.shouldContainExactly(SessionOutcome.Stopped)
        }

        @Test
        fun `stop emits StopFailed when the use case throws`() = runTest {
            // ----- Arrange -----
            val controller = buildController(this)
            val emitted = collectOutcomes(
                testScope = this,
                controller = controller,
            )

            booksFlow.value = listOf(stubBook(id = 42))
            sessionFlow.value = stubSession(
                id = 3L,
                bookId = 42,
            )
            coEvery {
                stopReadingSessionUseCase(
                    any(),
                    any(),
                    any(),
                )
            } throws IllegalStateException("boom")

            // ----- Act -----
            controller.stop()

            // ----- Assert -----
            emitted.shouldContainExactly(SessionOutcome.StopFailed)
        }

        @Test
        fun `stop emits nothing when no session is active`() = runTest {
            // ----- Arrange -----
            val controller = buildController(this)
            val emitted = collectOutcomes(
                testScope = this,
                controller = controller,
            )

            // ----- Act -----
            controller.stop()

            // ----- Assert -----
            emitted.shouldContainExactly()
        }
    }
}
