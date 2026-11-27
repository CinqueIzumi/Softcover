package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.dismissed
import nl.rhaydus.softcover.core.component.sheet.isClosing
import nl.rhaydus.softcover.core.uibinding.session.SessionEndingStatus
import nl.rhaydus.softcover.core.uibinding.session.endSessionSheetScaffold
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnEndSessionClickActionTest {
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(
            buildBookDetailUiState().copy(
                endSessionRequest = SessionEndingStatus.Paused,
                endSessionSheet = SheetSession.Open(
                    model = endSessionSheetScaffold(
                        bookTitle = "Dune",
                        targetStatus = SessionEndingStatus.Paused,
                    ),
                ),
            ),
        )

        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `asks the sheet to close with the waiting status recorded as pending`() = runTest {
            // ----- Arrange -----
            val action = OnEndSessionClickAction()

            // ----- Act -----
            action.execute(
                dependencies = mockk<BookDetailDependencies>(relaxed = true),
                scope = scope,
            )

            // ----- Assert -----
            val sheet = stateFlow.value.endSessionSheet
            sheet.isClosing shouldBe true
            sheet.dismissed() shouldBe SessionEndingStatus.Paused
        }

        @Test
        fun `records Did not finish as pending when that is the requested status`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(endSessionRequest = SessionEndingStatus.DidNotFinish)
            val action = OnEndSessionClickAction()

            // ----- Act -----
            action.execute(
                dependencies = mockk<BookDetailDependencies>(relaxed = true),
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.endSessionSheet.dismissed() shouldBe SessionEndingStatus.DidNotFinish
        }

        @Test
        fun `clears the end-session request`() = runTest {
            // ----- Arrange -----
            val action = OnEndSessionClickAction()

            // ----- Act -----
            action.execute(
                dependencies = mockk<BookDetailDependencies>(relaxed = true),
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.endSessionRequest shouldBe null
        }
    }
}
