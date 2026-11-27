package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.isClosing
import nl.rhaydus.softcover.core.component.sheet.isOpen
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.domain.model.DidNotFinishReason
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.buildDidNotFinishReasonDraft
import nl.rhaydus.softcover.feature.book_detail.presentation.buildQuoteBook
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toDidNotFinishReasonSheet
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DidNotFinishReasonSheetCollectorTest {
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

    private fun TestScope.launchCollector() {
        backgroundScope.launch {
            DidNotFinishReasonSheetCollector().onLaunch(
                scope = scope,
                dependencies = mockk(relaxed = true),
            )
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `opens the sheet with a model of the draft while a draft exists`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val draft = buildDidNotFinishReasonDraft(reasons = setOf(DidNotFinishReason.TooSlow))

            stateFlow.value = buildBookDetailUiState().copy(
                book = buildQuoteBook(title = "Piranesi"),
                didNotFinishReasonDraft = draft,
            )

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.didNotFinishReasonSheet.modelOrNull shouldBe draft.toDidNotFinishReasonSheet(
                bookTitle = "Piranesi",
            )
        }

        @Test
        fun `follows the draft as the reader toggles reasons`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = buildQuoteBook(title = "Piranesi"),
                didNotFinishReasonDraft = buildDidNotFinishReasonDraft(),
            )

            launchCollector()
            val edited = buildDidNotFinishReasonDraft(reasons = setOf(DidNotFinishReason.LostInterest))

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(didNotFinishReasonDraft = edited)

            // ----- Assert -----
            stateFlow.value.didNotFinishReasonSheet.modelOrNull shouldBe edited.toDidNotFinishReasonSheet(
                bookTitle = "Piranesi",
            )
        }

        @Test
        fun `asks the sheet to hide once the draft is cleared`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = buildQuoteBook(title = "Piranesi"),
                didNotFinishReasonDraft = buildDidNotFinishReasonDraft(),
            )

            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(didNotFinishReasonDraft = null)

            // ----- Assert -----
            stateFlow.value.didNotFinishReasonSheet.isOpen shouldBe true
            stateFlow.value.didNotFinishReasonSheet.isClosing shouldBe true
        }

        @Test
        fun `keeps the sheet closed while the book has not loaded`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(didNotFinishReasonDraft = buildDidNotFinishReasonDraft())

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.didNotFinishReasonSheet.isOpen shouldBe false
        }
    }
}
