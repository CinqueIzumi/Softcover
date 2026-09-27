package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryArrangeDraft
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnArrangeSortChipClickedActionTest {
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    private val tabId = "list-10"

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
        fun `unknown key is a no-op`() = runTest {
            // ----- Arrange -----
            val draft = LibraryArrangeDraft(
                tabId = tabId,
                sortMode = LibrarySortMode.TITLE,
                sortDirection = SortDirection.ASCENDING,
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )
            stateFlow.value = LibraryUiState(arrangeDraft = draft)

            // ----- Act -----
            OnArrangeSortChipClickedAction(key = "sort:BOGUS").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.arrangeDraft shouldBe draft
        }

        @Test
        fun `no-op when arrangeDraft is null`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(arrangeDraft = null)

            // ----- Act -----
            OnArrangeSortChipClickedAction(key = "sort:TITLE").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.arrangeDraft shouldBe null
        }

        @Test
        fun `tapping the already-active mode flips its direction`() = runTest {
            // ----- Arrange -----
            val draft = LibraryArrangeDraft(
                tabId = tabId,
                sortMode = LibrarySortMode.TITLE,
                sortDirection = SortDirection.ASCENDING,
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )
            stateFlow.value = LibraryUiState(arrangeDraft = draft)

            // ----- Act -----
            OnArrangeSortChipClickedAction(key = "sort:TITLE").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.arrangeDraft?.sortMode shouldBe LibrarySortMode.TITLE
            stateFlow.value.arrangeDraft?.sortDirection shouldBe SortDirection.DESCENDING
        }

        @Test
        fun `tapping the already-active mode a second time flips direction back`() = runTest {
            // ----- Arrange -----
            val draft = LibraryArrangeDraft(
                tabId = tabId,
                sortMode = LibrarySortMode.TITLE,
                sortDirection = SortDirection.DESCENDING,
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )
            stateFlow.value = LibraryUiState(arrangeDraft = draft)

            // ----- Act -----
            OnArrangeSortChipClickedAction(key = "sort:TITLE").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.arrangeDraft?.sortDirection shouldBe SortDirection.ASCENDING
        }

        @Test
        fun `tapping a different mode switches to it at its default direction`() = runTest {
            // ----- Arrange -----
            val draft = LibraryArrangeDraft(
                tabId = tabId,
                sortMode = LibrarySortMode.TITLE,
                sortDirection = SortDirection.DESCENDING,
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )
            stateFlow.value = LibraryUiState(arrangeDraft = draft)

            // ----- Act -----
            OnArrangeSortChipClickedAction(key = "sort:RATING").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.arrangeDraft?.sortMode shouldBe LibrarySortMode.RATING
            stateFlow.value.arrangeDraft?.sortDirection shouldBe LibrarySortMode.RATING.defaultDirection
        }

        @Test
        fun `leaves gridLayout and tabId untouched`() = runTest {
            // ----- Arrange -----
            val draft = LibraryArrangeDraft(
                tabId = tabId,
                sortMode = LibrarySortMode.TITLE,
                sortDirection = SortDirection.ASCENDING,
                gridLayout = LibraryGridLayout.LIST_LARGE,
            )
            stateFlow.value = LibraryUiState(arrangeDraft = draft)

            // ----- Act -----
            OnArrangeSortChipClickedAction(key = "sort:RATING").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.arrangeDraft shouldBe draft.copy(
                sortMode = LibrarySortMode.RATING,
                sortDirection = LibrarySortMode.RATING.defaultDirection,
            )
        }
    }
}
