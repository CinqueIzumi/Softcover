package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
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
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toHowItReadSheet
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.HowItReadDraft
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HowItReadSheetCollectorTest {
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

    private fun titledBook(title: String = "Piranesi"): Book = mockk<Book>().also {
        every {
            it.title
        } returns title
    }

    private fun TestScope.launchCollector() {
        backgroundScope.launch {
            HowItReadSheetCollector().onLaunch(
                scope = scope,
                dependencies = dependencies,
            )
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `opens the sheet with a model of the draft while a draft exists`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val draft = HowItReadDraft(paceKey = "pace-fast")
            stateFlow.value = buildBookDetailUiState().copy(
                book = titledBook(),
                howItReadDraft = draft,
            )

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.howItReadSheet.modelOrNull shouldBe draft.toHowItReadSheet(bookTitle = "Piranesi")
        }

        @Test
        fun `follows the draft as the reader edits it`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = titledBook(),
                howItReadDraft = HowItReadDraft(),
            )
            launchCollector()
            val edited = HowItReadDraft(paceKey = "pace-slow")

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(howItReadDraft = edited)

            // ----- Assert -----
            stateFlow.value.howItReadSheet.modelOrNull shouldBe edited.toHowItReadSheet(bookTitle = "Piranesi")
        }

        @Test
        fun `asks the sheet to hide once the draft is cleared`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = titledBook(),
                howItReadDraft = HowItReadDraft(),
            )
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(howItReadDraft = null)

            // ----- Assert -----
            stateFlow.value.howItReadSheet.isOpen shouldBe true
            stateFlow.value.howItReadSheet.isClosing shouldBe true
        }

        @Test
        fun `keeps the sheet closed while the book has not loaded`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(howItReadDraft = HowItReadDraft())

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.howItReadSheet.isOpen shouldBe false
        }
    }
}
