package nl.rhaydus.softcover.feature.book_detail.presentation.flows

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.books.domain.usecase.GetAllUserBooksUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserBooksFlowCollectorTest {

    private lateinit var getAllUserBooksUseCase: GetAllUserBooksUseCase
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>
    private lateinit var userBooksFlow: MutableSharedFlow<List<Book>>

    @BeforeEach
    fun setUp() {
        userBooksFlow = MutableSharedFlow()
        getAllUserBooksUseCase = mockk()
        stateFlow = MutableStateFlow(BookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getAllUserBooksUseCase()
        } returns userBooksFlow

        dependencies = mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.getAllUserBooksUseCase
            } returns getAllUserBooksUseCase
        }
    }

    private fun stubBook(id: Int): Book = mockk<Book>().also { mock ->
        every { mock.id } returns id
    }

    @Nested
    inner class OnLaunch {

        @Test
        fun `updates book in state when emitted list contains a book with matching id`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val initialBook = stubBook(id = 7)
            val updatedBook = stubBook(id = 7)
            stateFlow.value = BookDetailUiState(book = initialBook)
            val collector = UserBooksFlowCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            userBooksFlow.emit(listOf(updatedBook))

            // ----- Assert -----
            stateFlow.value.book shouldBe updatedBook
            job.cancel()
        }

        @Test
        fun `does not update book when emitted list does not contain a book with matching id`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val currentBook = stubBook(id = 1)
            val unrelatedBook = stubBook(id = 99)
            stateFlow.value = BookDetailUiState(book = currentBook)
            val collector = UserBooksFlowCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            userBooksFlow.emit(listOf(unrelatedBook))

            // ----- Assert -----
            stateFlow.value.book shouldBe currentBook
            job.cancel()
        }

        @Test
        fun `does not update book when emitted list is empty`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val currentBook = stubBook(id = 5)
            stateFlow.value = BookDetailUiState(book = currentBook)
            val collector = UserBooksFlowCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            userBooksFlow.emit(emptyList())

            // ----- Assert -----
            stateFlow.value.book shouldBe currentBook
            job.cancel()
        }

        @Test
        fun `does not update book when current book in state is null`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val emittedBook = stubBook(id = 3)
            stateFlow.value = BookDetailUiState(book = null)
            val collector = UserBooksFlowCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            userBooksFlow.emit(listOf(emittedBook))

            // ----- Assert -----
            stateFlow.value.book shouldBe null
            job.cancel()
        }

        @Test
        fun `updates book to the latest matching entry on multiple emissions`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val initialBook = stubBook(id = 10)
            val secondBook = stubBook(id = 10)
            val thirdBook = stubBook(id = 10)
            stateFlow.value = BookDetailUiState(book = initialBook)
            val collector = UserBooksFlowCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            userBooksFlow.emit(listOf(secondBook))
            userBooksFlow.emit(listOf(thirdBook))

            // ----- Assert -----
            stateFlow.value.book shouldBe thirdBook
            job.cancel()
        }

        @Test
        fun `picks the correct book from a list containing multiple books`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val currentBook = stubBook(id = 2)
            val otherBook = stubBook(id = 50)
            val updatedCurrentBook = stubBook(id = 2)
            stateFlow.value = BookDetailUiState(book = currentBook)
            val collector = UserBooksFlowCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            userBooksFlow.emit(listOf(otherBook, updatedCurrentBook))

            // ----- Assert -----
            stateFlow.value.book shouldBe updatedCurrentBook
            job.cancel()
        }

        @Test
        fun `picks the first book when multiple books in list share the matching id`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val currentBook = stubBook(id = 8)
            val firstDuplicate = stubBook(id = 8)
            val secondDuplicate = stubBook(id = 8)
            stateFlow.value = BookDetailUiState(book = currentBook)
            val collector = UserBooksFlowCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            userBooksFlow.emit(listOf(firstDuplicate, secondDuplicate))

            // ----- Assert -----
            stateFlow.value.book shouldBe firstDuplicate
            job.cancel()
        }

        @Test
        fun `preserves all other state fields when updating book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val initialBook = stubBook(id = 4)
            val updatedBook = stubBook(id = 4)
            stateFlow.value = BookDetailUiState(
                book = initialBook,
                loadingBookDetails = false,
                fabMenuExpanded = true,
                showEditEditionSheet = true,
            )
            val collector = UserBooksFlowCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            userBooksFlow.emit(listOf(updatedBook))

            // ----- Assert -----
            stateFlow.value.loadingBookDetails shouldBe false
            stateFlow.value.fabMenuExpanded shouldBe true
            stateFlow.value.showEditEditionSheet shouldBe true
            stateFlow.value.book shouldBe updatedBook
            job.cancel()
        }
    }
}
