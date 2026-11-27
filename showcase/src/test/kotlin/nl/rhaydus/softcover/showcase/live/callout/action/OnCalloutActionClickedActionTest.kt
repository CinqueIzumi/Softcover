package nl.rhaydus.softcover.showcase.live.callout.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.callout.CalloutActionUiModel
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

class OnCalloutActionClickedActionTest {
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

    private fun detailOf(callout: nl.rhaydus.softcover.core.component.callout.CalloutUiModel) =
        callout.callToAction as CalloutActionUiModel.Detail

    @Nested
    inner class Execute {
        @Test
        fun `empties the lines of a filled detail callout`() = runTest {
            // ----- Arrange -----
            val callout = stateFlow.value.callouts.single { it.callToAction is CalloutActionUiModel.Detail }
            val action = OnCalloutActionClickedAction(callout = callout)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            detailOf(stateFlow.value.callouts.single { it.eyebrow == callout.eyebrow }).lines shouldBe persistentListOf()
        }

        @Test
        fun `fills the lines of an empty detail callout`() = runTest {
            // ----- Arrange -----
            val filled = stateFlow.value.callouts.single { it.callToAction is CalloutActionUiModel.Detail }
            val empty = filled.copy(callToAction = detailOf(filled).copy(lines = persistentListOf()))

            OnCalloutActionClickedAction(callout = filled).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Act -----
            OnCalloutActionClickedAction(callout = empty).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            detailOf(stateFlow.value.callouts.single { it.eyebrow == filled.eyebrow }).lines shouldBe
                detailOf(filled).lines
        }

        @Test
        fun `leaves state unchanged for a callout that is not a detail`() = runTest {
            // ----- Arrange -----
            val before = stateFlow.value
            val callout = before.callouts.first { it.callToAction !is CalloutActionUiModel.Detail }
            val action = OnCalloutActionClickedAction(callout = callout)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe before
        }
    }
}
