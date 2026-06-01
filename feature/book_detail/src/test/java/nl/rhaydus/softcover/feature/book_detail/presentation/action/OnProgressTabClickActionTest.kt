package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.presentation.model.ProgressSheetTab
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnProgressTabClickActionTest {

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
        fun `sets selectedProgressSheetTab to PAGE when tab is PAGE`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(selectedProgressSheetTab = ProgressSheetTab.PERCENTAGE)
            val action = OnProgressTabClickAction(tab = ProgressSheetTab.PAGE)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectedProgressSheetTab shouldBe ProgressSheetTab.PAGE
        }

        @Test
        fun `sets selectedProgressSheetTab to PERCENTAGE when tab is PERCENTAGE`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(selectedProgressSheetTab = ProgressSheetTab.PAGE)
            val action = OnProgressTabClickAction(tab = ProgressSheetTab.PERCENTAGE)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectedProgressSheetTab shouldBe ProgressSheetTab.PERCENTAGE
        }

        @Test
        fun `does not mutate any other state field when updating the selected tab`() = runTest {
            // ----- Arrange -----
            val initialState = stateFlow.value
            val action = OnProgressTabClickAction(tab = ProgressSheetTab.PERCENTAGE)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe initialState.copy(selectedProgressSheetTab = ProgressSheetTab.PERCENTAGE)
        }
    }
}
