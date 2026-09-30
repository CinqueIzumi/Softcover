package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.share.BookShareCardUiModel
import nl.rhaydus.softcover.core.component.share.ReadingUpdateShareCardUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShareCardVariant
import nl.rhaydus.softcover.feature.book_detail.presentation.state.shareSheetScaffoldFor
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ShareSheetCollectorTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(BookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk(relaxed = true)
    }

    private fun bookCardTitled(title: String): BookShareCardUiModel = mockk<BookShareCardUiModel>().also {
        every {
            it.title
        } returns title
    }

    private fun updateCard(): ReadingUpdateShareCardUiModel = mockk()

    private fun TestScope.launchCollector() {
        backgroundScope.launch {
            ShareSheetCollector().onLaunch(
                scope = scope,
                dependencies = dependencies,
            )
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `leaves shareSheet and shareSheetBody null while there is no book card`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(shareBookCard = null)

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.shareSheet.shouldBeNull()
            stateFlow.value.shareSheetBody.shouldBeNull()
        }

        @Test
        fun `builds the sheet for the book variant when there is no update card`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(shareBookCard = bookCardTitled("Dune"))

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.shareSheet shouldBe shareSheetScaffoldFor(
                bookTitle = "Dune",
                variant = ShareCardVariant.BOOK,
            )
        }

        @Test
        fun `builds the sheet for the update variant when an update card exists`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(
                shareBookCard = bookCardTitled("Dune"),
                shareUpdateCard = updateCard(),
            )

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.shareSheet shouldBe shareSheetScaffoldFor(
                bookTitle = "Dune",
                variant = ShareCardVariant.MY_UPDATE,
            )
        }

        @Test
        fun `updates the subtitle when the book title changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(shareBookCard = bookCardTitled("Dune"))
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(shareBookCard = bookCardTitled("Emma"))

            // ----- Assert -----
            stateFlow.value.shareSheet shouldBe shareSheetScaffoldFor(
                bookTitle = "Emma",
                variant = ShareCardVariant.BOOK,
            )
        }

        @Test
        fun `clears the sheet when the book card goes away`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(shareBookCard = bookCardTitled("Dune"))
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(shareBookCard = null)

            // ----- Assert -----
            stateFlow.value.shareSheet.shouldBeNull()
        }
    }

    @Nested
    inner class ShareSheetBody {
        @Test
        fun `leaves the body null while there is no book card`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(shareBookCard = null)

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.shareSheetBody.shouldBeNull()
        }

        @Test
        fun `clears the body when the book card goes away`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(shareBookCard = bookCardTitled("Dune"))
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(shareBookCard = null)

            // ----- Assert -----
            stateFlow.value.shareSheetBody.shouldBeNull()
        }

        @Test
        fun `defaults the body to my update when an update card exists`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(
                shareBookCard = bookCardTitled("Dune"),
                shareUpdateCard = updateCard(),
            )

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.shareSheetBody.shouldNotBeNull().variant shouldBe ShareCardVariant.MY_UPDATE
        }

        @Test
        fun `defaults the body to the book when there is no update card`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(shareBookCard = bookCardTitled("Dune"))

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.shareSheetBody.shouldNotBeNull().variant shouldBe ShareCardVariant.BOOK
        }

        @Test
        fun `keeps an explicit book choice when an update card exists`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(
                shareBookCard = bookCardTitled("Dune"),
                shareUpdateCard = updateCard(),
            )
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(chosenShareVariant = ShareCardVariant.BOOK)

            // ----- Assert -----
            stateFlow.value.shareSheetBody.shouldNotBeNull().variant shouldBe ShareCardVariant.BOOK
        }

        @Test
        fun `keeps an explicit update choice when an update card exists`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(
                shareBookCard = bookCardTitled("Dune"),
                shareUpdateCard = updateCard(),
            )
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(chosenShareVariant = ShareCardVariant.MY_UPDATE)

            // ----- Assert -----
            stateFlow.value.shareSheetBody.shouldNotBeNull().variant shouldBe ShareCardVariant.MY_UPDATE
        }

        @Test
        fun `falls back to the book when an update choice has no update card`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(
                shareBookCard = bookCardTitled("Dune"),
                chosenShareVariant = ShareCardVariant.MY_UPDATE,
            )

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.shareSheetBody.shouldNotBeNull().variant shouldBe ShareCardVariant.BOOK
        }

        @Test
        fun `switches to my update when the update card arrives later`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(shareBookCard = bookCardTitled("Dune"))
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(shareUpdateCard = updateCard())

            // ----- Assert -----
            stateFlow.value.shareSheetBody.shouldNotBeNull().variant shouldBe ShareCardVariant.MY_UPDATE
        }

        @Test
        fun `is busy while sharing`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(
                shareBookCard = bookCardTitled("Dune"),
                shareUpdateCard = updateCard(),
            )
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(isSharing = true)

            // ----- Assert -----
            stateFlow.value.shareSheetBody.shouldNotBeNull().isBusy shouldBe true
        }
    }
}
