package nl.rhaydus.softcover.feature.explore.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.RefreshTrendingBooksUseCase
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Duration

class OnRefreshActionTest {
    private lateinit var refreshTrendingBooksUseCase: RefreshTrendingBooksUseCase
    private lateinit var refreshTrigger: MutableStateFlow<Long>
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>

    @BeforeEach
    fun setUp() {
        refreshTrendingBooksUseCase = mockk()
        refreshTrigger = MutableStateFlow(0L)
        stateFlow = MutableStateFlow(ExploreScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ExploreLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        coEvery {
            refreshTrendingBooksUseCase(
                period = any(),
                maxAge = any(),
            )
        } returns Result.success(Unit)

        dependencies = mockk<ExploreDependencies>(relaxed = true).also { mock ->
            every {
                mock.refreshTrendingBooksUseCase
            } returns refreshTrendingBooksUseCase

            every {
                mock.continueSeriesRefreshTrigger
            } returns refreshTrigger
        }
    }

    @Nested
    inner class Execute {
        @Test
        fun `forces the trending refresh past the staleness window`() = runTest {
            // ----- Arrange -----
            // Default dependencies from setUp.

            // ----- Act -----
            OnRefreshAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                refreshTrendingBooksUseCase(
                    period = TrendingPeriod.Week,
                    maxAge = Duration.ZERO,
                )
            }
        }

        @Test
        fun `bumps the continue series refresh trigger`() = runTest {
            // ----- Arrange -----
            // Default dependencies from setUp.

            // ----- Act -----
            OnRefreshAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            refreshTrigger.value shouldBe 1L
        }

        @Test
        fun `clears the refreshing flag when the trending refresh fails`() = runTest {
            // ----- Arrange -----
            coEvery {
                refreshTrendingBooksUseCase(
                period = any(),
                maxAge = any(),
            )
            } returns Result.failure(IllegalStateException("offline"))

            // ----- Act -----
            OnRefreshAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isRefreshing shouldBe false
        }
    }
}
