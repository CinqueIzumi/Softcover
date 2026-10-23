package nl.rhaydus.softcover.showcase.live.celebration.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.celebration.MarkAsReadBurstScale
import nl.rhaydus.softcover.showcase.live.celebration.event.CelebrationLiveEvent
import nl.rhaydus.softcover.showcase.live.celebration.mapper.celebrationLiveTopBar
import nl.rhaydus.softcover.showcase.live.celebration.mapper.restingBurst
import nl.rhaydus.softcover.showcase.live.celebration.screenmodel.CelebrationLiveDependencies
import nl.rhaydus.softcover.showcase.live.celebration.state.CelebrationLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.celebration.state.CelebrationLiveUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnScreenBurstClickedActionTest {
    private lateinit var dependencies: CelebrationLiveDependencies
    private lateinit var stateFlow: MutableStateFlow<CelebrationLiveUiState>
    private lateinit var eventChannel: Channel<CelebrationLiveEvent>
    private lateinit var scope: ActionScope<CelebrationLiveUiState, CelebrationLiveEvent, CelebrationLiveLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(
            CelebrationLiveUiState(
                topBar = celebrationLiveTopBar(),
                cardBurst = restingBurst(MarkAsReadBurstScale.Card),
                screenBurst = restingBurst(MarkAsReadBurstScale.Screen),
            ),
        )
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(CelebrationLiveLocalVariables()),
            eventChannel = eventChannel,
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `increments the screen burst key and leaves the card burst alone`() = runTest {
            // ----- Arrange -----
            val action = OnScreenBurstClickedAction()
            val before = stateFlow.value

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.screenBurst.triggerKey shouldBe before.screenBurst.triggerKey + 1
            stateFlow.value.cardBurst shouldBe before.cardBurst
        }
    }
}
