package nl.rhaydus.softcover.feature.explore.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.removal.UndoableBookRemover
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnRemoveBookFromLibraryClickActionTest {
    private lateinit var undoableBookRemover: UndoableBookRemover
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>

    @BeforeEach
    fun setUp() {
        undoableBookRemover = mockk()
        stateFlow = MutableStateFlow(ExploreScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ExploreLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk<ExploreDependencies>(relaxed = true).also { mock ->
            every {
                mock.undoableBookRemover
            } returns undoableBookRemover
        }
    }

    @Nested
    inner class Execute {
        @Test
        fun `hands the book to the undoable remover as a single-item list`() = runTest {
            // ----- Arrange -----
            val book = mockk<Book>()

            coEvery {
                undoableBookRemover.remove(books = listOf(book))
            } returns Result.success(Unit)

            val action = OnRemoveBookFromLibraryClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) { undoableBookRemover.remove(books = listOf(book)) }
        }

        @Test
        fun `does not change state when the remover fails`() = runTest {
            // ----- Arrange -----
            val book = mockk<Book>()

            coEvery {
                undoableBookRemover.remove(books = listOf(book))
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
