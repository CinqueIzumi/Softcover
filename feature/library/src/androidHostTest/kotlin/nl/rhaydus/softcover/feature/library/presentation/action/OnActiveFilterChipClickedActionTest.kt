package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
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
        fun `the clear-all key clears the tab's filters, same as OnClearFiltersAction`() = runTest {
            // ----- Arrange -----
            val activeFilters = LibraryFilters(formats = setOf("ebook"))

            stateFlow.value = LibraryUiState(filtersByTab = mapOf(tabId to activeFilters))

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
            stateFlow.value.filtersByTab.containsKey(tabId) shouldBe false
        }

        @Test
        fun `the clear-all key does not affect other tabs' filters`() = runTest {
            // ----- Arrange -----
            val otherFilters = LibraryFilters(releaseYears = setOf(2021))

            stateFlow.value = LibraryUiState(
                filtersByTab = mapOf(
                    tabId to LibraryFilters(formats = setOf("ebook")),
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
            stateFlow.value.filtersByTab[otherTabId] shouldBe otherFilters
        }

        @Test
        fun `a known chip key toggles the resolved filter value off, same as OnToggleFilterValueAction`() = runTest {
            // ----- Arrange -----
            val value = LibraryFilterValue.Format(value = "ebook")
            val chip = ChipUiModel(
                key = "format:ebook",
                label = "ebook",
            )

            stateFlow.value = LibraryUiState(
                filtersByTab = mapOf(tabId to LibraryFilters(formats = setOf("ebook"))),
                activeFilterChipsByTab = mapOf(
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
            val existing = LibraryFilters(formats = setOf("ebook"))

            stateFlow.value = LibraryUiState(
                filtersByTab = mapOf(tabId to existing),
                activeFilterChipsByTab = emptyMap(),
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

        @Test
        fun `a key present in another tab's active filter chips but not this tab's is a no-op`() = runTest {
            // ----- Arrange -----
            val value = LibraryFilterValue.Format(value = "ebook")
            val chip = ChipUiModel(
                key = "format:ebook",
                label = "ebook",
            )
            val existing = LibraryFilters(formats = setOf("ebook"))

            stateFlow.value = LibraryUiState(
                filtersByTab = mapOf(tabId to existing),
                activeFilterChipsByTab = mapOf(
                    otherTabId to LibraryActiveFilterChips(chips = listOf(chip to value).toChipSet()),
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
            stateFlow.value.filtersByTab[tabId] shouldBe existing
        }
    }
}
