package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.buildEdition
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.editionSheetScaffoldFor
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class EditionSheetCollectorTest {
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

    private fun offShelfBook(title: String): Book = mockk<Book>().also {
        every {
            it.title
        } returns title
        every {
            it.userBook
        } returns null
        every {
            it.currentEdition
        } returns null
    }

    private fun TestScope.launchCollector() {
        backgroundScope.launch {
            EditionSheetCollector().onLaunch(
                scope = scope,
                dependencies = dependencies,
            )
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `leaves editionSheet null while there is no displayed edition`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                editionSheetRequested = true,
                book = offShelfBook("Dune"),
            )

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.editionSheet shouldBe SheetSession.Closed
        }

        @Test
        fun `builds editionSheet with the primary disabled while the selection is the current edition`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val current = buildEdition(id = 1)
                stateFlow.value = buildBookDetailUiState().copy(
                    editionSheetRequested = true,
                    book = offShelfBook("Dune"),
                    previewEdition = current,
                    editions = persistentListOf(current, buildEdition(id = 2)),
                )

                // ----- Act -----
                launchCollector()

                // ----- Assert -----
                stateFlow.value.editionSheet.modelOrNull.shouldNotBeNull().scaffold shouldBe editionSheetScaffoldFor(
                    bookTitle = "Dune",
                    currentEdition = current,
                    selectedEdition = current,
                    cover = null,
                    isLoading = false,
                )
            }

        @Test
        fun `enables the primary when selectedEditionId changes to another edition`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val current = buildEdition(id = 1)
            val other = buildEdition(id = 2)
            stateFlow.value = buildBookDetailUiState().copy(
                editionSheetRequested = true,
                book = offShelfBook("Dune"),
                previewEdition = current,
                editions = persistentListOf(current, other),
            )
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(selectedEditionId = other.id)

            // ----- Assert -----
            stateFlow.value.editionSheet.modelOrNull.shouldNotBeNull().scaffold shouldBe editionSheetScaffoldFor(
                bookTitle = "Dune",
                currentEdition = current,
                selectedEdition = other,
                cover = null,
                isLoading = false,
            )
        }

        @Test
        fun `disables the primary when editions start loading`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val current = buildEdition(id = 1)
            val other = buildEdition(id = 2)
            stateFlow.value = buildBookDetailUiState().copy(
                editionSheetRequested = true,
                book = offShelfBook("Dune"),
                previewEdition = current,
                editions = persistentListOf(current, other),
                selectedEditionId = other.id,
            )
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(loadingEditions = true)

            // ----- Assert -----
            stateFlow.value.editionSheet.modelOrNull.shouldNotBeNull().scaffold shouldBe editionSheetScaffoldFor(
                bookTitle = "Dune",
                currentEdition = current,
                selectedEdition = other,
                cover = null,
                isLoading = true,
            )
        }

        @Test
        fun `requests the sheet to close once it is no longer requested`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                editionSheetRequested = true,
                book = offShelfBook("Dune"),
                previewEdition = buildEdition(id = 1),
            )
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(editionSheetRequested = false)

            // ----- Assert -----
            stateFlow.value.editionSheet.modelOrNull.shouldNotBeNull().closeRequested shouldBe true
        }

        @Test
        fun `carries the cards, count header and search query from state into editionSheet`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val current = buildEdition(id = 1)
                val cards = persistentListOf(mockk<BookCardUiModel>())
                val countHeader = SectionHeaderUiModel.Label(eyebrow = "1 EDITIONS")
                stateFlow.value = buildBookDetailUiState().copy(
                    editionSheetRequested = true,
                    book = offShelfBook("Dune"),
                    previewEdition = current,
                    editions = persistentListOf(current),
                    editionCards = cards,
                    editionCountHeader = countHeader,
                    editionSearchQuery = "penguin",
                )

                // ----- Act -----
                launchCollector()

                // ----- Assert -----
                val sheet = stateFlow.value.editionSheet.modelOrNull.shouldNotBeNull()
                sheet.cards shouldBe cards
                sheet.countHeader shouldBe countHeader
                sheet.searchQuery shouldBe "penguin"
            }

        @Test
        fun `follows the search query as it changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val current = buildEdition(id = 1)
            stateFlow.value = buildBookDetailUiState().copy(
                editionSheetRequested = true,
                book = offShelfBook("Dune"),
                previewEdition = current,
                editions = persistentListOf(current),
            )
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(editionSearchQuery = "978")

            // ----- Assert -----
            stateFlow.value.editionSheet.modelOrNull.shouldNotBeNull().searchQuery shouldBe "978"
        }

        @Test
        fun `shows search and empty-state copy once the sheet is built`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val current = buildEdition(id = 1)
            stateFlow.value = buildBookDetailUiState().copy(
                editionSheetRequested = true,
                book = offShelfBook("Dune"),
                previewEdition = current,
                editions = persistentListOf(current),
            )

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            val sheet = stateFlow.value.editionSheet.modelOrNull.shouldNotBeNull()
            sheet.searchPlaceholder.isNotBlank() shouldBe true
            sheet.emptyState shouldBe null
        }

        @Test
        fun `shows the empty state when loaded without error and no edition matches`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val current = buildEdition(id = 1)
            stateFlow.value = buildBookDetailUiState().copy(
                editionSheetRequested = true,
                book = offShelfBook("Dune"),
                previewEdition = current,
                editions = persistentListOf(current),
                editionSearchQuery = "no-such-publisher",
            )

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.editionSheet.modelOrNull.shouldNotBeNull().emptyState.shouldNotBeNull()
        }

        @Test
        fun `omits the empty state while editions are loading`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val current = buildEdition(id = 1)
            stateFlow.value = buildBookDetailUiState().copy(
                editionSheetRequested = true,
                book = offShelfBook("Dune"),
                previewEdition = current,
                editions = persistentListOf(current),
                editionSearchQuery = "no-such-publisher",
                loadingEditions = true,
            )

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.editionSheet.modelOrNull.shouldNotBeNull().emptyState.shouldBeNull()
        }

        @Test
        fun `carries editionsError and omits the empty state when loading failed`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val current = buildEdition(id = 1)
            stateFlow.value = buildBookDetailUiState().copy(
                editionSheetRequested = true,
                book = offShelfBook("Dune"),
                previewEdition = current,
                editions = persistentListOf(),
                editionsError = "Couldn't load the editions.",
            )

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            val sheet = stateFlow.value.editionSheet.modelOrNull.shouldNotBeNull()
            sheet.error shouldBe "Couldn't load the editions."
            sheet.emptyState.shouldBeNull()
        }
    }
}
