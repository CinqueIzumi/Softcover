package nl.rhaydus.softcover.feature.settings.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.exception.OfflineException
import nl.rhaydus.softcover.feature.settings.domain.usecase.RefreshRoadmapUseCase
import nl.rhaydus.softcover.feature.settings.presentation.event.RoadmapEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.RoadmapDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.RoadmapLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.RoadmapUiState
import nl.rhaydus.toad.ActionScope

private const val FALLBACK_MESSAGE = "Couldn't reach the roadmap. Check your connection and try again."

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
        fun `sets isRefreshing and clears roadmapError before calling the use case`() = runTest {
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
        }

        @Test
        fun `clears isRefreshing after the use case completes`() = runTest {
            // ----- Arrange -----
            coEvery {
                refreshRoadmapUseCase(force = true)
            } returns Result.success(Unit)

            val action = RefreshRoadmapAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isRefreshing shouldBe false
        }

        @Test
        fun `fills roadmapError using the mapped message for a typed api exception`() = runTest {
            // ----- Arrange -----
            coEvery {
                refreshRoadmapUseCase(force = true)
            } returns Result.failure(OfflineException())

            val action = RefreshRoadmapAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.roadmapError shouldBe "You're offline. Check your connection and try again."
        }

        @Test
        fun `fills roadmapError using the fallback message for a generic throwable`() = runTest {
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
            stateFlow.value.roadmapError shouldBe FALLBACK_MESSAGE
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
