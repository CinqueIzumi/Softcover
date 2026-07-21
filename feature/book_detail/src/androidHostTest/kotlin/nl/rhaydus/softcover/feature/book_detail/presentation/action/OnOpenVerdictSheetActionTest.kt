package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.designsystem.presentation.model.VerdictSheetContext
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

class OnOpenVerdictSheetActionTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<BookDetailLocalVariables>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk<BookDetailDependencies>(relaxed = true)
        stateFlow = MutableStateFlow(
            BookDetailUiState(
                loadingReviews = true,
                revealedSpoilerReviewIds = setOf(1, 2),
            ),
        )
        localVariablesFlow = MutableStateFlow(BookDetailLocalVariables())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = localVariablesFlow,
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `sets verdictSheetContext to FINISHED when constructed with FINISHED`() = runTest {
            // ----- Arrange -----
            val action = OnOpenVerdictSheetAction(context = VerdictSheetContext.FINISHED)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.verdictSheetContext shouldBe VerdictSheetContext.FINISHED
        }

        @Test
        fun `sets verdictSheetContext to EDIT when constructed with EDIT`() = runTest {
            // ----- Arrange -----
            val action = OnOpenVerdictSheetAction(context = VerdictSheetContext.EDIT)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.verdictSheetContext shouldBe VerdictSheetContext.EDIT
        }

        @Test
        fun `leaves other state fields untouched`() = runTest {
            // ----- Arrange -----
            val action = OnOpenVerdictSheetAction(context = VerdictSheetContext.FINISHED)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.loadingReviews shouldBe true
            stateFlow.value.revealedSpoilerReviewIds shouldBe setOf(1, 2)
        }
    }
}
