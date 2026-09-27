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
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryArrangeDraft
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

class OnArrangeTitlesToggleChangedActionTest {
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
        fun `no-op when arrangeDraft is null`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(arrangeDraft = null)

            // ----- Act -----
            OnArrangeTitlesToggleChangedAction(show = false).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.arrangeDraft shouldBe null
        }

        @Test
        fun `show=false switches a titled grid to cover-only`() = runTest {
            // ----- Arrange -----
            val draft = LibraryArrangeDraft(
                tabId = tabId,
                sortMode = LibrarySortMode.TITLE,
                sortDirection = SortDirection.ASCENDING,
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )
            stateFlow.value = LibraryUiState(arrangeDraft = draft)

            // ----- Act -----
            OnArrangeTitlesToggleChangedAction(show = false).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.arrangeDraft?.gridLayout shouldBe LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY
        }

        @Test
        fun `show=true switches a cover-only grid back to titled`() = runTest {
            // ----- Arrange -----
            val draft = LibraryArrangeDraft(
                tabId = tabId,
                sortMode = LibrarySortMode.TITLE,
                sortDirection = SortDirection.ASCENDING,
                gridLayout = LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
            )
            stateFlow.value = LibraryUiState(arrangeDraft = draft)

            // ----- Act -----
            OnArrangeTitlesToggleChangedAction(show = true).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.arrangeDraft?.gridLayout shouldBe LibraryGridLayout.GRID_THREE_COLUMNS
        }

        @Test
        fun `show=false is a no-op on List, which has no cover-only sibling`() = runTest {
            // ----- Arrange -----
            val draft = LibraryArrangeDraft(
                tabId = tabId,
                sortMode = LibrarySortMode.TITLE,
                sortDirection = SortDirection.ASCENDING,
                gridLayout = LibraryGridLayout.LIST_LARGE,
            )
            stateFlow.value = LibraryUiState(arrangeDraft = draft)

            // ----- Act -----
            OnArrangeTitlesToggleChangedAction(show = false).execute(
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
            OnArrangeTitlesToggleChangedAction(show = false).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.arrangeDraft shouldBe draft.copy(
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
            )
        }
    }
}
