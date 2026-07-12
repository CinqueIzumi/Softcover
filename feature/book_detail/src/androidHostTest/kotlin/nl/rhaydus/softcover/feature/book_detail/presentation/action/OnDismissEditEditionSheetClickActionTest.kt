package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

class OnDismissEditEditionSheetClickActionTest {
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
        fun `sets showEditEditionSheet to false when it was true`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(showEditEditionSheet = true)
            val action = OnDismissEditEditionSheetClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.showEditEditionSheet shouldBe false
        }

        @Test
        fun `leaves showEditEditionSheet as false when it was already false`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(showEditEditionSheet = false)
            val action = OnDismissEditEditionSheetClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.showEditEditionSheet shouldBe false
        }

        @Test
        fun `resets editionSearchQuery to empty string`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(editionSearchQuery = "Penguin")
            val action = OnDismissEditEditionSheetClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.editionSearchQuery shouldBe ""
        }

        @Test
        fun `resets editionSearchQuery even when it was already empty`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(editionSearchQuery = "")
            val action = OnDismissEditEditionSheetClickAction()

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
                showUpdateProgressSheet = true,
                editionSearchQuery = "isbn",
            )
            stateFlow.value = initialState
            val action = OnDismissEditEditionSheetClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe initialState.copy(
                showEditEditionSheet = false,
                editionSearchQuery = "",
            )
        }
    }
}
