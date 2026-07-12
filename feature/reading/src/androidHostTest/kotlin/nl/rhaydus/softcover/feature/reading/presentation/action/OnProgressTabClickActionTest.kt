package nl.rhaydus.softcover.feature.reading.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.designsystem.presentation.model.ProgressSheetTab
import nl.rhaydus.softcover.core.domain.model.ProgressUnit
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

class OnProgressTabClickActionTest {
    private lateinit var dependencies: ReadingScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(ReadingScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ReadingLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(testScope: TestScope): ReadingScreenDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)

        return mockk<ReadingScreenDependencies>(relaxed = true).also { mock ->
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
        fun `updates progressSheetTab to PAGE when PAGE is provided`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(progressSheetTab = ProgressSheetTab.PERCENTAGE)
            dependencies = stubDependencies(this)
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
            dependencies = stubDependencies(this)
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
            dependencies = stubDependencies(this)
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

        @Test
        fun `invokes setLastUsedProgressUnitUseCase with the mapped unit for PAGE tab`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(progressSheetTab = ProgressSheetTab.PERCENTAGE)
            dependencies = stubDependencies(this)
            val action = OnProgressTabClickAction(newTab = ProgressSheetTab.PAGE)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                dependencies.setLastUsedProgressUnitUseCase(unit = ProgressUnit.PAGE)
            }
        }
    }
}
