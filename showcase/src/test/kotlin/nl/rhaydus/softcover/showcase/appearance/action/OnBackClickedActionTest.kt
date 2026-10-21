package nl.rhaydus.softcover.showcase.appearance.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.showcase.family.event.FamilyEvent
import nl.rhaydus.softcover.showcase.family.screenmodel.FamilyDependencies
import nl.rhaydus.softcover.showcase.family.state.FamilyLocalVariables
import nl.rhaydus.softcover.showcase.family.state.FamilyUiState
import nl.rhaydus.softcover.showcase.store.ShowcaseThemeStore
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnBackClickedActionTest {
    private lateinit var dependencies: FamilyDependencies
    private lateinit var eventChannel: Channel<FamilyEvent>
    private lateinit var scope: ActionScope<FamilyUiState, FamilyEvent, FamilyLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = FamilyDependencies(
            themeStore = ShowcaseThemeStore(),
            coroutineScope = mockk(),
            mainDispatcher = mockk(),
        )
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = MutableStateFlow(
                FamilyUiState(
                    topBar = mockk(),
                    entries = persistentListOf(),
                ),
            ),
            localVariablesFlow = MutableStateFlow(FamilyLocalVariables()),
            eventChannel = eventChannel,
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `sends the back event it was given`() = runTest {
            // ----- Arrange -----
            val action = OnBackClickedAction<FamilyDependencies, FamilyUiState, FamilyEvent, FamilyLocalVariables>(
                backEvent = FamilyEvent.NavigateBack,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull() shouldBe FamilyEvent.NavigateBack
        }
    }
}
