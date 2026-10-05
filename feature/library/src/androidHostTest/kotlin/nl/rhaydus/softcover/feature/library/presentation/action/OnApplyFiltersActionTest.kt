package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterDraft
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilters
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnApplyFiltersActionTest {
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
        fun `no-op when filterDraft is null`() = runTest {
            // ----- Arrange -----
            val existing = LibraryFilters(formats = setOf("ebook"))
            stateFlow.value = LibraryUiState(
                filtersByTab = mapOf(tabId to existing),
                filterDraft = null,
            )

            // ----- Act -----
            OnApplyFiltersAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filtersByTab shouldBe mapOf(tabId to existing)
        }

        @Test
        fun `replaces the tab's entry with the draft filters and leaves other tabs alone`() = runTest {
            // ----- Arrange -----
            val previous = LibraryFilters(formats = setOf("paperback"))
            val draft = LibraryFilters(
                formats = setOf("ebook"),
                owned = true,
            )
            val otherFilters = LibraryFilters(releaseYears = setOf(2020))
            stateFlow.value = LibraryUiState(
                filtersByTab = mapOf(
                    tabId to previous,
                    otherTabId to otherFilters,
                ),
                filterDraft = LibraryFilterDraft(
                    tabId = tabId,
                    filters = draft,
                ),
            )

            // ----- Act -----
            OnApplyFiltersAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filtersByTab shouldBe mapOf(
                tabId to draft,
                otherTabId to otherFilters,
            )
        }

        @Test
        fun `removes the tab's entry when the draft filters are empty and leaves other tabs alone`() = runTest {
            // ----- Arrange -----
            val ownFilters = LibraryFilters(formats = setOf("ebook"))
            val otherFilters = LibraryFilters(releaseYears = setOf(2020))
            stateFlow.value = LibraryUiState(
                filtersByTab = mapOf(
                    tabId to ownFilters,
                    otherTabId to otherFilters,
                ),
                filterDraft = LibraryFilterDraft(
                    tabId = tabId,
                    filters = LibraryFilters(),
                ),
            )

            // ----- Act -----
            OnApplyFiltersAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filtersByTab shouldBe mapOf(otherTabId to otherFilters)
        }
    }
}
