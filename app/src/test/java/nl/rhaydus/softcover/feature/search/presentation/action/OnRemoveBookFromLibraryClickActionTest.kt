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
import nl.rhaydus.softcover.feature.books.domain.usecase.RemoveBookFromLibraryUseCase
import nl.rhaydus.softcover.feature.search.presentation.event.SearchEvent
import nl.rhaydus.softcover.feature.search.presentation.screenmodel.SearchDependencies
import nl.rhaydus.softcover.feature.search.presentation.state.SearchLocalVariables
import nl.rhaydus.softcover.feature.search.presentation.state.SearchScreenUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnRemoveBookFromLibraryClickActionTest {

    private lateinit var removeBookFromLibraryUseCase: RemoveBookFromLibraryUseCase
    private lateinit var dependencies: SearchDependencies
    private lateinit var stateFlow: MutableStateFlow<SearchScreenUiState>
    private lateinit var scope: ActionScope<SearchScreenUiState, SearchEvent, SearchLocalVariables>

    @BeforeEach
    fun setUp() {
        removeBookFromLibraryUseCase = mockk()
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

    private fun stubBook(): Book = mockk()

    @Nested
    inner class Execute {

        @Test
        fun `invokes removeBookFromLibraryUseCase with the given book`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            dependencies = stubDependencies(this)

            coEvery {
                removeBookFromLibraryUseCase(book = book)
            } returns Result.success(Unit)

            val action = OnRemoveBookFromLibraryClickAction(book = book)

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
        fun `does not change state when use case succeeds`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            dependencies = stubDependencies(this)

            coEvery {
                removeBookFromLibraryUseCase(book = book)
            } returns Result.success(Unit)

            val action = OnRemoveBookFromLibraryClickAction(book = book)
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
            val book = stubBook()
            dependencies = stubDependencies(this)

            coEvery {
                removeBookFromLibraryUseCase(book = book)
            } returns Result.failure(RuntimeException("remove failed"))

            val action = OnRemoveBookFromLibraryClickAction(book = book)
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
