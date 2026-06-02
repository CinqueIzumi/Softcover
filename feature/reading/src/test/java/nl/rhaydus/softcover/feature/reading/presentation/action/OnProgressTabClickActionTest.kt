package nl.rhaydus.softcover.feature.reading.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.designsystem.presentation.model.ProgressSheetTab
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnProgressTabClickActionTest {

    private lateinit var dependencies: ReadingScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(ReadingScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ReadingLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {

        @Test
        fun `updates progressSheetTab to PAGE when PAGE is provided`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(progressSheetTab = ProgressSheetTab.PERCENTAGE)
            val action = OnProgressTabClickAction(newTab = ProgressSheetTab.PAGE)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.progressSheetTab shouldBe ProgressSheetTab.PAGE
        }

        @Test
        fun `updates progressSheetTab to PERCENTAGE when PERCENTAGE is provided`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(progressSheetTab = ProgressSheetTab.PAGE)
            val action = OnProgressTabClickAction(newTab = ProgressSheetTab.PERCENTAGE)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.progressSheetTab shouldBe ProgressSheetTab.PERCENTAGE
        }

        @Test
        fun `preserves all other state fields unchanged`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                isLoading = false,
                showProgressSheet = true,
                progressSheetTab = ProgressSheetTab.PAGE,
            )
            val action = OnProgressTabClickAction(newTab = ProgressSheetTab.PERCENTAGE)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe false
            stateFlow.value.showProgressSheet shouldBe true
        }
    }
}
