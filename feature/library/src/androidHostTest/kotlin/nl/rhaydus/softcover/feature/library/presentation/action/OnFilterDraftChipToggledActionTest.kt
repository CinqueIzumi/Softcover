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
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterChips
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterDraft
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterValue
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilters
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnFilterDraftChipToggledActionTest {
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    private val tabId = "list-10"
    private val otherTabId = "list-99"
    private val formatKey = "format:Hardcover"
    private val formatValue = LibraryFilterValue.Format(value = "Hardcover")
    private val formatChip = ChipUiModel(
        key = formatKey,
        label = "Hardcover",
    )
    private val ownedKey = "owned:true"
    private val ownedValue = LibraryFilterValue.Owned(owned = true)
    private val ownedChip = ChipUiModel(
        key = ownedKey,
        label = "Owned",
    )

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
            val draft = LibraryFilterDraft(
                tabId = tabId,
                filters = LibraryFilters(),
            )
            stateFlow.value = LibraryUiState(
                filterDraft = draft,
                filterChipsByTab = mapOf(
                    tabId to LibraryFilterChips(formatChips = listOf(formatChip to formatValue).toChipSet()),
                ),
            )

            // ----- Act -----
            OnFilterDraftChipToggledAction(key = "format:Bogus").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft shouldBe draft
        }

        @Test
        fun `no-op when filterDraft is null`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                filterDraft = null,
                filterChipsByTab = mapOf(
                    tabId to LibraryFilterChips(formatChips = listOf(formatChip to formatValue).toChipSet()),
                ),
            )

            // ----- Act -----
            OnFilterDraftChipToggledAction(key = formatKey).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft shouldBe null
        }

        @Test
        fun `toggling an absent facet value adds it to the draft`() = runTest {
            // ----- Arrange -----
            val draft = LibraryFilterDraft(
                tabId = tabId,
                filters = LibraryFilters(),
            )
            stateFlow.value = LibraryUiState(
                filterDraft = draft,
                filterChipsByTab = mapOf(
                    tabId to LibraryFilterChips(formatChips = listOf(formatChip to formatValue).toChipSet()),
                ),
            )

            // ----- Act -----
            OnFilterDraftChipToggledAction(key = formatKey).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft?.filters?.formats shouldBe setOf("Hardcover")
        }

        @Test
        fun `toggling an already-selected facet value removes it from the draft`() = runTest {
            // ----- Arrange -----
            val draft = LibraryFilterDraft(
                tabId = tabId,
                filters = LibraryFilters(formats = setOf("Hardcover")),
            )
            stateFlow.value = LibraryUiState(
                filterDraft = draft,
                filterChipsByTab = mapOf(
                    tabId to LibraryFilterChips(formatChips = listOf(formatChip to formatValue).toChipSet()),
                ),
            )

            // ----- Act -----
            OnFilterDraftChipToggledAction(key = formatKey).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft?.filters?.formats shouldBe emptySet()
        }

        @Test
        fun `toggling a single-select facet value on then off clears it back to null`() = runTest {
            // ----- Arrange -----
            val draft = LibraryFilterDraft(
                tabId = tabId,
                filters = LibraryFilters(),
            )
            stateFlow.value = LibraryUiState(
                filterDraft = draft,
                filterChipsByTab = mapOf(
                    tabId to LibraryFilterChips(ownershipChips = listOf(ownedChip to ownedValue).toChipSet()),
                ),
            )

            // ----- Act -----
            OnFilterDraftChipToggledAction(key = ownedKey).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft?.filters?.owned shouldBe true

            // ----- Act -----
            OnFilterDraftChipToggledAction(key = ownedKey).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft?.filters?.owned shouldBe null
        }

        @Test
        fun `leaves tagSearch and tabId untouched`() = runTest {
            // ----- Arrange -----
            val draft = LibraryFilterDraft(
                tabId = tabId,
                filters = LibraryFilters(),
                tagSearch = "kotlin",
            )
            stateFlow.value = LibraryUiState(
                filterDraft = draft,
                filterChipsByTab = mapOf(
                    tabId to LibraryFilterChips(formatChips = listOf(formatChip to formatValue).toChipSet()),
                ),
            )

            // ----- Act -----
            OnFilterDraftChipToggledAction(key = formatKey).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft?.tabId shouldBe tabId
            stateFlow.value.filterDraft?.tagSearch shouldBe "kotlin"
        }

        @Test
        fun `a key present in another tab's chips but not the draft's tab is a no-op`() = runTest {
            // ----- Arrange -----
            val draft = LibraryFilterDraft(
                tabId = tabId,
                filters = LibraryFilters(),
            )
            stateFlow.value = LibraryUiState(
                filterDraft = draft,
                filterChipsByTab = mapOf(
                    otherTabId to LibraryFilterChips(formatChips = listOf(formatChip to formatValue).toChipSet()),
                ),
            )

            // ----- Act -----
            OnFilterDraftChipToggledAction(key = formatKey).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft shouldBe draft
        }
    }
}
