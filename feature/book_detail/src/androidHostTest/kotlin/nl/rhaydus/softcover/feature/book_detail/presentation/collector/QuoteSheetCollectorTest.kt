package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.quote.QuotePositionTab
import nl.rhaydus.softcover.core.component.sheet.isClosing
import nl.rhaydus.softcover.core.component.sheet.isOpen
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.buildQuoteBook
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.QuoteDraft
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class QuoteSheetCollectorTest {
    private lateinit var dependencies: BookDetailDependencies
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
        dependencies = mockk(relaxed = true)
    }

    private fun draft(text: String = ""): QuoteDraft = QuoteDraft(
        tab = QuotePositionTab.PAGE,
        privacy = PrivacySetting.PUBLIC,
        text = text,
    )

    private fun TestScope.launchCollector() {
        backgroundScope.launch {
            QuoteSheetCollector().onLaunch(
                scope = scope,
                dependencies = dependencies,
            )
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `opens the sheet with the draft's text while a draft exists`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = buildQuoteBook(currentPages = 437),
                quoteDraft = draft(text = "A line worth keeping"),
            )

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.quoteSheet.modelOrNull.shouldNotBeNull().text.text shouldBe "A line worth keeping"
        }

        @Test
        fun `follows the draft as the reader types`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = buildQuoteBook(currentPages = 437),
                quoteDraft = draft(),
            )
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(quoteDraft = draft(text = "Typed"))

            // ----- Assert -----
            stateFlow.value.quoteSheet.modelOrNull.shouldNotBeNull().text.text shouldBe "Typed"
        }

        @Test
        fun `asks the sheet to hide once the draft is cleared`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = buildQuoteBook(currentPages = 437),
                quoteDraft = draft(),
            )
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(quoteDraft = null)

            // ----- Assert -----
            stateFlow.value.quoteSheet.isOpen shouldBe true
            stateFlow.value.quoteSheet.isClosing shouldBe true
        }
    }
}
