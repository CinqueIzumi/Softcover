package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.selection.SelectionMarkBackdrop
import nl.rhaydus.softcover.core.component.selection.SelectionMarkUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SelectionMarksCollectorTest {
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.defaultDispatcher
            } returns testDispatcher
        }
    }

    private fun stubBook(id: Int): Book = mockk<Book>(relaxed = true).also { book ->
        every {
            book.id
        } returns id
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `is empty outside selection mode regardless of the books on the shelf`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val book = stubBook(id = 1)
            val collector = SelectionMarksCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                selectionMode = false,
                selectedBookIds = setOf(1),
                booksByTab = mapOf("all" to listOf(book)),
            )

            // ----- Assert -----
            stateFlow.value.selectionMarks shouldBe emptyMap()
            job.cancel()
        }

        @Test
        fun `marks every book on the shelf, selected or not, once selection mode is on`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val selected = stubBook(id = 1)
            val unselected = stubBook(id = 2)
            val collector = SelectionMarksCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                selectionMode = true,
                selectedBookIds = setOf(1),
                booksByTab = mapOf("all" to listOf(selected, unselected)),
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )

            // ----- Assert -----
            stateFlow.value.selectionMarks shouldBe mapOf(
                1 to SelectionMarkUiModel(
                    selected = true,
                    backdrop = SelectionMarkBackdrop.Image,
                ),
                2 to SelectionMarkUiModel(
                    selected = false,
                    backdrop = SelectionMarkBackdrop.Image,
                ),
            )
            job.cancel()
        }

        @Test
        fun `LIST_COMPACT layout picks the Surface backdrop`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val book = stubBook(id = 1)
            val collector = SelectionMarksCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                selectionMode = true,
                selectedBookIds = emptySet(),
                booksByTab = mapOf("all" to listOf(book)),
                gridLayout = LibraryGridLayout.LIST_COMPACT,
            )

            // ----- Assert -----
            stateFlow.value.selectionMarks.getValue(1).backdrop shouldBe SelectionMarkBackdrop.Surface
            job.cancel()
        }

        @Test
        fun `every cover grid layout picks the Image backdrop`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val book = stubBook(id = 1)
            val collector = SelectionMarksCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                selectionMode = true,
                selectedBookIds = emptySet(),
                booksByTab = mapOf("all" to listOf(book)),
                gridLayout = LibraryGridLayout.LIST_LARGE,
            )

            // ----- Assert -----
            stateFlow.value.selectionMarks.getValue(1).backdrop shouldBe SelectionMarkBackdrop.Image
            job.cancel()
        }

        @Test
        fun `re-emits with updated selected flags when selectedBookIds changes`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val book = stubBook(id = 1)
            val collector = SelectionMarksCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                selectionMode = true,
                selectedBookIds = emptySet(),
                booksByTab = mapOf("all" to listOf(book)),
            )
            val beforeSelection = stateFlow.value.selectionMarks.getValue(1).selected

            stateFlow.value = stateFlow.value.copy(selectedBookIds = setOf(1))
            val afterSelection = stateFlow.value.selectionMarks.getValue(1).selected

            // ----- Assert -----
            beforeSelection shouldBe false
            afterSelection shouldBe true
            job.cancel()
        }

        @Test
        fun `is empty when selection mode is on with no books on any shelf`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val collector = SelectionMarksCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                selectionMode = true,
                selectedBookIds = emptySet(),
                booksByTab = emptyMap(),
            )

            // ----- Assert -----
            stateFlow.value.selectionMarks shouldBe emptyMap()
            job.cancel()
        }
    }
}
