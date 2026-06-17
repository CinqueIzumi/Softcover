package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetLibraryGridLayoutUseCase
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnGridLayoutChangeActionTest {
    private lateinit var setLibraryGridLayoutUseCase: SetLibraryGridLayoutUseCase
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    @BeforeEach
    fun setUp() {
        setLibraryGridLayoutUseCase = mockk()
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(testScope: TestScope): LibraryDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.setLibraryGridLayoutUseCase
            } returns setLibraryGridLayoutUseCase

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
    inner class Execute {
        @Test
        fun `sets isLayoutMenuExpanded to false on success`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            stateFlow.value = LibraryUiState(isLayoutMenuExpanded = true)

            coEvery {
                setLibraryGridLayoutUseCase(newLayout = LibraryGridLayout.GRID_THREE_COLUMNS)
            } returns Result.success(Unit)

            val action = OnGridLayoutChangeAction(newLayout = LibraryGridLayout.GRID_THREE_COLUMNS)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isLayoutMenuExpanded shouldBe false
        }

        @Test
        fun `sets isLayoutMenuExpanded to false even when the use case fails`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            stateFlow.value = LibraryUiState(isLayoutMenuExpanded = true)

            coEvery {
                setLibraryGridLayoutUseCase(newLayout = LibraryGridLayout.LIST_COMPACT)
            } returns Result.failure(RuntimeException("storage error"))

            val action = OnGridLayoutChangeAction(newLayout = LibraryGridLayout.LIST_COMPACT)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isLayoutMenuExpanded shouldBe false
        }

        @Test
        fun `invokes use case with the layout provided to the action constructor`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)

            coEvery {
                setLibraryGridLayoutUseCase(newLayout = LibraryGridLayout.LIST_LARGE)
            } returns Result.success(Unit)

            val action = OnGridLayoutChangeAction(newLayout = LibraryGridLayout.LIST_LARGE)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                setLibraryGridLayoutUseCase(newLayout = LibraryGridLayout.LIST_LARGE)
            }
        }

        @Test
        fun `preserves other state fields when collapsing the layout menu`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            stateFlow.value = LibraryUiState(
                isLoading = false,
                isLayoutMenuExpanded = true,
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )

            coEvery {
                setLibraryGridLayoutUseCase(newLayout = LibraryGridLayout.GRID_THREE_COLUMNS)
            } returns Result.success(Unit)

            val action = OnGridLayoutChangeAction(newLayout = LibraryGridLayout.GRID_THREE_COLUMNS)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe false
            stateFlow.value.isLayoutMenuExpanded shouldBe false
        }
    }
}
