package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilters
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

class OnClearFiltersActionTest {
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
        fun `removes the tab's entry from filtersByTab`() = runTest {
            // ----- Arrange -----
            val activeFilters = LibraryFilters(formats = setOf("ebook"))

            stateFlow.value = LibraryUiState(
                filtersByTab = mapOf(tabId to activeFilters),
            )
            val action = OnClearFiltersAction(tabId = tabId)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filtersByTab.containsKey(tabId) shouldBe false
        }

        @Test
        fun `does not affect other tabs' filters`() = runTest {
            // ----- Arrange -----
            val activeFilters = LibraryFilters(formats = setOf("ebook"))
            val otherFilters = LibraryFilters(releaseYears = setOf(2021))

            stateFlow.value = LibraryUiState(
                filtersByTab = mapOf(
                    tabId to activeFilters,
                    otherTabId to otherFilters,
                ),
            )
            val action = OnClearFiltersAction(tabId = tabId)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filtersByTab[otherTabId] shouldBe otherFilters
        }

        @Test
        fun `is a no-op when the tab has no filters set`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(filtersByTab = emptyMap())
            val action = OnClearFiltersAction(tabId = tabId)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filtersByTab shouldBe emptyMap()
        }
    }
}
