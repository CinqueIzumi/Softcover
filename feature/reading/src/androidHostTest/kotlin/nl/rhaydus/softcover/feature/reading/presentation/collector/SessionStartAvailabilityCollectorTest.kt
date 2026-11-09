package nl.rhaydus.softcover.feature.reading.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.presentation.session.ActiveSession
import nl.rhaydus.softcover.core.presentation.session.ActiveSessionController
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SessionStartAvailabilityCollectorTest {
    private lateinit var dependencies: ReadingScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>
    private lateinit var sessionLoaded: MutableStateFlow<Boolean>
    private lateinit var activeSession: MutableStateFlow<ActiveSession?>

    @BeforeEach
    fun setUp() {
        sessionLoaded = MutableStateFlow(false)
        activeSession = MutableStateFlow(null)
        stateFlow = MutableStateFlow(ReadingScreenUiState())

        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ReadingLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        val controller = mockk<ActiveSessionController>(relaxed = true)

        every {
            controller.sessionLoaded
        } returns sessionLoaded

        every {
            controller.activeSession
        } returns activeSession

        dependencies = mockk<ReadingScreenDependencies>(relaxed = true)

        every {
            dependencies.activeSessionController
        } returns controller
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `cannot start while the session has not loaded`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(canStartSession = true)
            sessionLoaded.value = false
            activeSession.value = null

            // ----- Act -----
            val job = launch {
                SessionStartAvailabilityCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.canStartSession shouldBe false
            job.cancel()
        }

        @Test
        fun `can start once loaded with no active session`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                SessionStartAvailabilityCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            sessionLoaded.value = true

            // ----- Assert -----
            stateFlow.value.canStartSession shouldBe true
            job.cancel()
        }

        @Test
        fun `cannot start while a session is active and can again after it ends`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                SessionStartAvailabilityCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            sessionLoaded.value = true

            // ----- Act -----
            activeSession.value = mockk<ActiveSession>()
            val whileActive = stateFlow.value.canStartSession
            activeSession.value = null

            // ----- Assert -----
            whileActive shouldBe false
            stateFlow.value.canStartSession shouldBe true
            job.cancel()
        }
    }
}
