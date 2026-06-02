package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.RemoveBookFromLibraryUseCase
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.event.RefreshDetailBookEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnRemoveBookClickActionTest {

    private lateinit var removeBookFromLibraryUseCase: RemoveBookFromLibraryUseCase
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<BookDetailLocalVariables>
    private lateinit var eventChannel: Channel<BookDetailEvent>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        removeBookFromLibraryUseCase = mockk()
        stateFlow = MutableStateFlow(BookDetailUiState())
        localVariablesFlow = MutableStateFlow(BookDetailLocalVariables())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = localVariablesFlow,
            eventChannel = eventChannel,
        )
    }

    private fun stubDependencies(testScope: kotlinx.coroutines.test.TestScope): BookDetailDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.removeBookFromLibraryUseCase
            } returns removeBookFromLibraryUseCase

            every {
                mock.coroutineScope
            } returns testScope

            every {
                mock.mainDispatcher
            } returns dispatcher

            every {
                mock.launch(any())
            } answers { callOriginal() }
        }
    }

    private fun stubBook(id: Int = 42): Book = mockk<Book>().also { mock ->
        every { mock.id } returns id
    }

    @Nested
    inner class Execute {

        @Test
        fun `invokes use case with the book provided to the action constructor`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            dependencies = stubDependencies(this)

            coEvery {
                removeBookFromLibraryUseCase(book = book)
            } returns Result.success(Unit)

            val action = OnRemoveBookClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                removeBookFromLibraryUseCase(book = book)
            }
        }

        @Test
        fun `sends RefreshDetailBookEvent when use case succeeds`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            dependencies = stubDependencies(this)

            coEvery {
                removeBookFromLibraryUseCase(book = book)
            } returns Result.success(Unit)

            val action = OnRemoveBookClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val event = eventChannel.tryReceive().getOrNull()
            (event is RefreshDetailBookEvent) shouldBe true
        }

        @Test
        fun `does not send any event when use case fails`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            dependencies = stubDependencies(this)

            coEvery {
                removeBookFromLibraryUseCase(book = book)
            } returns Result.failure(RuntimeException("removal error"))

            val action = OnRemoveBookClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val event = eventChannel.tryReceive().getOrNull()
            event shouldBe null
        }

        @Test
        fun `does not add book id to failedMutationBookIds when use case succeeds`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            dependencies = stubDependencies(this)

            coEvery {
                removeBookFromLibraryUseCase(book = book)
            } returns Result.success(Unit)

            val action = OnRemoveBookClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.failedMutationBookIds.contains(42) shouldBe false
        }

        @Test
        fun `adds book id to failedMutationBookIds when use case fails`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            dependencies = stubDependencies(this)

            coEvery {
                removeBookFromLibraryUseCase(book = book)
            } returns Result.failure(RuntimeException("api error"))

            val action = OnRemoveBookClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.failedMutationBookIds.contains(42) shouldBe true
        }

        @Test
        fun `stores job in bookMutationJobs after execute returns`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            dependencies = stubDependencies(this)

            coEvery {
                removeBookFromLibraryUseCase(book = book)
            } returns Result.success(Unit)

            val action = OnRemoveBookClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.bookMutationJobs.containsKey(42) shouldBe true
        }

        @Test
        fun `cancels prior job for same book id and replaces it with a new one`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val book = stubBook(id = bookId)
            val priorJob = Job()
            localVariablesFlow.value = BookDetailLocalVariables(
                bookMutationJobs = mapOf(bookId to priorJob),
            )

            scope = ActionScope(
                stateFlow = stateFlow,
                localVariablesFlow = localVariablesFlow,
                eventChannel = eventChannel,
            )

            dependencies = stubDependencies(this)

            coEvery {
                removeBookFromLibraryUseCase(book = book)
            } returns Result.success(Unit)

            val action = OnRemoveBookClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            priorJob.isCancelled shouldBe true
            localVariablesFlow.value.bookMutationJobs.containsKey(bookId) shouldBe true
        }

        @Test
        fun `completes without throwing when use case fails`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            dependencies = stubDependencies(this)

            coEvery {
                removeBookFromLibraryUseCase(book = book)
            } returns Result.failure(RuntimeException("network failure"))

            val action = OnRemoveBookClickAction(book = book)

            // ----- Act & Assert -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )
        }
    }
}
