package nl.rhaydus.softcover.showcase.home.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.showcase.appearance.mapper.appearanceSheet
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceState
import nl.rhaydus.softcover.showcase.home.event.HomeEvent
import nl.rhaydus.softcover.showcase.home.screenmodel.HomeDependencies
import nl.rhaydus.softcover.showcase.home.state.HomeLocalVariables
import nl.rhaydus.softcover.showcase.home.state.HomeUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnAppearanceDismissedActionTest {
    private lateinit var dependencies: HomeDependencies
    private lateinit var stateFlow: MutableStateFlow<HomeUiState>
    private lateinit var eventChannel: Channel<HomeEvent>
    private lateinit var scope: ActionScope<HomeUiState, HomeEvent, HomeLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(HomeUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(HomeLocalVariables()),
            eventChannel = eventChannel,
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `closes the open appearance sheet`() = runTest {
            // ----- Arrange -----
            stateFlow.value = HomeUiState(appearance = AppearanceState(sheet = SheetSession.Open(model = appearanceSheet())))
            val action = OnAppearanceDismissedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.appearance.sheet shouldBe SheetSession.Closed
        }
    }
}
