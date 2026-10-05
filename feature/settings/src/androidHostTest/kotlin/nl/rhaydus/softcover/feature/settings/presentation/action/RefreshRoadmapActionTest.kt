package nl.rhaydus.softcover.feature.settings.presentation.action

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.settings.domain.usecase.RefreshRoadmapUseCase
import nl.rhaydus.softcover.feature.settings.presentation.event.RoadmapEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.RoadmapDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.RoadmapLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.RoadmapUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RefreshRoadmapActionTest {
    private lateinit var refreshRoadmapUseCase: RefreshRoadmapUseCase
    private lateinit var dependencies: RoadmapDependencies
    private lateinit var stateFlow: MutableStateFlow<RoadmapUiState>
    private lateinit var scope: ActionScope<RoadmapUiState, RoadmapEvent, RoadmapLocalVariables>

    @BeforeEach
    fun setUp() {
        refreshRoadmapUseCase = mockk()
        stateFlow = MutableStateFlow(RoadmapUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(RoadmapLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        dependencies = mockk<RoadmapDependencies>(relaxed = true).also { mock ->
            every {
                mock.refreshRoadmapUseCase
            } returns refreshRoadmapUseCase
        }
    }

    @Nested
    inner class Execute {
        @Test
        fun `shows the refreshing state with no error during the call and clears it afterwards`() = runTest {
            // ----- Arrange -----
            stateFlow.value = RoadmapUiState(roadmapError = "stale error")
            var isRefreshingDuringCall = false
            var errorDuringCall: String? = "unset"

            coEvery {
                refreshRoadmapUseCase(force = true)
            } coAnswers {
                isRefreshingDuringCall = scope.currentState.isRefreshing
                errorDuringCall = scope.currentState.roadmapError
                Result.success(Unit)
            }

            val action = RefreshRoadmapAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            isRefreshingDuringCall shouldBe true
            errorDuringCall shouldBe null
            stateFlow.value.isRefreshing shouldBe false
        }

        @Test
        fun `surfaces an error and stops refreshing when the use case fails`() = runTest {
            // ----- Arrange -----
            coEvery {
                refreshRoadmapUseCase(force = true)
            } returns Result.failure(RuntimeException("boom"))

            val action = RefreshRoadmapAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.roadmapError.shouldNotBeNull()
            stateFlow.value.isRefreshing shouldBe false
        }

        @Test
        fun `does not dispatch a refresh when already refreshing`() = runTest {
            // ----- Arrange -----
            val initialState = RoadmapUiState(isRefreshing = true)
            stateFlow.value = initialState

            val action = RefreshRoadmapAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                refreshRoadmapUseCase(force = any())
            }
            stateFlow.value shouldBe initialState
        }
    }
}
