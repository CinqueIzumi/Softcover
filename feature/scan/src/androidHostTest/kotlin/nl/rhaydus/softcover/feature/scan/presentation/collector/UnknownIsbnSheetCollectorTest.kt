package nl.rhaydus.softcover.feature.scan.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.scan.presentation.event.ScanEvent
import nl.rhaydus.softcover.feature.scan.presentation.screenmodel.ScanDependencies
import nl.rhaydus.softcover.feature.scan.presentation.state.LocalScanVariables
import nl.rhaydus.softcover.feature.scan.presentation.state.ScanUiState
import nl.rhaydus.softcover.feature.scan.presentation.state.unknownIsbnSheetScaffoldFor
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UnknownIsbnSheetCollectorTest {
    private lateinit var dependencies: ScanDependencies
    private lateinit var stateFlow: MutableStateFlow<ScanUiState>
    private lateinit var scope: ActionScope<ScanUiState, ScanEvent, LocalScanVariables>

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(ScanUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalScanVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk(relaxed = true)
    }

    private fun TestScope.launchCollector() {
        backgroundScope.launch {
            UnknownIsbnSheetCollector().onLaunch(
                scope = scope,
                dependencies = dependencies,
            )
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `derives the idle scaffold on launch when no book is being added`() = runTest(testDispatcher) {
            // ----- Arrange -----
            stateFlow.update { it.copy(unknownIsbnSheet = unknownIsbnSheetScaffoldFor(isAdding = true)) }

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            val sheet = stateFlow.value.unknownIsbnSheet
            sheet shouldBe unknownIsbnSheetScaffoldFor(isAdding = false)
        }

        @Test
        fun `isAddingBook true gives the adding scaffold`() = runTest(testDispatcher) {
            // ----- Arrange -----
            launchCollector()

            // ----- Act -----
            stateFlow.update { it.copy(isAddingBook = true) }

            // ----- Assert -----
            val sheet = stateFlow.value.unknownIsbnSheet
            sheet shouldBe unknownIsbnSheetScaffoldFor(isAdding = true)
        }

        @Test
        fun `flipping isAddingBook back to false gives the idle scaffold`() = runTest(testDispatcher) {
            // ----- Arrange -----
            launchCollector()
            stateFlow.update { it.copy(isAddingBook = true) }

            // ----- Act -----
            stateFlow.update { it.copy(isAddingBook = false) }

            // ----- Assert -----
            val sheet = stateFlow.value.unknownIsbnSheet
            sheet shouldBe unknownIsbnSheetScaffoldFor(isAdding = false)
        }
    }
}
