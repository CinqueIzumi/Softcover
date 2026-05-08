package nl.rhaydus.softcover.feature.explore.presentation.flows

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
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetQueriedBooksUseCase
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class QueriedBooksCollectorTest {

    private lateinit var getAllUserBooksUseCase: GetAllUserBooksUseCase
    private lateinit var getQueriedBooksUseCase: GetQueriedBooksUseCase
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>
    private lateinit var allUserBooksFlow: MutableSharedFlow<List<Book>>
    private lateinit var queriedBooksFlow: MutableSharedFlow<List<Book>>

    @BeforeEach
    fun setUp() {
        allUserBooksFlow = MutableSharedFlow()
        queriedBooksFlow = MutableSharedFlow()
        getAllUserBooksUseCase = mockk()
        getQueriedBooksUseCase = mockk()
        stateFlow = MutableStateFlow(ExploreScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ExploreLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getAllUserBooksUseCase()
        } returns allUserBooksFlow

        every {
            getQueriedBooksUseCase()
        } returns queriedBooksFlow

        dependencies = mockk<ExploreDependencies>(relaxed = true).also { mock ->
            every {
                mock.getAllUserBooksUseCase
            } returns getAllUserBooksUseCase

            every {
                mock.getQueriedBooksUseCase
            } returns getQueriedBooksUseCase
        }
    }

    private fun stubBook(id: Int): Book = mockk {
        every { this@mockk.id } returns id
    }

    @Nested
    inner class OnLaunch {

        @Test
        fun `updates queriedBooks when both flows emit`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val fetchedBook = stubBook(id = 1)
            val collector = QueriedBooksCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            allUserBooksFlow.emit(emptyList())
            queriedBooksFlow.emit(listOf(fetchedBook))

            // ----- Assert -----
            stateFlow.value.queriedBooks shouldBe listOf(fetchedBook)
            job.cancel()
        }

        @Test
        fun `replaces fetched book with the matching user book when ids match`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val fetchedBook = stubBook(id = 5)
            val userBook = stubBook(id = 5)
            val collector = QueriedBooksCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            allUserBooksFlow.emit(listOf(userBook))
            queriedBooksFlow.emit(listOf(fetchedBook))

            // ----- Assert -----
            stateFlow.value.queriedBooks shouldBe listOf(userBook)
            job.cancel()
        }

        @Test
        fun `keeps fetched book when no matching user book is found`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val fetchedBook = stubBook(id = 10)
            val userBook = stubBook(id = 99)
            val collector = QueriedBooksCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            allUserBooksFlow.emit(listOf(userBook))
            queriedBooksFlow.emit(listOf(fetchedBook))

            // ----- Assert -----
            stateFlow.value.queriedBooks shouldBe listOf(fetchedBook)
            job.cancel()
        }

        @Test
        fun `updates queriedBooks to empty list when fetched books are empty`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val userBook = stubBook(id = 1)
            val collector = QueriedBooksCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            allUserBooksFlow.emit(listOf(userBook))
            queriedBooksFlow.emit(emptyList())

            // ----- Assert -----
            stateFlow.value.queriedBooks shouldBe emptyList()
            job.cancel()
        }

        @Test
        fun `merges multiple books replacing matched entries and keeping unmatched ones`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val fetchedBook1 = stubBook(id = 1)
            val fetchedBook2 = stubBook(id = 2)
            val userBook1 = stubBook(id = 1)
            val collector = QueriedBooksCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            allUserBooksFlow.emit(listOf(userBook1))
            queriedBooksFlow.emit(listOf(fetchedBook1, fetchedBook2))

            // ----- Assert -----
            val result = stateFlow.value.queriedBooks
            result[0] shouldBe userBook1
            result[1] shouldBe fetchedBook2
            job.cancel()
        }

        @Test
        fun `does not change queriedBooks before both flows emit`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = QueriedBooksCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            allUserBooksFlow.emit(listOf(stubBook(id = 1)))
            // queriedBooksFlow has NOT emitted yet

            // ----- Assert -----
            stateFlow.value.queriedBooks shouldBe emptyList()
            job.cancel()
        }

        @Test
        fun `responds to latest value when one flow re-emits`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val fetchedBook = stubBook(id = 3)
            val updatedUserBook = stubBook(id = 3)
            val collector = QueriedBooksCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            allUserBooksFlow.emit(emptyList())
            queriedBooksFlow.emit(listOf(fetchedBook))

            // ----- Act -----
            allUserBooksFlow.emit(listOf(updatedUserBook))

            // ----- Assert -----
            stateFlow.value.queriedBooks shouldBe listOf(updatedUserBook)
            job.cancel()
        }

        @Test
        fun `preserves all other state fields when updating queriedBooks`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ExploreScreenUiState(
                searchText = "test query",
                isLoading = false,
                previousSearchQueries = listOf("old"),
            )
            val collector = QueriedBooksCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            allUserBooksFlow.emit(emptyList())
            queriedBooksFlow.emit(listOf(stubBook(id = 7)))

            // ----- Assert -----
            stateFlow.value.searchText shouldBe "test query"
            stateFlow.value.isLoading shouldBe false
            stateFlow.value.previousSearchQueries shouldBe listOf("old")
            job.cancel()
        }
    }
}
