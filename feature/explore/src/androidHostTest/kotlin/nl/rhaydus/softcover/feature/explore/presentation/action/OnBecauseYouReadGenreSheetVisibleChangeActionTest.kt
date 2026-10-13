package nl.rhaydus.softcover.feature.explore.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnBecauseYouReadGenreSheetVisibleChangeActionTest {
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(ExploreScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ExploreLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `opens the sheet and leaves every other state field untouched`() = runTest {
            // ----- Arrange -----
            val stateBefore = stateFlow.value
            val action = OnBecauseYouReadGenreSheetVisibleChangeAction(visible = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe stateBefore.copy(
                becauseYouReadGenreControl = stateBefore.becauseYouReadGenreControl.copy(sheetVisible = true),
            )
        }

        @Test
        fun `closes a visible sheet and keeps the rest of the control`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(
                becauseYouReadGenreControl = stateFlow.value.becauseYouReadGenreControl.copy(
                    label = "GENRE · HORROR",
                    visible = true,
                    sheetVisible = true,
                ),
            )
            val stateBefore = stateFlow.value
            val action = OnBecauseYouReadGenreSheetVisibleChangeAction(visible = false)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe stateBefore.copy(
                becauseYouReadGenreControl = stateBefore.becauseYouReadGenreControl.copy(sheetVisible = false),
            )
        }
    }
}
