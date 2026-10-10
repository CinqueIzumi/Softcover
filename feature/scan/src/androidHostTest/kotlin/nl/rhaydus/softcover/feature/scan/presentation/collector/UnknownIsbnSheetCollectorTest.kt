package nl.rhaydus.softcover.feature.scan.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
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
import nl.rhaydus.softcover.feature.scan.presentation.state.UnknownIsbnSheetUiModel
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

    private fun sheet(isbn: String = ISBN) = UnknownIsbnSheetUiModel(
        isbn = isbn,
        scaffold = unknownIsbnSheetScaffoldFor(isAdding = false),
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `follows isAddingBook between the adding and idle scaffold`() = runTest(testDispatcher) {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(unknownIsbnSheet = sheet())
            launchCollector()

            // ----- Act -----
            stateFlow.update { it.copy(isAddingBook = true) }
            val whileAdding = stateFlow.value.unknownIsbnSheet
            stateFlow.update { it.copy(isAddingBook = false) }

            // ----- Assert -----
            whileAdding.shouldNotBeNull().scaffold shouldBe unknownIsbnSheetScaffoldFor(isAdding = true)
            stateFlow.value.unknownIsbnSheet.shouldNotBeNull().scaffold shouldBe
                unknownIsbnSheetScaffoldFor(isAdding = false)
        }

        @Test
        fun `keeps the isbn when it rebuilds the scaffold`() = runTest(testDispatcher) {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(unknownIsbnSheet = sheet())
            launchCollector()

            // ----- Act -----
            stateFlow.update { it.copy(isAddingBook = true) }

            // ----- Assert -----
            stateFlow.value.unknownIsbnSheet.shouldNotBeNull().isbn shouldBe ISBN
        }

        @Test
        fun `leaves a null sheet untouched`() = runTest(testDispatcher) {
            // ----- Arrange -----
            launchCollector()

            // ----- Act -----
            stateFlow.update { it.copy(isAddingBook = true) }
            stateFlow.update { it.copy(isUnknownIsbnCloseRequested = true) }

            // ----- Assert -----
            stateFlow.value.unknownIsbnSheet.shouldBeNull()
        }

        @Test
        fun `a close request flags the scaffold and keeps the adding footer instead of flashing idle`() =
            runTest(testDispatcher) {
                // ----- Arrange -----
                stateFlow.value = ScanUiState(unknownIsbnSheet = sheet())
                launchCollector()
                stateFlow.update { it.copy(isAddingBook = true) }

                // ----- Act -----
                stateFlow.update {
                    it.copy(
                        isAddingBook = false,
                        isUnknownIsbnCloseRequested = true,
                    )
                }

                // ----- Assert -----
                stateFlow.value.unknownIsbnSheet.shouldNotBeNull().scaffold shouldBe
                    unknownIsbnSheetScaffoldFor(isAdding = true).copy(closeRequested = true)
            }

        @Test
        fun `clearing the close request rebuilds the scaffold from the current state`() = runTest(testDispatcher) {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(unknownIsbnSheet = sheet())
            launchCollector()
            stateFlow.update { it.copy(isUnknownIsbnCloseRequested = true) }

            // ----- Act -----
            stateFlow.update { it.copy(isUnknownIsbnCloseRequested = false) }

            // ----- Assert -----
            stateFlow.value.unknownIsbnSheet.shouldNotBeNull().scaffold shouldBe
                unknownIsbnSheetScaffoldFor(isAdding = false)
        }
    }

    private companion object {
        const val ISBN = "9780451524935"
    }
}
