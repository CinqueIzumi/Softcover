package nl.rhaydus.softcover.core.presentation.removal

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.book.domain.model.StagedBookRemoval
import nl.rhaydus.softcover.core.book.domain.usecase.CommitBookRemovalUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.StageBookRemovalUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.UndoBookRemovalUseCase
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.domain.model.Book
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UndoableBookRemoverTest {
    private lateinit var stageBookRemovalUseCase: StageBookRemovalUseCase
    private lateinit var undoBookRemovalUseCase: UndoBookRemovalUseCase
    private lateinit var commitBookRemovalUseCase: CommitBookRemovalUseCase
    private lateinit var hostState: SnackbarHostState
    private lateinit var windowResult: CompletableDeferred<SnackbarResult>
    private lateinit var shownMessage: CapturingSlot<String>
    private lateinit var testScope: TestScope
    private lateinit var remover: UndoableBookRemover

    @BeforeEach
    fun setUp() {
        stageBookRemovalUseCase = mockk()
        undoBookRemovalUseCase = mockk(relaxed = true)
        commitBookRemovalUseCase = mockk()
        hostState = mockk()
        windowResult = CompletableDeferred()
        shownMessage = slot()

        coEvery {
            hostState.showSnackbar(
                message = capture(shownMessage),
                actionLabel = any(),
                withDismissAction = any(),
                duration = any(),
            )
        } coAnswers { windowResult.await() }

        mockkObject(SnackBarManager)

        every {
            SnackBarManager.snackBarState
        } returns MutableStateFlow(hostState)
        every {
            SnackBarManager.showSnackbar(
                title = any(),
                duration = any(),
            )
        } just Runs

        val dispatcher = UnconfinedTestDispatcher()
        testScope = TestScope(dispatcher)

        remover = UndoableBookRemover(
            stageBookRemovalUseCase = stageBookRemovalUseCase,
            undoBookRemovalUseCase = undoBookRemovalUseCase,
            commitBookRemovalUseCase = commitBookRemovalUseCase,
            applicationScope = ApplicationScope(scope = testScope),
            appDispatchers = AppDispatchers(
                main = dispatcher,
                io = dispatcher,
                default = dispatcher,
            ),
        )
    }

    @AfterEach
    fun tearDown() {
        testScope.cancel()
        unmockkObject(SnackBarManager)
        clearAllMocks()
    }

    private fun buildBook(id: Int): Book = mockk<Book>().also { book ->
        every {
            book.title
        } returns "Book $id"
    }

    private fun buildBooks(count: Int): List<Book> = (1..count).map { id -> buildBook(id = id) }

    private fun stagedOf(books: List<Book>) = StagedBookRemoval(
        books = books,
        snapshots = books,
    )

    @Nested
    inner class Remove {
        @Test
        fun `empty input succeeds without staging`() = runTest {
            // ----- Arrange -----
            // (no books)

            // ----- Act -----
            val result = remover.remove(books = emptyList())

            // ----- Assert -----
            result.isSuccess.shouldBeTrue()
            coVerify(exactly = 0) { stageBookRemovalUseCase(books = any()) }
        }

        @Test
        fun `a staging failure returns failure and shows the failure snackbar`() = runTest {
            // ----- Arrange -----
            val books = buildBooks(count = 1)

            coEvery {
                stageBookRemovalUseCase(books = books)
            } returns Result.failure(RuntimeException("stage failed"))

            // ----- Act -----
            val result = remover.remove(books = books)

            // ----- Assert -----
            result.isFailure.shouldBeTrue()
            verify(exactly = 1) {
                SnackBarManager.showSnackbar(
                    title = "Couldn't remove 1 book — try again.",
                    duration = any(),
                )
            }
        }

        @Test
        fun `a staging failure for several books pluralises the failure snackbar`() = runTest {
            // ----- Arrange -----
            val books = buildBooks(count = 2)

            coEvery {
                stageBookRemovalUseCase(books = books)
            } returns Result.failure(RuntimeException("stage failed"))

            // ----- Act -----
            remover.remove(books = books)

            // ----- Assert -----
            verify(exactly = 1) {
                SnackBarManager.showSnackbar(
                    title = "Couldn't remove 2 books — try again.",
                    duration = any(),
                )
            }
        }

        @Test
        fun `nothing staged opens no undo window`() = runTest {
            // ----- Arrange -----
            val books = buildBooks(count = 1)
            val emptyStaged = stagedOf(books = emptyList())

            coEvery {
                stageBookRemovalUseCase(books = books)
            } returns Result.success(emptyStaged)

            // ----- Act -----
            val result = remover.remove(books = books)

            // ----- Assert -----
            result.isSuccess.shouldBeTrue()
            coVerify(exactly = 0) {
                hostState.showSnackbar(
                    message = any(),
                    actionLabel = any(),
                    withDismissAction = any(),
                    duration = any(),
                )
            }
        }

        @Test
        fun `a single staged book shows the Removed title message`() = runTest {
            // ----- Arrange -----
            val book = buildBook(id = 7)
            val books = listOf(book)
            val staged = stagedOf(books = books)

            coEvery {
                stageBookRemovalUseCase(books = books)
            } returns Result.success(staged)

            // ----- Act -----
            remover.remove(books = books)

            // ----- Assert -----
            shownMessage.captured shouldBe "Removed Book 7"
        }

        @Test
        fun `several staged books show the Removed count message`() = runTest {
            // ----- Arrange -----
            val books = buildBooks(count = 3)
            val staged = stagedOf(books = books)

            coEvery {
                stageBookRemovalUseCase(books = books)
            } returns Result.success(staged)

            // ----- Act -----
            remover.remove(books = books)

            // ----- Assert -----
            shownMessage.captured shouldBe "Removed 3 books"
        }

        @Test
        fun `the message reflects the staged books, not the requested ones`() = runTest {
            // ----- Arrange -----
            val requested = buildBooks(count = 2)
            val stagedPart = stagedOf(books = listOf(requested.last()))

            coEvery {
                stageBookRemovalUseCase(books = requested)
            } returns Result.success(stagedPart)

            // ----- Act -----
            remover.remove(books = requested)

            // ----- Assert -----
            shownMessage.captured shouldBe "Removed Book 2"
        }

        @Test
        fun `the window stays open without committing or undoing until the snackbar resolves`() = runTest {
            // ----- Arrange -----
            val books = buildBooks(count = 1)
            val staged = stagedOf(books = books)

            coEvery {
                stageBookRemovalUseCase(books = books)
            } returns Result.success(staged)

            // ----- Act -----
            remover.remove(books = books)

            // ----- Assert -----
            windowResult.isCompleted.shouldBeFalse()
            coVerify(exactly = 0) { undoBookRemovalUseCase(staged = any()) }
            coVerify(exactly = 0) { commitBookRemovalUseCase(staged = any()) }
        }

        @Test
        fun `Undo restores the staged removal and never commits`() = runTest {
            // ----- Arrange -----
            val books = buildBooks(count = 1)
            val staged = stagedOf(books = books)

            coEvery {
                stageBookRemovalUseCase(books = books)
            } returns Result.success(staged)

            // ----- Act -----
            remover.remove(books = books)
            windowResult.complete(SnackbarResult.ActionPerformed)

            // ----- Assert -----
            coVerify(exactly = 1) { undoBookRemovalUseCase(staged = staged) }
            coVerify(exactly = 0) { commitBookRemovalUseCase(staged = any()) }
        }

        @Test
        fun `Dismissed commits the staged removal and never undoes`() = runTest {
            // ----- Arrange -----
            val books = buildBooks(count = 1)
            val staged = stagedOf(books = books)

            coEvery {
                stageBookRemovalUseCase(books = books)
            } returns Result.success(staged)
            coEvery {
                commitBookRemovalUseCase(staged = staged)
            } returns Result.success(emptyList())

            // ----- Act -----
            remover.remove(books = books)
            windowResult.complete(SnackbarResult.Dismissed)

            // ----- Assert -----
            coVerify(exactly = 1) { commitBookRemovalUseCase(staged = staged) }
            coVerify(exactly = 0) { undoBookRemovalUseCase(staged = any()) }
            verify(exactly = 0) {
                SnackBarManager.showSnackbar(
                    title = any(),
                    duration = any(),
                )
            }
        }

        @Test
        fun `a commit that fails for some books shows the failure snackbar for those books`() = runTest {
            // ----- Arrange -----
            val books = buildBooks(count = 3)
            val staged = stagedOf(books = books)

            coEvery {
                stageBookRemovalUseCase(books = books)
            } returns Result.success(staged)
            coEvery {
                commitBookRemovalUseCase(staged = staged)
            } returns Result.success(books.take(2))

            // ----- Act -----
            remover.remove(books = books)
            windowResult.complete(SnackbarResult.Dismissed)

            // ----- Assert -----
            verify(exactly = 1) {
                SnackBarManager.showSnackbar(
                    title = "Couldn't remove 2 books — try again.",
                    duration = any(),
                )
            }
        }

        @Test
        fun `a commit failing for one book uses the singular copy`() = runTest {
            // ----- Arrange -----
            val books = buildBooks(count = 2)
            val staged = stagedOf(books = books)

            coEvery {
                stageBookRemovalUseCase(books = books)
            } returns Result.success(staged)
            coEvery {
                commitBookRemovalUseCase(staged = staged)
            } returns Result.success(books.take(1))

            // ----- Act -----
            remover.remove(books = books)
            windowResult.complete(SnackbarResult.Dismissed)

            // ----- Assert -----
            verify(exactly = 1) {
                SnackBarManager.showSnackbar(
                    title = "Couldn't remove 1 book — try again.",
                    duration = any(),
                )
            }
        }
    }
}
