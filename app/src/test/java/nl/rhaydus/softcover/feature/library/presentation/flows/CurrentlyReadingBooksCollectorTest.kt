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
import nl.rhaydus.softcover.feature.books.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CurrentlyReadingBooksCollectorTest {

    private lateinit var getCurrentlyReadingUserBooksUseCase: GetCurrentlyReadingUserBooksUseCase
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>
    private lateinit var booksFlow: MutableSharedFlow<List<Book>>

    @BeforeEach
    fun setUp() {
        booksFlow = MutableSharedFlow()
        getCurrentlyReadingUserBooksUseCase = mockk()
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getCurrentlyReadingUserBooksUseCase()
        } returns booksFlow

        dependencies = mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.getCurrentlyReadingUserBooksUseCase
            } returns getCurrentlyReadingUserBooksUseCase
        }
    }

    @Nested
    inner class OnLaunch {

        @Test
        fun `updates currentlyReadingBooks and clears isLoading when flow emits`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val books = listOf(mockk<Book>(), mockk<Book>())
            val collector = CurrentlyReadingBooksCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            booksFlow.emit(books)

            // ----- Assert -----
            stateFlow.value.currentlyReadingBooks shouldBe books
            stateFlow.value.isLoading shouldBe false
            job.cancel()
        }

        @Test
        fun `updates currentlyReadingBooks to the latest emission when flow emits multiple times`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val firstBooks = listOf(mockk<Book>())
            val secondBooks = listOf(mockk<Book>(), mockk<Book>())
            val collector = CurrentlyReadingBooksCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            booksFlow.emit(firstBooks)
            booksFlow.emit(secondBooks)

            // ----- Assert -----
            stateFlow.value.currentlyReadingBooks shouldBe secondBooks
            job.cancel()
        }

        @Test
        fun `updates currentlyReadingBooks to empty list when flow emits empty list`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = CurrentlyReadingBooksCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            booksFlow.emit(emptyList())

            // ----- Assert -----
            stateFlow.value.currentlyReadingBooks shouldBe emptyList()
            stateFlow.value.isLoading shouldBe false
            job.cancel()
        }

        @Test
        fun `does not change currentlyReadingBooks before the flow emits`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = CurrentlyReadingBooksCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act & Assert -----
            stateFlow.value.currentlyReadingBooks shouldBe null
            job.cancel()
        }

        @Test
        fun `preserves other state fields when updating currentlyReadingBooks`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val existingReadBooks = listOf(mockk<Book>())
            stateFlow.value = LibraryUiState(readBooks = existingReadBooks, isLoading = true)
            val books = listOf(mockk<Book>())
            val collector = CurrentlyReadingBooksCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            booksFlow.emit(books)

            // ----- Assert -----
            stateFlow.value.readBooks shouldBe existingReadBooks
            stateFlow.value.currentlyReadingBooks shouldBe books
            job.cancel()
        }
    }
}
