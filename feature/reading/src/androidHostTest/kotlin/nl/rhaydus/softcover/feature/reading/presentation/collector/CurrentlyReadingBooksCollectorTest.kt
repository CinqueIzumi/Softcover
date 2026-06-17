package nl.rhaydus.softcover.feature.reading.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CurrentlyReadingBooksCollectorTest {
    private lateinit var getCurrentlyReadingBooksUseCase: GetCurrentlyReadingUserBooksUseCase
    private lateinit var dependencies: ReadingScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>
    private lateinit var booksFlow: MutableSharedFlow<List<Book>>

    @BeforeEach
    fun setUp() {
        booksFlow = MutableSharedFlow()
        getCurrentlyReadingBooksUseCase = mockk()
        stateFlow = MutableStateFlow(ReadingScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ReadingLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getCurrentlyReadingBooksUseCase()
        } returns booksFlow

        dependencies = mockk<ReadingScreenDependencies>(relaxed = true).also { mock ->
            every {
                mock.getCurrentlyReadingBooksUseCase
            } returns getCurrentlyReadingBooksUseCase
        }
    }

    private fun stubBook(): Book = mockk()

    @Nested
    inner class OnLaunch {
        @Test
        fun `updates books state when flow emits a list`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val books = listOf(stubBook(), stubBook())
            val collector = CurrentlyReadingBooksCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            booksFlow.emit(books)

            // ----- Assert -----
            stateFlow.value.books shouldBe books
            job.cancel()
        }

        @Test
        fun `sets isLoading to false when flow emits`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(isLoading = true)
            val collector = CurrentlyReadingBooksCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            booksFlow.emit(emptyList())

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe false
            job.cancel()
        }

        @Test
        fun `updates books to the latest emission when flow emits multiple times`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val firstBooks = listOf(stubBook())
            val secondBooks = listOf(stubBook(), stubBook(), stubBook())
            val collector = CurrentlyReadingBooksCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            booksFlow.emit(firstBooks)
            booksFlow.emit(secondBooks)

            // ----- Assert -----
            stateFlow.value.books shouldBe secondBooks
            job.cancel()
        }

        @Test
        fun `sets books to empty list when flow emits an empty list`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(books = listOf(stubBook()))
            val collector = CurrentlyReadingBooksCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            booksFlow.emit(emptyList())

            // ----- Assert -----
            stateFlow.value.books shouldBe emptyList()
            job.cancel()
        }

        @Test
        fun `does not update books before the flow emits`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val initialBooks = listOf(stubBook())
            stateFlow.value = ReadingScreenUiState(books = initialBooks)
            val collector = CurrentlyReadingBooksCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act & Assert -----
            stateFlow.value.books shouldBe initialBooks
            job.cancel()
        }

        @Test
        fun `retains the last emitted books list after the collector job is cancelled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val books = listOf(stubBook(), stubBook())
            val collector = CurrentlyReadingBooksCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }
            booksFlow.emit(books)
            job.cancel()

            // ----- Act & Assert -----
            stateFlow.value.books shouldBe books
        }

        @Test
        fun `preserves all other state fields when updating books`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                isLoading = true,
                showProgressSheet = true,
            )
            val books = listOf(stubBook())
            val collector = CurrentlyReadingBooksCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            booksFlow.emit(books)

            // ----- Assert -----
            stateFlow.value.showProgressSheet shouldBe true
            stateFlow.value.books shouldBe books
            job.cancel()
        }
    }
}
