package nl.rhaydus.softcover.feature.profile.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetHideUntaggedAuthorsAsFlowUseCase
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.softcover.feature.profile.presentation.state.hideUntaggedAuthorsToggleFor
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HideUntaggedAuthorsCollectorTest {
    private lateinit var getHideUntaggedAuthorsAsFlowUseCase: GetHideUntaggedAuthorsAsFlowUseCase
    private lateinit var stateFlow: MutableStateFlow<StatsAtlasUiState>
    private lateinit var eventChannel: Channel<StatsAtlasEvent>
    private lateinit var scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>
    private lateinit var hideUntaggedAuthorsFlow: MutableSharedFlow<Boolean>

    @BeforeEach
    fun setUp() {
        hideUntaggedAuthorsFlow = MutableSharedFlow()
        getHideUntaggedAuthorsAsFlowUseCase = mockk()
        stateFlow = MutableStateFlow(StatsAtlasUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(StatsAtlasLocalVariables()),
            eventChannel = eventChannel,
        )

        every {
            getHideUntaggedAuthorsAsFlowUseCase()
        } returns hideUntaggedAuthorsFlow
    }

    private fun stubDependencies(testScope: TestScope): StatsAtlasDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<StatsAtlasDependencies>(relaxed = true).also { mock ->
            every {
                mock.getHideUntaggedAuthorsAsFlowUseCase
            } returns getHideUntaggedAuthorsAsFlowUseCase

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
    inner class OnLaunch {
        @Test
        fun `rebuilds the toggle model to match the preference`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val collector = HideUntaggedAuthorsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            hideUntaggedAuthorsFlow.emit(true)

            // ----- Assert -----
            stateFlow.value.hideUntaggedAuthorsToggle shouldBe hideUntaggedAuthorsToggleFor(hide = true)
            job.cancel()
        }

        @Test
        fun `tracks the latest emission when the preference emits more than once`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val collector = HideUntaggedAuthorsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            hideUntaggedAuthorsFlow.emit(true)
            hideUntaggedAuthorsFlow.emit(false)

            // ----- Assert -----
            stateFlow.value.hideUntaggedAuthors shouldBe false
            job.cancel()
        }
    }
}
