package nl.rhaydus.softcover.feature.library.presentation.flows

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
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.books.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookListsCollectorTest {

    private lateinit var getAllUserListsUseCase: GetAllUserListsUseCase
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>
    private lateinit var listsFlow: MutableSharedFlow<List<BookList>>

    @BeforeEach
    fun setUp() {
        listsFlow = MutableSharedFlow()
        getAllUserListsUseCase = mockk()
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getAllUserListsUseCase()
        } returns listsFlow

        dependencies = mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.getAllUserListsUseCase
            } returns getAllUserListsUseCase
        }
    }

    private fun stubListBook(edition: BookEdition): ListBook = mockk<ListBook>().also { listBook ->
        every { listBook.edition } returns edition
    }

    private fun stubBookList(slug: String, books: List<ListBook>): BookList = mockk<BookList>().also { bookList ->
        every { bookList.slug } returns slug
        every { bookList.books } returns books
    }

    @Nested
    inner class OnLaunch {

        @Test
        fun `updates ownedEditions with editions from the owned list when flow emits`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val edition1 = mockk<BookEdition>()
            val edition2 = mockk<BookEdition>()
            val ownedListBook1 = stubListBook(edition1)
            val ownedListBook2 = stubListBook(edition2)
            val ownedList = stubBookList(slug = "owned", books = listOf(ownedListBook1, ownedListBook2))
            val collector = BookListsCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            listsFlow.emit(listOf(ownedList))

            // ----- Assert -----
            stateFlow.value.ownedEditions shouldBe listOf(edition1, edition2)
            job.cancel()
        }

        @Test
        fun `does not update ownedEditions when the owned list is absent`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val otherList = stubBookList(slug = "reading", books = listOf(stubListBook(mockk())))
            val collector = BookListsCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            listsFlow.emit(listOf(otherList))

            // ----- Assert -----
            stateFlow.value.ownedEditions shouldBe null
            job.cancel()
        }

        @Test
        fun `does not update ownedEditions when the emitted list is empty`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = BookListsCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            listsFlow.emit(emptyList())

            // ----- Assert -----
            stateFlow.value.ownedEditions shouldBe null
            job.cancel()
        }

        @Test
        fun `sets ownedEditions to empty list when the owned list exists but has no books`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val ownedList = stubBookList(slug = "owned", books = emptyList())
            val collector = BookListsCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            listsFlow.emit(listOf(ownedList))

            // ----- Assert -----
            stateFlow.value.ownedEditions shouldBe emptyList()
            job.cancel()
        }

        @Test
        fun `uses the first owned list found when multiple lists share the owned slug`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val edition1 = mockk<BookEdition>()
            val edition2 = mockk<BookEdition>()
            val firstOwnedList = stubBookList(slug = "owned", books = listOf(stubListBook(edition1)))
            val secondOwnedList = stubBookList(slug = "owned", books = listOf(stubListBook(edition2)))
            val collector = BookListsCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            listsFlow.emit(listOf(firstOwnedList, secondOwnedList))

            // ----- Assert -----
            stateFlow.value.ownedEditions shouldBe listOf(edition1)
            job.cancel()
        }

        @Test
        fun `updates ownedEditions to the latest owned list on subsequent emissions`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val firstEdition = mockk<BookEdition>()
            val secondEdition = mockk<BookEdition>()
            val firstOwnedList = stubBookList(slug = "owned", books = listOf(stubListBook(firstEdition)))
            val secondOwnedList = stubBookList(slug = "owned", books = listOf(stubListBook(secondEdition)))
            val collector = BookListsCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            listsFlow.emit(listOf(firstOwnedList))
            listsFlow.emit(listOf(secondOwnedList))

            // ----- Assert -----
            stateFlow.value.ownedEditions shouldBe listOf(secondEdition)
            job.cancel()
        }

        @Test
        fun `does not change ownedEditions before the flow emits`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = BookListsCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act & Assert -----
            stateFlow.value.ownedEditions shouldBe null
            job.cancel()
        }

        @Test
        fun `preserves other state fields when updating ownedEditions`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val existingBooks = listOf(mockk<Book>())
            stateFlow.value = LibraryUiState(allBooks = existingBooks)
            val edition = mockk<BookEdition>()
            val ownedList = stubBookList(slug = "owned", books = listOf(stubListBook(edition)))
            val collector = BookListsCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            listsFlow.emit(listOf(ownedList))

            // ----- Assert -----
            stateFlow.value.allBooks shouldBe existingBooks
            stateFlow.value.ownedEditions shouldBe listOf(edition)
            job.cancel()
        }
    }
}
