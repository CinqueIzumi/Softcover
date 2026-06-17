package nl.rhaydus.softcover.feature.scan.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
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

class OnAddUnknownIsbnDismissedActionTest {
    private lateinit var stateFlow: MutableStateFlow<ScanUiState>
    private lateinit var eventChannel: Channel<ScanEvent>
    private lateinit var scope: ActionScope<ScanUiState, ScanEvent, LocalScanVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(ScanUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalScanVariables()),
            eventChannel = eventChannel,
        )
    }

    private fun stubDependencies(testScope: kotlinx.coroutines.test.TestScope): ScanDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)

        return mockk<ScanDependencies>(relaxed = true).also { mock ->
            every {
                mock.coroutineScope
            } returns testScope

            every {
                mock.mainDispatcher
            } returns dispatcher

            every {
                mock.launch(any())
            } answers { callOriginal() }
        }
    }

    @Nested
    inner class Execute {
        @Test
        fun `clears unknownIsbn`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(unknownIsbn = "9780451524935")
            val dependencies = stubDependencies(this)
            val action = OnAddUnknownIsbnDismissedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.unknownIsbn shouldBe null
        }

        @Test
        fun `sets isAddingBook to false`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(
                unknownIsbn = "9780451524935",
                isAddingBook = true,
            )
            val dependencies = stubDependencies(this)
            val action = OnAddUnknownIsbnDismissedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isAddingBook shouldBe false
        }

        @Test
        fun `does not alter isResolving`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(
                unknownIsbn = "9780451524935",
                isResolving = true,
            )
            val dependencies = stubDependencies(this)
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
            val dependencies = stubDependencies(this)
            val action = OnAddUnknownIsbnDismissedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val event = eventChannel.tryReceive().getOrNull()
            event shouldBe null
        }

        @Test
        fun `is idempotent when unknownIsbn is already null`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(
                unknownIsbn = null,
                isAddingBook = false,
            )
            val dependencies = stubDependencies(this)
            val action = OnAddUnknownIsbnDismissedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe ScanUiState(
                unknownIsbn = null,
                isAddingBook = false,
            )
        }
    }
}
