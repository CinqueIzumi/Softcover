package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterDraft
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilters
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

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
        fun `sets the tab's entry to the non-empty draft filters`() = runTest {
            // ----- Arrange -----
            val draft = LibraryFilters(formats = setOf("ebook"))
            stateFlow.value = LibraryUiState(
                filtersByTab = emptyMap(),
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
            stateFlow.value.filtersByTab[tabId] shouldBe draft
        }

        @Test
        fun `replaces an existing non-empty entry for the tab`() = runTest {
            // ----- Arrange -----
            val previous = LibraryFilters(formats = setOf("paperback"))
            val draft = LibraryFilters(
                formats = setOf("ebook"),
                owned = true,
            )
            stateFlow.value = LibraryUiState(
                filtersByTab = mapOf(tabId to previous),
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
            stateFlow.value.filtersByTab[tabId] shouldBe draft
        }

        @Test
        fun `removes the tab entry when the applied filters are empty`() = runTest {
            // ----- Arrange -----
            val previous = LibraryFilters(formats = setOf("ebook"))
            stateFlow.value = LibraryUiState(
                filtersByTab = mapOf(tabId to previous),
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
            stateFlow.value.filtersByTab.containsKey(tabId) shouldBe false
        }

        @Test
        fun `applying empty filters to a tab with no existing entry is a no-op`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                filtersByTab = emptyMap(),
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
            stateFlow.value.filtersByTab shouldBe emptyMap()
        }

        @Test
        fun `does not touch other tabs' filters when setting a non-empty entry`() = runTest {
            // ----- Arrange -----
            val otherFilters = LibraryFilters(releaseYears = setOf(2020))
            stateFlow.value = LibraryUiState(
                filtersByTab = mapOf(otherTabId to otherFilters),
                filterDraft = LibraryFilterDraft(
                    tabId = tabId,
                    filters = LibraryFilters(formats = setOf("ebook")),
                ),
            )

            // ----- Act -----
            OnApplyFiltersAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filtersByTab[otherTabId] shouldBe otherFilters
        }

        @Test
        fun `does not touch other tabs' filters when clearing to an empty entry`() = runTest {
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

        @Test
        fun `does not clobber unrelated state fields`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                searchQuery = "kotlin",
                gridLayout = LibraryGridLayout.LIST_COMPACT,
                filterDraft = LibraryFilterDraft(
                    tabId = tabId,
                    filters = LibraryFilters(formats = setOf("ebook")),
                ),
            )

            // ----- Act -----
            OnApplyFiltersAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.searchQuery shouldBe "kotlin"
            stateFlow.value.gridLayout shouldBe LibraryGridLayout.LIST_COMPACT
        }
    }
}
