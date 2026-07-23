package nl.rhaydus.softcover.feature.settings.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetShelfSwipeEnabledAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.toad.ActionScope

class ShelfSwipeCollectorTest {
    private lateinit var getShelfSwipeEnabledAsFlowUseCase: GetShelfSwipeEnabledAsFlowUseCase
    private lateinit var dependencies: SettingsScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<SettingsScreenUiState>
    private lateinit var scope: ActionScope<SettingsScreenUiState, SettingsScreenEvent, SettingsLocalVariables>
    private lateinit var shelfSwipeEnabledFlow: MutableSharedFlow<Boolean>

    @BeforeEach
    fun setUp() {
        shelfSwipeEnabledFlow = MutableSharedFlow()
        getShelfSwipeEnabledAsFlowUseCase = mockk()
        stateFlow = MutableStateFlow(SettingsScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(SettingsLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getShelfSwipeEnabledAsFlowUseCase()
        } returns shelfSwipeEnabledFlow

        dependencies = mockk<SettingsScreenDependencies>(relaxed = true).also { mock ->
            every {
                mock.getShelfSwipeEnabledAsFlowUseCase
            } returns getShelfSwipeEnabledAsFlowUseCase
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `sets shelfSwipeEnabledChecked to true when flow emits true`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ShelfSwipeCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            shelfSwipeEnabledFlow.emit(true)

            // ----- Assert -----
            stateFlow.value.shelfSwipeEnabledChecked shouldBe true
            job.cancel()
        }

        @Test
        fun `sets shelfSwipeEnabledChecked to false when flow emits false`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ShelfSwipeCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            shelfSwipeEnabledFlow.emit(false)

            // ----- Assert -----
            stateFlow.value.shelfSwipeEnabledChecked shouldBe false
            job.cancel()
        }

        @Test
        fun `updates shelfSwipeEnabledChecked to the latest value when flow emits multiple times`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ShelfSwipeCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            shelfSwipeEnabledFlow.emit(false)
            shelfSwipeEnabledFlow.emit(true)

            // ----- Assert -----
            stateFlow.value.shelfSwipeEnabledChecked shouldBe true
            job.cancel()
        }

        @Test
        fun `does not change shelfSwipeEnabledChecked before the flow emits`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ShelfSwipeCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act & Assert -----
            stateFlow.value.shelfSwipeEnabledChecked shouldBe false
            job.cancel()
        }

        @Test
        fun `preserves other state fields when updating shelfSwipeEnabledChecked`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = SettingsScreenUiState(
                shelfSwipeEnabledChecked = true,
                dropDownExpanded = true,
            )
            val collector = ShelfSwipeCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            shelfSwipeEnabledFlow.emit(false)

            // ----- Assert -----
            stateFlow.value.dropDownExpanded shouldBe true
            stateFlow.value.shelfSwipeEnabledChecked shouldBe false
            job.cancel()
        }

        @Test
        fun `retains last emitted shelfSwipeEnabledChecked after the collector job is cancelled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ShelfSwipeCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            shelfSwipeEnabledFlow.emit(false)
            job.cancel()

            // ----- Act & Assert -----
            stateFlow.value.shelfSwipeEnabledChecked shouldBe false
        }
    }
}
