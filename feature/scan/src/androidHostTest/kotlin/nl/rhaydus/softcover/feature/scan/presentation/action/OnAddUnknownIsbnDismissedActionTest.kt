package nl.rhaydus.softcover.feature.scan.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
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

class OnAddUnknownIsbnDismissedActionTest {
    private lateinit var dependencies: ScanDependencies
    private lateinit var stateFlow: MutableStateFlow<ScanUiState>
    private lateinit var eventChannel: Channel<ScanEvent>
    private lateinit var scope: ActionScope<ScanUiState, ScanEvent, LocalScanVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(ScanUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalScanVariables()),
            eventChannel = eventChannel,
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `clears unknownIsbn`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(unknownIsbn = "9780451524935")
            val action = OnAddUnknownIsbnDismissedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.unknownIsbn.shouldBeNull()
        }

        @Test
        fun `leaves state unchanged and sends no event while a book is being added`() = runTest {
            // ----- Arrange -----
            val addingState = ScanUiState(
                unknownIsbn = "9780451524935",
                isAddingBook = true,
            )
            stateFlow.value = addingState
            val action = OnAddUnknownIsbnDismissedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe addingState
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }

        @Test
        fun `does not alter isResolving`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(
                unknownIsbn = "9780451524935",
                isResolving = true,
            )
            val action = OnAddUnknownIsbnDismissedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isResolving shouldBe true
        }

        @Test
        fun `does not send any event`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(unknownIsbn = "9780451524935")
            val action = OnAddUnknownIsbnDismissedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }

        @Test
        fun `is idempotent when unknownIsbn is already null`() = runTest {
            // ----- Arrange -----
            val idleState = ScanUiState(
                unknownIsbn = null,
                isAddingBook = false,
            )
            stateFlow.value = idleState
            val action = OnAddUnknownIsbnDismissedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe idleState
        }
    }
}
