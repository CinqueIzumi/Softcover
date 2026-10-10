package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.selectionCountOf
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SelectionCountCollectorTest {
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk(relaxed = true)
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `writes the size of the initial selection`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(selectedBookIds = setOf(1, 2, 3))
            val collector = SelectionCountCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.selectionCount shouldBe selectionCountOf(count = 3)
            job.cancel()
        }

        @Test
        fun `follows the selection as it grows and clears`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = SelectionCountCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            stateFlow.value.selectionCount shouldBe selectionCountOf(count = 0)

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(selectedBookIds = setOf(4, 5))
            val afterSelecting = stateFlow.value.selectionCount
            stateFlow.value = stateFlow.value.copy(selectedBookIds = emptySet())

            // ----- Assert -----
            afterSelecting shouldBe selectionCountOf(count = 2)
            stateFlow.value.selectionCount shouldBe selectionCountOf(count = 0)
            job.cancel()
        }
    }
}
