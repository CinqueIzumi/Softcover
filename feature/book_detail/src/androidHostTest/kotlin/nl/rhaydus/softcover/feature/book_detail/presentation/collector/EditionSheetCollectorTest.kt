package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
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
        stateFlow = MutableStateFlow(BookDetailUiState())
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
        fun `leaves editionSheet null while there is no book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(previewEdition = buildEdition(id = 1))

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.editionSheet.shouldBeNull()
        }

        @Test
        fun `leaves editionSheet null while there is no displayed edition`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(book = offShelfBook("Dune"))

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.editionSheet.shouldBeNull()
        }

        @Test
        fun `builds editionSheet with the primary disabled while the selection is the current edition`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val current = buildEdition(id = 1)
                stateFlow.value = BookDetailUiState(
                    book = offShelfBook("Dune"),
                    previewEdition = current,
                    editions = listOf(current, buildEdition(id = 2)),
                )

                // ----- Act -----
                launchCollector()

                // ----- Assert -----
                stateFlow.value.editionSheet shouldBe editionSheetScaffoldFor(
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
            stateFlow.value = BookDetailUiState(
                book = offShelfBook("Dune"),
                previewEdition = current,
                editions = listOf(current, other),
            )
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(selectedEditionId = other.id)

            // ----- Assert -----
            stateFlow.value.editionSheet shouldBe editionSheetScaffoldFor(
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
            stateFlow.value = BookDetailUiState(
                book = offShelfBook("Dune"),
                previewEdition = current,
                editions = listOf(current, other),
                selectedEditionId = other.id,
            )
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(loadingEditions = true)

            // ----- Assert -----
            stateFlow.value.editionSheet shouldBe editionSheetScaffoldFor(
                bookTitle = "Dune",
                currentEdition = current,
                selectedEdition = other,
                cover = null,
                isLoading = true,
            )
        }

        @Test
        fun `clears editionSheet when the book goes away`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(
                book = offShelfBook("Dune"),
                previewEdition = buildEdition(id = 1),
            )
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = null)

            // ----- Assert -----
            stateFlow.value.editionSheet.shouldBeNull()
        }
    }
}
