package nl.rhaydus.softcover.feature.settings.presentation.collector

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
import nl.rhaydus.softcover.feature.settings.domain.usecase.RefreshRoadmapUseCase
import nl.rhaydus.softcover.feature.settings.presentation.event.RoadmapEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.RoadmapDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.RoadmapLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.RoadmapUiState
import nl.rhaydus.toad.ActionScope

class RoadmapRefreshCollectorTest {
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
    inner class OnLaunch {
        @Test
        fun `calls the use case without bypassing the cache`() = runTest {
            // ----- Arrange -----
            coEvery {
                refreshRoadmapUseCase(force = false)
            } returns Result.success(Unit)

            val collector = RoadmapRefreshCollector()

            // ----- Act -----
            collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            )

            // ----- Assert -----
            coVerify {
                refreshRoadmapUseCase(force = false)
            }
        }

        @Test
        fun `does not fill roadmapError when the use case fails`() = runTest {
            // ----- Arrange -----
            coEvery {
                refreshRoadmapUseCase(force = false)
            } returns Result.failure(RuntimeException("network error"))

            val collector = RoadmapRefreshCollector()

            // ----- Act -----
            collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            )

            // ----- Assert -----
            stateFlow.value.roadmapError shouldBe null
        }

        @Test
        fun `does not throw when the use case fails`() = runTest {
            // ----- Arrange -----
            coEvery {
                refreshRoadmapUseCase(force = false)
            } returns Result.failure(RuntimeException("network error"))

            val collector = RoadmapRefreshCollector()

            // ----- Act -----
            val result = runCatching {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            result.isSuccess shouldBe true
        }
    }
}
