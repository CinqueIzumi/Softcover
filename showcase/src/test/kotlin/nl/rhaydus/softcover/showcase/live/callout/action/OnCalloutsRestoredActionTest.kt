package nl.rhaydus.softcover.showcase.live.callout.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.showcase.live.callout.event.CalloutLiveEvent
import nl.rhaydus.softcover.showcase.live.callout.mapper.calloutLiveTopBar
import nl.rhaydus.softcover.showcase.live.callout.mapper.liveBanners
import nl.rhaydus.softcover.showcase.live.callout.mapper.liveCallouts
import nl.rhaydus.softcover.showcase.live.callout.screenmodel.CalloutLiveDependencies
import nl.rhaydus.softcover.showcase.live.callout.state.CalloutLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.callout.state.CalloutLiveUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnCalloutsRestoredActionTest {
    private lateinit var dependencies: CalloutLiveDependencies
    private lateinit var stateFlow: MutableStateFlow<CalloutLiveUiState>
    private lateinit var eventChannel: Channel<CalloutLiveEvent>
    private lateinit var scope: ActionScope<CalloutLiveUiState, CalloutLiveEvent, CalloutLiveLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(
            CalloutLiveUiState(
                topBar = calloutLiveTopBar(),
                banners = liveBanners(),
                callouts = liveCallouts(),
            ),
        )
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(CalloutLiveLocalVariables()),
            eventChannel = eventChannel,
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `brings back both the banners and the callouts`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(
                banners = persistentListOf(),
                callouts = persistentListOf(),
            )
            val action = OnCalloutsRestoredAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.banners shouldBe liveBanners()
            stateFlow.value.callouts shouldBe liveCallouts()
        }
    }
}
