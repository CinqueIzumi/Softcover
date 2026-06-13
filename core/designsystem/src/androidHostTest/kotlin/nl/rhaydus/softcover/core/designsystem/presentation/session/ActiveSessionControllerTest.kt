package nl.rhaydus.softcover.core.designsystem.presentation.session

import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.book.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RecordBookProgressUseCase
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ReadingSession
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.personal.domain.usecase.ObserveActiveSessionUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.PauseReadingSessionUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.ResumeReadingSessionUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.StartReadingSessionUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.StopReadingSessionUseCase

class ActiveSessionControllerTest {
    private val sessionFlow = MutableStateFlow<ReadingSession?>(null)
    private val booksFlow = MutableStateFlow<List<Book>>(emptyList())

    private lateinit var observeActiveSessionUseCase: ObserveActiveSessionUseCase
    private lateinit var getCurrentlyReadingBooksUseCase: GetCurrentlyReadingUserBooksUseCase
    private lateinit var startReadingSessionUseCase: StartReadingSessionUseCase
    private lateinit var stopReadingSessionUseCase: StopReadingSessionUseCase
    private lateinit var pauseReadingSessionUseCase: PauseReadingSessionUseCase
    private lateinit var resumeReadingSessionUseCase: ResumeReadingSessionUseCase
    private lateinit var recordBookProgressUseCase: RecordBookProgressUseCase

    private fun buildController(testScope: TestScope): ActiveSessionController {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)

        // backgroundScope is cancelled automatically when runTest exits, so the eager
        // stateIn collector does not leak past the test body.  We replace its inherited
        // StandardTestDispatcher with the UnconfinedTestDispatcher so combine emissions
        // are processed synchronously when upstream MutableStateFlows are mutated.
        val appScope = CoroutineScope(testScope.backgroundScope.coroutineContext + dispatcher)

        return ActiveSessionController(
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
            readingSessionLauncher = mockk(relaxed = true),
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
        every { book.id } returns id

        val userBookRead = mockk<UserBookRead> {
            every { this@mockk.currentPage } returns currentPage
            every { this@mockk.currentSeconds } returns currentSeconds
        }

        every { book.userBookRead } returns userBookRead
    }

    @BeforeEach
    fun setUp() {
        observeActiveSessionUseCase = mockk()
        getCurrentlyReadingBooksUseCase = mockk()

        every { observeActiveSessionUseCase() } returns sessionFlow
        every { getCurrentlyReadingBooksUseCase() } returns booksFlow

        startReadingSessionUseCase = mockk(relaxed = true)
        stopReadingSessionUseCase = mockk(relaxed = true)
        pauseReadingSessionUseCase = mockk(relaxed = true)
        resumeReadingSessionUseCase = mockk(relaxed = true)
        recordBookProgressUseCase = mockk(relaxed = true)
    }

    @Nested
    inner class ActiveSessionFlow {
        @Test
        fun `emits null when session flow emits null`() = runTest {
            // ----- Arrange -----
            val controller = buildController(this)

            // ----- Act -----
            sessionFlow.value = null

            // ----- Assert -----
            controller.activeSession.value shouldBe null
        }

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

            active shouldBe ActiveSession(
                session = session,
                book = book,
            )
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

        @Test
        fun `does not invoke resumeReadingSessionUseCase when no session is active`() = runTest {
            // ----- Arrange -----
            val controller = buildController(this)

            // ----- Act -----
            controller.resume()

            // ----- Assert -----
            coVerify(exactly = 0) { resumeReadingSessionUseCase(any()) }
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
            coVerify(exactly = 0) { stopReadingSessionUseCase(
                any(),
                any(),
                any(),
            ) }
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
            coVerify(exactly = 1) { recordBookProgressUseCase(
                book = book,
                newPage = 200,
            ) }
        }

        @Test
        fun `does not invoke recordBookProgressUseCase when no session is active`() = runTest {
            // ----- Arrange -----
            val controller = buildController(this)

            // ----- Act -----
            controller.updatePage(newPage = 50)

            // ----- Assert -----
            coVerify(exactly = 0) { recordBookProgressUseCase(
                any(),
                any(),
            ) }
        }
    }

    @Nested
    inner class FocusMode {
        @Test
        fun `pendingFocusMode starts as false`() = runTest {
            // ----- Arrange & Act -----
            val controller = buildController(this)

            // ----- Assert -----
            controller.pendingFocusMode.value shouldBe false
        }

        @Test
        fun `requestFocusMode sets pendingFocusMode to true`() = runTest {
            // ----- Arrange -----
            val controller = buildController(this)

            // ----- Act -----
            controller.requestFocusMode()

            // ----- Assert -----
            controller.pendingFocusMode.value shouldBe true
        }

        @Test
        fun `consumeFocusModeRequest sets pendingFocusMode back to false`() = runTest {
            // ----- Arrange -----
            val controller = buildController(this)
            controller.requestFocusMode()

            // ----- Act -----
            controller.consumeFocusModeRequest()

            // ----- Assert -----
            controller.pendingFocusMode.value shouldBe false
        }
    }
}
