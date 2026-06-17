package nl.rhaydus.softcover.feature.explore.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsWantToReadUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ShelfMutationOutcome
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnAddBookToLibraryClickActionTest {
    private lateinit var markBookAsWantToReadUseCase: MarkBookAsWantToReadUseCase
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>

    @BeforeEach
    fun setUp() {
        markBookAsWantToReadUseCase = mockk()
        stateFlow = MutableStateFlow(ExploreScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ExploreLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(testScope: kotlinx.coroutines.test.TestScope): ExploreDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<ExploreDependencies>(relaxed = true).also { mock ->
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
        fun `invokes markBookAsWantToReadUseCase with the book`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            dependencies = stubDependencies(this)

            coEvery {
                markBookAsWantToReadUseCase(book = book)
            } returns Result.success(ShelfMutationOutcome.Applied)

            val action = OnAddBookToLibraryClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                markBookAsWantToReadUseCase(book = book)
            }
        }

        @Test
        fun `does not change state when use case succeeds`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 7)
            dependencies = stubDependencies(this)

            coEvery {
                markBookAsWantToReadUseCase(book = book)
            } returns Result.success(ShelfMutationOutcome.Applied)

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
                markBookAsWantToReadUseCase(book = book)
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
