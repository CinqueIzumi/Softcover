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
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetShelfSwipeEnabledAsFlowUseCase
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

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
    }
}
