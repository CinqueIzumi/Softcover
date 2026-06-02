package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnEditionSearchQueryChangeActionTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(BookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `writes the query into editionSearchQuery`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(editionSearchQuery = "")
            val action = OnEditionSearchQueryChangeAction(query = "978")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.editionSearchQuery shouldBe "978"
        }

        @Test
        fun `overwrites a previous query with the new value`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(editionSearchQuery = "old query")
            val action = OnEditionSearchQueryChangeAction(query = "new query")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.editionSearchQuery shouldBe "new query"
        }

        @Test
        fun `accepts an empty string and clears editionSearchQuery`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(editionSearchQuery = "something")
            val action = OnEditionSearchQueryChangeAction(query = "")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.editionSearchQuery shouldBe ""
        }

        @Test
        fun `does not mutate any other state fields`() = runTest {
            // ----- Arrange -----
            val initialState = BookDetailUiState(
                loadingBookDetails = false,
                showEditEditionSheet = true,
                fabMenuExpanded = true,
                editionSearchQuery = "",
            )
            stateFlow.value = initialState
            val action = OnEditionSearchQueryChangeAction(query = "Penguin")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe initialState.copy(editionSearchQuery = "Penguin")
        }
    }
}
