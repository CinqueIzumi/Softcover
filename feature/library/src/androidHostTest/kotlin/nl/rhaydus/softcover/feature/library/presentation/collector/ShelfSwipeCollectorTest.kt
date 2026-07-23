package nl.rhaydus.softcover.feature.library.presentation.collector

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
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

class ShelfSwipeCollectorTest {
    private lateinit var getShelfSwipeEnabledAsFlowUseCase: GetShelfSwipeEnabledAsFlowUseCase
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>
    private lateinit var shelfSwipeFlow: MutableSharedFlow<Boolean>

    @BeforeEach
    fun setUp() {
        shelfSwipeFlow = MutableSharedFlow()
        getShelfSwipeEnabledAsFlowUseCase = mockk()
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getShelfSwipeEnabledAsFlowUseCase()
        } returns shelfSwipeFlow

        dependencies = mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.getShelfSwipeEnabledAsFlowUseCase
            } returns getShelfSwipeEnabledAsFlowUseCase
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `updates shelfSwipeEnabled when flow emits a value`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ShelfSwipeCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            shelfSwipeFlow.emit(false)

            // ----- Assert -----
            stateFlow.value.shelfSwipeEnabled shouldBe false
            job.cancel()
        }

        @Test
        fun `updates shelfSwipeEnabled to the latest value when flow emits multiple times`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ShelfSwipeCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            shelfSwipeFlow.emit(false)
            shelfSwipeFlow.emit(true)

            // ----- Assert -----
            stateFlow.value.shelfSwipeEnabled shouldBe true
            job.cancel()
        }

        @Test
        fun `does not change shelfSwipeEnabled before the flow emits`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ShelfSwipeCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act & Assert -----
            stateFlow.value.shelfSwipeEnabled shouldBe LibraryUiState().shelfSwipeEnabled
            job.cancel()
        }

        @Test
        fun `retains the last emitted value after the collector job is cancelled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ShelfSwipeCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            shelfSwipeFlow.emit(false)
            job.cancel()

            // ----- Act & Assert -----
            stateFlow.value.shelfSwipeEnabled shouldBe false
        }

        @Test
        fun `preserves other state fields when updating shelfSwipeEnabled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                isLoading = false,
                isArrangeSheetExpanded = true,
                shelfSwipeEnabled = true,
            )
            val collector = ShelfSwipeCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            shelfSwipeFlow.emit(false)

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe false
            stateFlow.value.isArrangeSheetExpanded shouldBe true
            stateFlow.value.shelfSwipeEnabled shouldBe false
            job.cancel()
        }
    }
}
