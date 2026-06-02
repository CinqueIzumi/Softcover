package nl.rhaydus.softcover.feature.library.presentation.flows

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetLibraryGridLayoutAsFlowUseCase
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GridLayoutCollectorTest {

    private lateinit var getLibraryGridLayoutAsFlowUseCase: GetLibraryGridLayoutAsFlowUseCase
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>
    private lateinit var gridLayoutFlow: MutableSharedFlow<LibraryGridLayout>

    @BeforeEach
    fun setUp() {
        gridLayoutFlow = MutableSharedFlow()
        getLibraryGridLayoutAsFlowUseCase = mockk()
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getLibraryGridLayoutAsFlowUseCase()
        } returns gridLayoutFlow

        dependencies = mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.getLibraryGridLayoutAsFlowUseCase
            } returns getLibraryGridLayoutAsFlowUseCase
        }
    }

    @Nested
    inner class OnLaunch {

        @Test
        fun `updates gridLayout when flow emits a value`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = GridLayoutCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            gridLayoutFlow.emit(LibraryGridLayout.GRID_THREE_COLUMNS)

            // ----- Assert -----
            stateFlow.value.gridLayout shouldBe LibraryGridLayout.GRID_THREE_COLUMNS
            job.cancel()
        }

        @Test
        fun `updates gridLayout to the latest value when flow emits multiple times`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = GridLayoutCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            gridLayoutFlow.emit(LibraryGridLayout.LIST_COMPACT)
            gridLayoutFlow.emit(LibraryGridLayout.LIST_LARGE)

            // ----- Assert -----
            stateFlow.value.gridLayout shouldBe LibraryGridLayout.LIST_LARGE
            job.cancel()
        }

        @Test
        fun `does not change gridLayout before the flow emits`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = GridLayoutCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act & Assert -----
            stateFlow.value.gridLayout shouldBe LibraryGridLayout.GRID_TWO_COLUMNS
            job.cancel()
        }

        @Test
        fun `retains the last emitted layout after the collector job is cancelled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = GridLayoutCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }
            gridLayoutFlow.emit(LibraryGridLayout.LIST_COMPACT)
            job.cancel()

            // ----- Act & Assert -----
            stateFlow.value.gridLayout shouldBe LibraryGridLayout.LIST_COMPACT
        }

        @Test
        fun `preserves other state fields when updating gridLayout`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                isLoading = false,
                isLayoutMenuExpanded = true,
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )
            val collector = GridLayoutCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            gridLayoutFlow.emit(LibraryGridLayout.GRID_THREE_COLUMNS)

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe false
            stateFlow.value.isLayoutMenuExpanded shouldBe true
            stateFlow.value.gridLayout shouldBe LibraryGridLayout.GRID_THREE_COLUMNS
            job.cancel()
        }
    }
}
