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
import nl.rhaydus.softcover.feature.books.domain.usecase.GetReadUserBooksUseCase
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadBooksCollectorTest {

    private lateinit var getReadUserBooksUseCase: GetReadUserBooksUseCase
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>
    private lateinit var booksFlow: MutableSharedFlow<List<Book>>

    @BeforeEach
    fun setUp() {
        booksFlow = MutableSharedFlow()
        getReadUserBooksUseCase = mockk()
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getReadUserBooksUseCase()
        } returns booksFlow

        dependencies = mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.getReadUserBooksUseCase
            } returns getReadUserBooksUseCase
        }
    }

    @Nested
    inner class OnLaunch {

        @Test
        fun `updates readBooks and clears isLoading when flow emits`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val books = listOf(mockk<Book>(), mockk<Book>())
            val collector = ReadBooksCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            booksFlow.emit(books)

            // ----- Assert -----
            stateFlow.value.readBooks shouldBe books
            stateFlow.value.isLoading shouldBe false
            job.cancel()
        }

        @Test
        fun `updates readBooks to the latest emission when flow emits multiple times`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val firstBooks = listOf(mockk<Book>())
            val secondBooks = listOf(mockk<Book>(), mockk<Book>())
            val collector = ReadBooksCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            booksFlow.emit(firstBooks)
            booksFlow.emit(secondBooks)

            // ----- Assert -----
            stateFlow.value.readBooks shouldBe secondBooks
            job.cancel()
        }

        @Test
        fun `updates readBooks to empty list when flow emits empty list`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ReadBooksCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            booksFlow.emit(emptyList())

            // ----- Assert -----
            stateFlow.value.readBooks shouldBe emptyList()
            stateFlow.value.isLoading shouldBe false
            job.cancel()
        }

        @Test
        fun `does not change readBooks before the flow emits`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ReadBooksCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act & Assert -----
            stateFlow.value.readBooks shouldBe null
            job.cancel()
        }

        @Test
        fun `preserves other state fields when updating readBooks`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val existingDnfBooks = listOf(mockk<Book>())
            stateFlow.value = LibraryUiState(dnfBooks = existingDnfBooks, isLoading = true)
            val books = listOf(mockk<Book>())
            val collector = ReadBooksCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            booksFlow.emit(books)

            // ----- Assert -----
            stateFlow.value.dnfBooks shouldBe existingDnfBooks
            stateFlow.value.readBooks shouldBe books
            job.cancel()
        }
    }
}
