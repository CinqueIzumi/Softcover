package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.toChipSet
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.HardcoverTagKey
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterChips
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterDraft
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterOptions
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterValue
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilters
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryTagOption
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.TagSectionDraft
import nl.rhaydus.softcover.feature.library.presentation.state.TagSelection
import nl.rhaydus.softcover.feature.library.presentation.state.TagSource
import nl.rhaydus.softcover.feature.library.presentation.state.UserTagKey
import nl.rhaydus.softcover.feature.library.presentation.state.buildTagSourceSummary
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
    private val cozy = UserTagKey(
        category = TagCategory.MOOD,
        name = "cozy",
    )
    private val fiction = HardcoverTagKey(
        id = 1,
        name = "Fiction",
    )
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

        @Test
        fun `a picker row's Include key includes the tag in its source's selection`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateWithTagOptions(filters = LibraryFilters())

            // ----- Act -----
            OnFilterDraftChipToggledAction(key = "yours:include:MOOD:cozy").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft?.filters shouldBe LibraryFilters(
                yourTags = TagSelection(included = persistentSetOf(cozy)),
            )
        }

        @Test
        fun `a picker row's Leave out key excludes the Hardcover tag`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateWithTagOptions(filters = LibraryFilters())

            // ----- Act -----
            OnFilterDraftChipToggledAction(key = "hardcover:exclude:1").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft?.filters shouldBe LibraryFilters(
                hardcoverTags = TagSelection(excluded = persistentSetOf(fiction)),
            )
        }

        @Test
        fun `tapping the direction a tag already has clears it`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateWithTagOptions(
                filters = LibraryFilters(yourTags = TagSelection(excluded = persistentSetOf(cozy))),
            )

            // ----- Act -----
            OnFilterDraftChipToggledAction(key = "yours:exclude:MOOD:cozy").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft?.filters shouldBe LibraryFilters()
        }

        @Test
        fun `tapping the other direction moves a tag across`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateWithTagOptions(
                filters = LibraryFilters(yourTags = TagSelection(included = persistentSetOf(cozy))),
            )

            // ----- Act -----
            OnFilterDraftChipToggledAction(key = "yours:exclude:MOOD:cozy").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft?.filters shouldBe LibraryFilters(
                yourTags = TagSelection(excluded = persistentSetOf(cozy)),
            )
        }

        @Test
        fun `an included chosen chip's key flips the tag to left out`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateWithTagOptions(
                filters = LibraryFilters(yourTags = TagSelection(included = persistentSetOf(cozy))),
            )
            val chip = chosenChipKey()

            // ----- Act -----
            OnFilterDraftChipToggledAction(key = chip).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft?.filters shouldBe LibraryFilters(
                yourTags = TagSelection(excluded = persistentSetOf(cozy)),
            )
        }

        @Test
        fun `a left out chosen chip's key flips the tag to included`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateWithTagOptions(
                filters = LibraryFilters(yourTags = TagSelection(excluded = persistentSetOf(cozy))),
            )
            val chip = chosenChipKey()

            // ----- Act -----
            OnFilterDraftChipToggledAction(key = chip).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft?.filters shouldBe LibraryFilters(
                yourTags = TagSelection(included = persistentSetOf(cozy)),
            )
        }

        @Test
        fun `a tag key the shelf does not carry is a no-op`() = runTest {
            // ----- Arrange -----
            val initial = stateWithTagOptions(filters = LibraryFilters())
            stateFlow.value = initial

            // ----- Act -----
            OnFilterDraftChipToggledAction(key = "yours:include:MOOD:missing").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft shouldBe initial.filterDraft
        }

        @Test
        fun `a chosen Yours tag the shelf no longer carries can be cleared by its chip key`() = runTest {
            // ----- Arrange -----
            val gone = UserTagKey(
                category = TagCategory.MOOD,
                name = "gone",
            )
            stateFlow.value = stateWithTagOptions(
                filters = LibraryFilters(yourTags = TagSelection(excluded = persistentSetOf(gone))),
            )

            // ----- Act -----
            OnFilterDraftChipToggledAction(key = "yours:exclude:MOOD:gone").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft?.filters shouldBe LibraryFilters()
        }

        @Test
        fun `a chosen Hardcover tag the shelf no longer carries can be cleared by its chip key`() = runTest {
            // ----- Arrange -----
            val gone = HardcoverTagKey(
                id = 77,
                name = "Gone",
            )
            stateFlow.value = stateWithTagOptions(
                filters = LibraryFilters(hardcoverTags = TagSelection(included = persistentSetOf(gone))),
            )

            // ----- Act -----
            OnFilterDraftChipToggledAction(key = "hardcover:include:77").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft?.filters shouldBe LibraryFilters()
        }
    }

    private fun chosenChipKey(): String {
        val state = stateFlow.value
        val draft = state.filterDraft ?: error("draft expected")
        val options = state.availableFilterOptionsFor(draft.tabId)
        val summary = buildTagSourceSummary(
            source = TagSource.YOURS,
            options = options,
            filters = draft.filters,
            section = TagSectionDraft(),
        )

        return summary?.chosenChips?.single()?.key ?: error("chip expected")
    }

    private fun stateWithTagOptions(filters: LibraryFilters): LibraryUiState = LibraryUiState(
        filterDraft = LibraryFilterDraft(
            tabId = tabId,
            filters = filters,
        ),
        filterOptionsByTab = persistentMapOf(
            tabId to LibraryFilterOptions(
                yourTags = persistentListOf(
                    LibraryTagOption(
                        key = cozy,
                        category = TagCategory.MOOD,
                        count = 2,
                    ),
                ),
                hardcoverTags = persistentListOf(
                    LibraryTagOption(
                        key = fiction,
                        category = TagCategory.GENRE,
                        count = 3,
                    ),
                ),
            ),
        ),
    )
}
