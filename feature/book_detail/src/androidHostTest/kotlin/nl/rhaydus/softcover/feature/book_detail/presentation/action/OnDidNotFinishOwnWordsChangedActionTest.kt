package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
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

class OnDidNotFinishOwnWordsChangedActionTest {
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(buildBookDetailUiState())

        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `replaces the own words and keeps the selected reasons`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(
                didNotFinishReasonDraft = buildDidNotFinishReasonDraft(
                    reasons = setOf(DidNotFinishReason.LostInterest),
                    ownWords = "Old",
                ),
            )

            // ----- Act -----
            OnDidNotFinishOwnWordsChangedAction(text = "New words").execute(
                dependencies = mockk(relaxed = true),
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.didNotFinishReasonDraft shouldBe buildDidNotFinishReasonDraft(
                reasons = setOf(DidNotFinishReason.LostInterest),
                ownWords = "New words",
            )
        }

        @Test
        fun `leaves no draft when the sheet is not open`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState()

            // ----- Act -----
            OnDidNotFinishOwnWordsChangedAction(text = "New words").execute(
                dependencies = mockk(relaxed = true),
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.didNotFinishReasonDraft.shouldBeNull()
        }
    }
}
