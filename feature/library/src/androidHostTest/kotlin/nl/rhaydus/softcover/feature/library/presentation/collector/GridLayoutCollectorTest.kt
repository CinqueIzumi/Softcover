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
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetLibraryGridLayoutAsFlowUseCase
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
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
        fun `updates gridLayout to the latest value when flow emits multiple times`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = GridLayoutCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            gridLayoutFlow.emit(LibraryGridLayout.LIST_COMPACT)
            gridLayoutFlow.emit(LibraryGridLayout.LIST_LARGE)

            // ----- Assert -----
            stateFlow.value.gridLayout shouldBe LibraryGridLayout.LIST_LARGE
            job.cancel()
        }
    }
}
