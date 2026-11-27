package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.domain.model.DidNotFinishReason
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.buildDidNotFinishReasonDraft
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnDismissDidNotFinishReasonSheetActionTest {
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var eventChannel: Channel<BookDetailEvent>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        eventChannel = Channel(Channel.BUFFERED)

        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = eventChannel,
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `discards the draft and closes the sheet without saving`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(
                didNotFinishReasonDraft = buildDidNotFinishReasonDraft(
                    reasons = setOf(DidNotFinishReason.TooSlow),
                    ownWords = "Unsaved",
                ),
            )

            // ----- Act -----
            OnDismissDidNotFinishReasonSheetAction().execute(
                dependencies = mockk(relaxed = true),
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.didNotFinishReasonDraft.shouldBeNull()
            stateFlow.value.didNotFinishReasonSheet shouldBe SheetSession.Closed
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }
    }
}
