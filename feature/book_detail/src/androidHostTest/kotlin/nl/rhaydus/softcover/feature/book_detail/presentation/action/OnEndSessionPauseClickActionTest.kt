package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.dismissed
import nl.rhaydus.softcover.core.component.sheet.isClosing
import nl.rhaydus.softcover.core.uibinding.session.endSessionPauseSheetScaffold
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnEndSessionPauseClickActionTest {
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(
            buildBookDetailUiState().copy(
                endSessionPauseRequested = true,
                endSessionPauseSheet = SheetSession.Open(model = endSessionPauseSheetScaffold(bookTitle = "Dune")),
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
        fun `asks the sheet to close with the pause recorded as pending`() = runTest {
            // ----- Arrange -----
            val action = OnEndSessionPauseClickAction()

            // ----- Act -----
            action.execute(
                dependencies = mockk<BookDetailDependencies>(relaxed = true),
                scope = scope,
            )

            // ----- Assert -----
            val sheet = stateFlow.value.endSessionPauseSheet
            sheet.isClosing shouldBe true
            sheet.dismissed().shouldNotBeNull().bookIds shouldBe setOf(stateFlow.value.bookId)
        }

        @Test
        fun `clears the pause request`() = runTest {
            // ----- Arrange -----
            val action = OnEndSessionPauseClickAction()

            // ----- Act -----
            action.execute(
                dependencies = mockk<BookDetailDependencies>(relaxed = true),
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.endSessionPauseRequested shouldBe false
        }
    }
}
