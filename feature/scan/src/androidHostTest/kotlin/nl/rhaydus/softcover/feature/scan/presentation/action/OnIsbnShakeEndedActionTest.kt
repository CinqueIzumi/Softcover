package nl.rhaydus.softcover.feature.scan.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
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

class OnIsbnShakeEndedActionTest {
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
        fun `stops the shake but keeps the error message`() = runTest {
            // ----- Arrange -----
            val withError = stateFlow.value.isbnField.copy(
                error = TextFieldError(
                    message = "Invalid",
                    shaking = true,
                ),
            )
            stateFlow.value = stateFlow.value.copy(isbnField = withError)

            // ----- Act -----
            OnIsbnShakeEndedAction.execute(
                dependencies = mockk<ScanDependencies>(relaxed = true),
                scope = scope,
            )

            // ----- Assert -----
            val error = stateFlow.value.isbnField.error.shouldNotBeNull()
            error.shaking shouldBe false
            error.message shouldBe "Invalid"
        }

        @Test
        fun `leaves the field without an error untouched`() = runTest {
            // ----- Arrange -----
            val before = stateFlow.value

            // ----- Act -----
            OnIsbnShakeEndedAction.execute(
                dependencies = mockk<ScanDependencies>(relaxed = true),
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isbnField.error.shouldBeNull()
            stateFlow.value shouldBe before
        }
    }
}
