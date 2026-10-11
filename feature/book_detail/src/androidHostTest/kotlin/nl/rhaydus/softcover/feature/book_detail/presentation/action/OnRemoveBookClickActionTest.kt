package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.removal.UndoableBookRemover
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLens
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnRemoveBookClickActionTest {
    private lateinit var undoableBookRemover: UndoableBookRemover
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<BookDetailLocalVariables>
    private lateinit var eventChannel: Channel<BookDetailEvent>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        undoableBookRemover = mockk()
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        localVariablesFlow = MutableStateFlow(BookDetailLocalVariables())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = localVariablesFlow,
            eventChannel = eventChannel,
        )
        dependencies = mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.undoableBookRemover
            } returns undoableBookRemover
        }
    }

    private fun buildBook(id: Int): Book = Book(
        id = id,
        canonicalId = null,
        title = "Book $id",
        editions = persistentListOf(),
        defaultEdition = null,
        rating = 0.0,
        description = "",
        releaseYear = 2020,
        releaseDate = null,
        coverUrl = "",
        authors = emptyList(),
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = null,
        positionsInSeries = emptyList(),
        isCompilation = false,
        userBook = mockk(),
        userBookRead = mockk(),
    )

    @Nested
    inner class Execute {
        @Test
        fun `stages the removal of the book through the undoable remover`() = runTest {
            // ----- Arrange -----
            val book = buildBook(id = 42)

            coEvery {
                undoableBookRemover.remove(books = listOf(book))
            } returns Result.success(Unit)

            val action = OnRemoveBookClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) { undoableBookRemover.remove(books = listOf(book)) }
        }

        @Test
        fun `on success switches to THE_BOOK lens and clears the user book and read`() = runTest {
            // ----- Arrange -----
            val book = buildBook(id = 42)
            stateFlow.value = buildBookDetailUiState().copy(
                selectedLens = BookDetailLens.YOURS,
                book = book,
            )

            coEvery {
                undoableBookRemover.remove(books = listOf(book))
            } returns Result.success(Unit)

            val action = OnRemoveBookClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectedLens shouldBe BookDetailLens.THE_BOOK

            val updated = stateFlow.value.book.shouldNotBeNull()
            updated.userBook.shouldBeNull()
            updated.userBookRead.shouldBeNull()
            updated.id shouldBe 42
        }

        @Test
        fun `on success leaves the failed mutation ids untouched`() = runTest {
            // ----- Arrange -----
            val book = buildBook(id = 42)

            coEvery {
                undoableBookRemover.remove(books = listOf(book))
            } returns Result.success(Unit)

            val action = OnRemoveBookClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.failedMutationBookIds shouldBe emptySet()
        }

        @Test
        fun `on failure marks the book as failed and keeps lens and user book`() = runTest {
            // ----- Arrange -----
            val book = buildBook(id = 42)
            stateFlow.value = buildBookDetailUiState().copy(
                selectedLens = BookDetailLens.YOURS,
                book = book,
            )

            coEvery {
                undoableBookRemover.remove(books = listOf(book))
            } returns Result.failure(RuntimeException("stage failed"))

            val action = OnRemoveBookClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.failedMutationBookIds shouldContain 42
            stateFlow.value.selectedLens shouldBe BookDetailLens.YOURS
            stateFlow.value.book.shouldNotBeNull().userBook.shouldNotBeNull()
        }

        @Test
        fun `cancels the in-flight mutation job for the same book before removing`() = runTest {
            // ----- Arrange -----
            val book = buildBook(id = 42)
            val priorJob = Job()
            localVariablesFlow.value = BookDetailLocalVariables(
                bookMutationJobs = mapOf(42 to priorJob),
            )

            coEvery {
                undoableBookRemover.remove(books = listOf(book))
            } returns Result.success(Unit)

            val action = OnRemoveBookClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            priorJob.isCancelled shouldBe true
        }

        @Test
        fun `does not cancel the mutation job of a different book`() = runTest {
            // ----- Arrange -----
            val book = buildBook(id = 42)
            val otherJob = Job()
            localVariablesFlow.value = BookDetailLocalVariables(
                bookMutationJobs = mapOf(7 to otherJob),
            )

            coEvery {
                undoableBookRemover.remove(books = listOf(book))
            } returns Result.success(Unit)

            val action = OnRemoveBookClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            otherJob.isCancelled shouldBe false
        }
    }
}
