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

class OnArrangeLayoutChipClickedActionTest {
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
            OnArrangeLayoutChipClickedAction(key = "layout:BOGUS").execute(
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
            OnArrangeLayoutChipClickedAction(key = "layout:GRID_TWO").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.arrangeDraft shouldBe null
        }

        @Test
        fun `switching chip preserves titles shown`() = runTest {
            // ----- Arrange -----
            val draft = LibraryArrangeDraft(
                tabId = tabId,
                sortMode = LibrarySortMode.TITLE,
                sortDirection = SortDirection.ASCENDING,
                gridLayout = LibraryGridLayout.GRID_THREE_COLUMNS,
            )
            stateFlow.value = LibraryUiState(arrangeDraft = draft)

            // ----- Act -----
            OnArrangeLayoutChipClickedAction(key = "layout:GRID_TWO").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.arrangeDraft?.gridLayout shouldBe LibraryGridLayout.GRID_TWO_COLUMNS
        }

        @Test
        fun `switching chip preserves cover-only`() = runTest {
            // ----- Arrange -----
            val draft = LibraryArrangeDraft(
                tabId = tabId,
                sortMode = LibrarySortMode.TITLE,
                sortDirection = SortDirection.ASCENDING,
                gridLayout = LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
            )
            stateFlow.value = LibraryUiState(arrangeDraft = draft)

            // ----- Act -----
            OnArrangeLayoutChipClickedAction(key = "layout:GRID_TWO").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.arrangeDraft?.gridLayout shouldBe LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY
        }

        @Test
        fun `switching to List always shows titles, even from a cover-only grid`() = runTest {
            // ----- Arrange -----
            val draft = LibraryArrangeDraft(
                tabId = tabId,
                sortMode = LibrarySortMode.TITLE,
                sortDirection = SortDirection.ASCENDING,
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
            )
            stateFlow.value = LibraryUiState(arrangeDraft = draft)

            // ----- Act -----
            OnArrangeLayoutChipClickedAction(key = "layout:LIST").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.arrangeDraft?.gridLayout shouldBe LibraryGridLayout.LIST_LARGE
        }

        @Test
        fun `leaves sortMode, sortDirection and tabId untouched`() = runTest {
            // ----- Arrange -----
            val draft = LibraryArrangeDraft(
                tabId = tabId,
                sortMode = LibrarySortMode.RATING,
                sortDirection = SortDirection.DESCENDING,
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )
            stateFlow.value = LibraryUiState(arrangeDraft = draft)

            // ----- Act -----
            OnArrangeLayoutChipClickedAction(key = "layout:GRID_THREE").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.arrangeDraft shouldBe draft.copy(gridLayout = LibraryGridLayout.GRID_THREE_COLUMNS)
        }
    }
}
