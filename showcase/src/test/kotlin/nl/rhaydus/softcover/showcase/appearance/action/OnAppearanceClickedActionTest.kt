package nl.rhaydus.softcover.showcase.appearance.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.showcase.appearance.mapper.appearanceSheet
import nl.rhaydus.softcover.showcase.family.event.FamilyEvent
import nl.rhaydus.softcover.showcase.family.screenmodel.FamilyDependencies
import nl.rhaydus.softcover.showcase.family.state.FamilyLocalVariables
import nl.rhaydus.softcover.showcase.family.state.FamilyUiState
import nl.rhaydus.softcover.showcase.store.ShowcaseThemeStore
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnAppearanceClickedActionTest {
    private lateinit var dependencies: FamilyDependencies
    private lateinit var stateFlow: MutableStateFlow<FamilyUiState>
    private lateinit var scope: ActionScope<FamilyUiState, FamilyEvent, FamilyLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = FamilyDependencies(
            themeStore = ShowcaseThemeStore(),
            coroutineScope = mockk(),
            mainDispatcher = mockk(),
        )
        stateFlow = MutableStateFlow(
            FamilyUiState(
                topBar = mockk(),
                entries = persistentListOf(),
            ),
        )
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(FamilyLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `opens the appearance sheet when it is closed`() = runTest {
            // ----- Arrange -----
            val action = OnAppearanceClickedAction<FamilyDependencies, FamilyUiState, FamilyEvent, FamilyLocalVariables>()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.appearance.sheet shouldBe SheetSession.Open(model = appearanceSheet())
        }
    }
}
