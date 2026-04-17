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
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.books.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AllBooksCollectorTest {

    private lateinit var getAllUserBooksUseCase: GetAllUserBooksUseCase
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>
    private lateinit var booksFlow: MutableSharedFlow<List<Book>>

    @BeforeEach
    fun setUp() {
        booksFlow = MutableSharedFlow()
        getAllUserBooksUseCase = mockk()
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getAllUserBooksUseCase()
        } returns booksFlow

        dependencies = mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.getAllUserBooksUseCase
            } returns getAllUserBooksUseCase
        }
    }

    @Nested
    inner class OnLaunch {

        @Test
        fun `updates allBooks and clears isLoading when flow emits`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val books = listOf(mockk<Book>(), mockk<Book>())
            val collector = AllBooksCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            booksFlow.emit(books)

            // ----- Assert -----
            stateFlow.value.allBooks shouldBe books
            stateFlow.value.isLoading shouldBe false
            job.cancel()
        }

        @Test
        fun `updates allBooks to the latest emission when flow emits multiple times`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val firstBooks = listOf(mockk<Book>())
            val secondBooks = listOf(mockk<Book>(), mockk<Book>())
            val collector = AllBooksCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            booksFlow.emit(firstBooks)
            booksFlow.emit(secondBooks)

            // ----- Assert -----
            stateFlow.value.allBooks shouldBe secondBooks
            job.cancel()
        }

        @Test
        fun `updates allBooks to empty list when flow emits empty list`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = AllBooksCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            booksFlow.emit(emptyList())

            // ----- Assert -----
            stateFlow.value.allBooks shouldBe emptyList()
            stateFlow.value.isLoading shouldBe false
            job.cancel()
        }

        @Test
        fun `does not change allBooks before the flow emits`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = AllBooksCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act & Assert -----
            stateFlow.value.allBooks shouldBe null
            job.cancel()
        }

        @Test
        fun `preserves other state fields when updating allBooks`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val existingWantToRead = listOf(mockk<Book>())
            stateFlow.value = LibraryUiState(wantToReadBooks = existingWantToRead, isLoading = true)
            val books = listOf(mockk<Book>())
            val collector = AllBooksCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            booksFlow.emit(books)

            // ----- Assert -----
            stateFlow.value.wantToReadBooks shouldBe existingWantToRead
            stateFlow.value.allBooks shouldBe books
            job.cancel()
        }
    }
}
