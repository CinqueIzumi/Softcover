package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnSortModeChangeActionTest {

    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    private val tabId = "reading"

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {

        @Test
        fun `tapping active mode flips direction from ASCENDING to DESCENDING`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                sortModeByTab = mapOf(tabId to LibrarySortMode.TITLE),
                sortDirectionByTab = mapOf(tabId to SortDirection.ASCENDING),
            )

            val action = OnSortModeChangeAction(
                tabId = tabId,
                mode = LibrarySortMode.TITLE,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                dependencies.setLibrarySortUseCase(
                    tabId = tabId,
                    mode = LibrarySortMode.TITLE,
                    direction = SortDirection.DESCENDING,
                )
            }
        }

        @Test
        fun `tapping a different mode resets direction to that mode's defaultDirection`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                sortModeByTab = mapOf(tabId to LibrarySortMode.TITLE),
                sortDirectionByTab = mapOf(tabId to SortDirection.DESCENDING),
            )

            val action = OnSortModeChangeAction(
                tabId = tabId,
                mode = LibrarySortMode.DATE_ADDED,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                dependencies.setLibrarySortUseCase(
                    tabId = tabId,
                    mode = LibrarySortMode.DATE_ADDED,
                    direction = SortDirection.DESCENDING,
                )
            }
        }

        @Test
        fun `collapses the sort menu regardless of which mode is tapped`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(isSortMenuExpanded = true)

            val action = OnSortModeChangeAction(
                tabId = tabId,
                mode = LibrarySortMode.TITLE,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isSortMenuExpanded shouldBe false
        }

        @Test
        fun `resets isRearranging to false when sort mode changes`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(isRearranging = true)

            val action = OnSortModeChangeAction(
                tabId = tabId,
                mode = LibrarySortMode.TITLE,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isRearranging shouldBe false
        }
    }
}
