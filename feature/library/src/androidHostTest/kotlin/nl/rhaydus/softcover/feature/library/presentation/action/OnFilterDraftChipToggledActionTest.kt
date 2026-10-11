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
    private val formatKey = "format:Hardcover"
    private val formatValue = LibraryFilterValue.Format(value = "Hardcover")
    private val formatChip = ChipUiModel(
        key = formatKey,
        label = "Hardcover",
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
                filterChipsByTab = persistentMapOf(
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
                filterChipsByTab = persistentMapOf(
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
                filterChipsByTab = persistentMapOf(
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
                filters = LibraryFilters(formats = persistentSetOf("Hardcover")),
            )
            stateFlow.value = LibraryUiState(
                filterDraft = draft,
                filterChipsByTab = persistentMapOf(
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
    }
}
