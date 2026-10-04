package nl.rhaydus.softcover.feature.scan.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.scan.presentation.event.ScanEvent
import nl.rhaydus.softcover.feature.scan.presentation.screenmodel.ScanDependencies
import nl.rhaydus.softcover.feature.scan.presentation.state.LocalScanVariables
import nl.rhaydus.softcover.feature.scan.presentation.state.ScanUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnIsbnChangedActionTest {
    private lateinit var dependencies: ScanDependencies
    private lateinit var stateFlow: MutableStateFlow<ScanUiState>
    private lateinit var scope: ActionScope<ScanUiState, ScanEvent, LocalScanVariables>

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

    @Nested
    inner class Execute {
        @Test
        fun `writes the typed text into the isbn field`() = runTest {
            // ----- Arrange -----
            val action = OnIsbnChangedAction(text = "978037")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isbnField.text shouldBe "978037"
        }

        @Test
        fun `clears the field when the text is empty`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState().let { it.copy(isbnField = it.isbnField.copy(text = "978037")) }
            val action = OnIsbnChangedAction(text = "")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isbnField.text shouldBe ""
        }

        @Test
        fun `keeps the other field properties and state when the text changes`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState().let {
                it.copy(
                    isbnField = it.isbnField.copy(
                        enabled = false,
                        autoFocus = true,
                    ),
                    isResolving = true,
                )
            }
            val action = OnIsbnChangedAction(text = "1")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isbnField.enabled shouldBe false
            stateFlow.value.isbnField.autoFocus shouldBe true
            stateFlow.value.isResolving shouldBe true
        }
    }
}
