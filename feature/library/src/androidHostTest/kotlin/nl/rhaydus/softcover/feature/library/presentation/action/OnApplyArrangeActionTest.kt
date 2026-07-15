package nl.rhaydus.softcover.feature.library.presentation.action

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
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetLibraryGridLayoutUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetLibrarySortUseCase
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

class OnApplyArrangeActionTest {
    private lateinit var setLibrarySortUseCase: SetLibrarySortUseCase
    private lateinit var setLibraryGridLayoutUseCase: SetLibraryGridLayoutUseCase
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    private val tabId = "list-10"

    @BeforeEach
    fun setUp() {
        setLibrarySortUseCase = mockk(relaxed = true)
        setLibraryGridLayoutUseCase = mockk(relaxed = true)
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(): LibraryDependencies =
        mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.setLibrarySortUseCase
            } returns setLibrarySortUseCase

            every {
                mock.setLibraryGridLayoutUseCase
            } returns setLibraryGridLayoutUseCase
        }

    @Nested
    inner class Execute {
        @Test
        fun `resets isRearranging regardless of prior state`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(isRearranging = true)

            coEvery {
                setLibrarySortUseCase(
                    tabId = any(),
                    mode = any(),
                    direction = any(),
                )
            } returns Result.success(Unit)

            coEvery {
                setLibraryGridLayoutUseCase(newLayout = any())
            } returns Result.success(Unit)

            val action = OnApplyArrangeAction(
                tabId = tabId,
                sortMode = LibrarySortMode.TITLE,
                sortDirection = SortDirection.ASCENDING,
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )

            // ----- Act -----
            action.execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isRearranging shouldBe false
        }

        @Test
        fun `happy path invokes both use cases with the exact draft values`() = runTest {
            // ----- Arrange -----
            coEvery {
                setLibrarySortUseCase(
                    tabId = any(),
                    mode = any(),
                    direction = any(),
                )
            } returns Result.success(Unit)

            coEvery {
                setLibraryGridLayoutUseCase(newLayout = any())
            } returns Result.success(Unit)

            val action = OnApplyArrangeAction(
                tabId = tabId,
                sortMode = LibrarySortMode.RATING,
                sortDirection = SortDirection.DESCENDING,
                gridLayout = LibraryGridLayout.LIST_LARGE,
            )

            // ----- Act -----
            action.execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                setLibrarySortUseCase(
                    tabId = tabId,
                    mode = LibrarySortMode.RATING,
                    direction = SortDirection.DESCENDING,
                )
            }

            coVerify(exactly = 1) {
                setLibraryGridLayoutUseCase(newLayout = LibraryGridLayout.LIST_LARGE)
            }
        }

        @Test
        fun `sort use case failure is swallowed and does not prevent the layout use case call`() =
            runTest {
                // ----- Arrange -----
                coEvery {
                    setLibrarySortUseCase(
                        tabId = any(),
                        mode = any(),
                        direction = any(),
                    )
                } returns Result.failure(RuntimeException("sort write failed"))

                coEvery {
                    setLibraryGridLayoutUseCase(newLayout = any())
                } returns Result.success(Unit)

                val action = OnApplyArrangeAction(
                    tabId = tabId,
                    sortMode = LibrarySortMode.TITLE,
                    sortDirection = SortDirection.ASCENDING,
                    gridLayout = LibraryGridLayout.GRID_THREE_COLUMNS,
                )

                // ----- Act -----
                // No exception propagates out of execute() — the failure is logged, not rethrown or
                // rolled back.
                action.execute(
                    dependencies = stubDependencies(),
                    scope = scope,
                )

                // ----- Assert -----
                coVerify(exactly = 1) {
                    setLibraryGridLayoutUseCase(newLayout = LibraryGridLayout.GRID_THREE_COLUMNS)
                }

                stateFlow.value.isRearranging shouldBe false
            }

        @Test
        fun `layout use case failure is swallowed and does not undo the already-applied sort`() =
            runTest {
                // ----- Arrange -----
                coEvery {
                    setLibrarySortUseCase(
                        tabId = any(),
                        mode = any(),
                        direction = any(),
                    )
                } returns Result.success(Unit)

                coEvery {
                    setLibraryGridLayoutUseCase(newLayout = any())
                } returns Result.failure(RuntimeException("layout write failed"))

                val action = OnApplyArrangeAction(
                    tabId = tabId,
                    sortMode = LibrarySortMode.AUTHOR,
                    sortDirection = SortDirection.ASCENDING,
                    gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
                )

                // ----- Act -----
                // No exception propagates out of execute() and no compensating call is made — a
                // partial commit (sort applied, layout write failed) is the intended behavior.
                action.execute(
                    dependencies = stubDependencies(),
                    scope = scope,
                )

                // ----- Assert -----
                coVerify(exactly = 1) {
                    setLibrarySortUseCase(
                        tabId = tabId,
                        mode = LibrarySortMode.AUTHOR,
                        direction = SortDirection.ASCENDING,
                    )
                }

                coVerify(exactly = 1) {
                    setLibraryGridLayoutUseCase(newLayout = LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY)
                }
            }
    }
}
