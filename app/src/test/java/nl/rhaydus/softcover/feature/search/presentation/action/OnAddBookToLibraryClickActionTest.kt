package nl.rhaydus.softcover.feature.search.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.books.domain.usecase.MarkBookAsWantToReadUseCase
import nl.rhaydus.softcover.feature.search.presentation.event.SearchEvent
import nl.rhaydus.softcover.feature.search.presentation.screenmodel.SearchDependencies
import nl.rhaydus.softcover.feature.search.presentation.state.SearchLocalVariables
import nl.rhaydus.softcover.feature.search.presentation.state.SearchScreenUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnAddBookToLibraryClickActionTest {

    private lateinit var markBookAsWantToReadUseCase: MarkBookAsWantToReadUseCase
    private lateinit var dependencies: SearchDependencies
    private lateinit var stateFlow: MutableStateFlow<SearchScreenUiState>
    private lateinit var scope: ActionScope<SearchScreenUiState, SearchEvent, SearchLocalVariables>

    @BeforeEach
    fun setUp() {
        markBookAsWantToReadUseCase = mockk()
        stateFlow = MutableStateFlow(SearchScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(SearchLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(testScope: kotlinx.coroutines.test.TestScope): SearchDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<SearchDependencies>(relaxed = true).also { mock ->
            every {
                mock.markBookAsWantToReadUseCase
            } returns markBookAsWantToReadUseCase

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

    private fun stubBook(id: Int = 1): Book = mockk {
        every {
            this@mockk.id
        } returns id
    }

    @Nested
    inner class Execute {

        @Test
        fun `invokes markBookAsWantToReadUseCase with the book id`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val book = stubBook(id = bookId)
            dependencies = stubDependencies(this)

            coEvery {
                markBookAsWantToReadUseCase(bookId = bookId)
            } returns Result.success(Unit)

            val action = OnAddBookToLibraryClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                markBookAsWantToReadUseCase(bookId = bookId)
            }
        }

        @Test
        fun `does not change state when use case succeeds`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 7)
            dependencies = stubDependencies(this)

            coEvery {
                markBookAsWantToReadUseCase(bookId = 7)
            } returns Result.success(Unit)

            val action = OnAddBookToLibraryClickAction(book = book)
            val stateBefore = stateFlow.value

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe stateBefore
        }

        @Test
        fun `does not change state when use case fails`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 3)
            dependencies = stubDependencies(this)

            coEvery {
                markBookAsWantToReadUseCase(bookId = 3)
            } returns Result.failure(RuntimeException("network error"))

            val action = OnAddBookToLibraryClickAction(book = book)
            val stateBefore = stateFlow.value

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe stateBefore
        }
    }
}
