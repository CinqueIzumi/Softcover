package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Tag
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterValue
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilters
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnToggleFilterValueActionTest {

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
        fun `adds filter value when tab has no filters yet`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(filtersByTab = emptyMap())

            val action = OnToggleFilterValueAction(
                tabId = tabId,
                value = LibraryFilterValue.Format("ebook"),
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filtersByTab[tabId]?.formats shouldBe setOf("ebook")
        }

        @Test
        fun `adds filter value to existing non-empty filters`() = runTest {
            // ----- Arrange -----
            val existing = LibraryFilters(formats = setOf("ebook"))

            stateFlow.value = LibraryUiState(filtersByTab = mapOf(tabId to existing))

            val action = OnToggleFilterValueAction(
                tabId = tabId,
                value = LibraryFilterValue.ReleaseYear(2021),
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filtersByTab[tabId]?.formats shouldBe setOf("ebook")
            stateFlow.value.filtersByTab[tabId]?.releaseYears shouldBe setOf(2021)
        }

        @Test
        fun `removes tab entry from filtersByTab when resulting filters are empty`() = runTest {
            // ----- Arrange -----
            val existing = LibraryFilters(formats = setOf("ebook"))

            stateFlow.value = LibraryUiState(filtersByTab = mapOf(tabId to existing))

            val action = OnToggleFilterValueAction(
                tabId = tabId,
                value = LibraryFilterValue.Format("ebook"),
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
        fun `sets tab entry when resulting filters are non-empty after toggle`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(filtersByTab = emptyMap())

            val tag = Tag(id = 1, name = "Fiction")
            val action = OnToggleFilterValueAction(
                tabId = tabId,
                value = LibraryFilterValue.Tag(tag),
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filtersByTab.containsKey(tabId) shouldBe true
        }

        @Test
        fun `does not touch other tabs filters`() = runTest {
            // ----- Arrange -----
            val otherFilters = LibraryFilters(releaseYears = setOf(2020))

            stateFlow.value = LibraryUiState(
                filtersByTab = mapOf(otherTabId to otherFilters),
            )

            val action = OnToggleFilterValueAction(
                tabId = tabId,
                value = LibraryFilterValue.Format("ebook"),
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
        fun `toggling RatingMin to same value clears it and removes tab entry when only filter`() = runTest {
            // ----- Arrange -----
            val existing = LibraryFilters(ratingMin = 4.0)

            stateFlow.value = LibraryUiState(filtersByTab = mapOf(tabId to existing))

            val action = OnToggleFilterValueAction(
                tabId = tabId,
                value = LibraryFilterValue.RatingMin(4.0),
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filtersByTab.containsKey(tabId) shouldBe false
        }
    }
}
