package nl.rhaydus.softcover.feature.scan.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.field.TextFieldError
import nl.rhaydus.softcover.feature.scan.presentation.event.ScanEvent
import nl.rhaydus.softcover.feature.scan.presentation.screenmodel.ScanDependencies
import nl.rhaydus.softcover.feature.scan.presentation.state.LocalScanVariables
import nl.rhaydus.softcover.feature.scan.presentation.state.ScanUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnIsbnChangedActionTest {
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
    }

    @Nested
    inner class Execute {
        @Test
        fun `writes the typed text into the field`() = runTest {
            // ----- Arrange -----
            val action = OnIsbnChangedAction(text = "9780451")

            // ----- Act -----
            action.execute(
                dependencies = mockk<ScanDependencies>(relaxed = true),
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isbnField.text shouldBe "9780451"
        }

        @Test
        fun `clears an existing field error when the text changes`() = runTest {
            // ----- Arrange -----
            val withError = stateFlow.value.isbnField.copy(error = TextFieldError(message = "Invalid"))
            stateFlow.value = stateFlow.value.copy(isbnField = withError)
            val action = OnIsbnChangedAction(text = "97804515")

            // ----- Act -----
            action.execute(
                dependencies = mockk<ScanDependencies>(relaxed = true),
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isbnField.error.shouldBeNull()
            stateFlow.value.isbnField.text shouldBe "97804515"
        }
    }
}
