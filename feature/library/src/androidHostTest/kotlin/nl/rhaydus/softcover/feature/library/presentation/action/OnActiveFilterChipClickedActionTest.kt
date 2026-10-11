package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.toChipSet
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LIBRARY_CLEAR_ALL_CHIP_KEY
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryActiveFilterChips
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterValue
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilters
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnActiveFilterChipClickedActionTest {
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    private val tabId = "list-10"
    private val otherTabId = "list-99"

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
        fun `the clear-all key clears the tab's filters and leaves other tabs alone`() = runTest {
            // ----- Arrange -----
            val otherFilters = LibraryFilters(releaseYears = persistentSetOf(2021))

            stateFlow.value = LibraryUiState(
                filtersByTab = persistentMapOf(
                    tabId to LibraryFilters(formats = persistentSetOf("ebook")),
                    otherTabId to otherFilters,
                ),
            )

            val action = OnActiveFilterChipClickedAction(
                tabId = tabId,
                key = LIBRARY_CLEAR_ALL_CHIP_KEY,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filtersByTab shouldBe mapOf(otherTabId to otherFilters)
        }

        @Test
        fun `a known chip key toggles the resolved filter value off`() = runTest {
            // ----- Arrange -----
            val value = LibraryFilterValue.Format(value = "ebook")
            val chip = ChipUiModel(
                key = "format:ebook",
                label = "ebook",
            )

            stateFlow.value = LibraryUiState(
                filtersByTab = persistentMapOf(tabId to LibraryFilters(formats = persistentSetOf("ebook"))),
                activeFilterChipsByTab = persistentMapOf(
                    tabId to LibraryActiveFilterChips(chips = listOf(chip to value).toChipSet()),
                ),
            )

            val action = OnActiveFilterChipClickedAction(
                tabId = tabId,
                key = "format:ebook",
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filtersByTab.containsKey(tabId) shouldBe false
        }

        @Test
        fun `a key not present in the tab's active filter chips is a no-op`() = runTest {
            // ----- Arrange -----
            val existing = LibraryFilters(formats = persistentSetOf("ebook"))

            stateFlow.value = LibraryUiState(
                filtersByTab = persistentMapOf(tabId to existing),
                activeFilterChipsByTab = persistentMapOf(),
            )

            val action = OnActiveFilterChipClickedAction(
                tabId = tabId,
                key = "format:unknown",
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filtersByTab[tabId] shouldBe existing
        }
    }
}
