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
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class IsbnFieldCollectorTest {
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
            IsbnFieldCollector().onLaunch(
                scope = scope,
                dependencies = dependencies,
            )
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `leaves the field enabled when nothing is in flight`() = runTest(testDispatcher) {
            // ----- Arrange & Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.isbnField.enabled shouldBe true
        }

        @Test
        fun `disables the field while an ISBN is resolving`() = runTest(testDispatcher) {
            // ----- Arrange -----
            launchCollector()

            // ----- Act -----
            stateFlow.update { it.copy(isResolving = true) }

            // ----- Assert -----
            stateFlow.value.isbnField.enabled shouldBe false
        }

        @Test
        fun `disables the field while a book is being added`() = runTest(testDispatcher) {
            // ----- Arrange -----
            launchCollector()

            // ----- Act -----
            stateFlow.update { it.copy(isAddingBook = true) }

            // ----- Assert -----
            stateFlow.value.isbnField.enabled shouldBe false
        }

        @Test
        fun `keeps the field disabled while only one of the two flags clears`() = runTest(testDispatcher) {
            // ----- Arrange -----
            launchCollector()
            stateFlow.update {
                it.copy(
                    isResolving = true,
                    isAddingBook = true,
                )
            }

            // ----- Act -----
            stateFlow.update { it.copy(isResolving = false) }

            // ----- Assert -----
            stateFlow.value.isbnField.enabled shouldBe false
        }

        @Test
        fun `re-enables the field once the lookup finishes`() = runTest(testDispatcher) {
            // ----- Arrange -----
            launchCollector()
            stateFlow.update { it.copy(isResolving = true) }

            // ----- Act -----
            stateFlow.update { it.copy(isResolving = false) }

            // ----- Assert -----
            stateFlow.value.isbnField.enabled shouldBe true
        }

        @Test
        fun `preserves the typed text and focus request while toggling enabled`() = runTest(testDispatcher) {
            // ----- Arrange -----
            launchCollector()
            stateFlow.update {
                it.copy(
                    isbnField = it.isbnField.copy(
                        text = "9780374710781",
                        autoFocus = true,
                    ),
                )
            }

            // ----- Act -----
            stateFlow.update { it.copy(isResolving = true) }

            // ----- Assert -----
            stateFlow.value.isbnField.text shouldBe "9780374710781"
            stateFlow.value.isbnField.autoFocus shouldBe true
        }
    }
}
