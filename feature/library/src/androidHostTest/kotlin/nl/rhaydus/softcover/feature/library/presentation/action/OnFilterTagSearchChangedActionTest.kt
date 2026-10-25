package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterChips
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterDraft
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterOptions
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryTagOption
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.TagSectionDraft
import nl.rhaydus.softcover.feature.library.presentation.state.TagSource
import nl.rhaydus.softcover.feature.library.presentation.state.UserTagKey
import nl.rhaydus.softcover.feature.library.presentation.state.buildLibraryFilterSheetSelection
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnFilterTagSearchChangedActionTest {
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

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
            stateFlow.value = LibraryUiState(filterDraft = null)

            // ----- Act -----
            OnFilterTagSearchChangedAction(
                source = TagSource.YOURS,
                query = "cozy",
            ).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft.shouldBeNull()
        }

        @Test
        fun `writes the query into the source's section only`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                filterDraft = LibraryFilterDraft(
                    tabId = "all",
                    hardcoverTagsSection = TagSectionDraft(search = "magic"),
                ),
            )

            // ----- Act -----
            OnFilterTagSearchChangedAction(
                source = TagSource.YOURS,
                query = "cozy",
            ).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val draft = stateFlow.value.filterDraft.shouldNotBeNull()
            draft.yourTagsSection.search shouldBe "cozy"
            draft.hardcoverTagsSection.search shouldBe "magic"
        }

        @Test
        fun `emptying the search drops its category`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                filterDraft = LibraryFilterDraft(
                    tabId = "all",
                    yourTagsSection = TagSectionDraft(
                        search = "coz",
                        category = TagCategory.MOOD,
                    ),
                ),
            )

            // ----- Act -----
            OnFilterTagSearchChangedAction(
                source = TagSource.YOURS,
                query = "",
            ).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft?.yourTagsSection shouldBe TagSectionDraft()
        }

        @Test
        fun `typing keeps the category while the search is not empty`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                filterDraft = LibraryFilterDraft(
                    tabId = "all",
                    yourTagsSection = TagSectionDraft(
                        search = "c",
                        category = TagCategory.MOOD,
                    ),
                ),
            )

            // ----- Act -----
            OnFilterTagSearchChangedAction(
                source = TagSource.YOURS,
                query = "co",
            ).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft?.yourTagsSection?.category shouldBe TagCategory.MOOD
        }

        @Test
        fun `writes the query straight into the built search field of that source`() = runTest {
            // ----- Arrange -----
            val options = LibraryFilterOptions(
                yourTags = persistentListOf(
                    LibraryTagOption(
                        key = UserTagKey(
                            category = TagCategory.MOOD,
                            name = "cozy",
                        ),
                        category = TagCategory.MOOD,
                        count = 2,
                    ),
                ),
            )
            val draft = LibraryFilterDraft(tabId = "all")
            val initial = LibraryUiState()
            stateFlow.value = initial.copy(
                filterDraft = draft,
                filterSheet = initial.filterSheet.copy(
                    selection = buildLibraryFilterSheetSelection(
                        chips = LibraryFilterChips(),
                        options = options,
                        draft = draft,
                        resultCount = 0,
                    ),
                ),
            )

            // ----- Act -----
            OnFilterTagSearchChangedAction(
                source = TagSource.YOURS,
                query = "coz",
            ).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterSheet.selection?.yourTags?.searchField?.text shouldBe "coz"
        }

        @Test
        fun `no-op on the built selection while none has been produced yet`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(filterDraft = LibraryFilterDraft(tabId = "all"))

            // ----- Act -----
            OnFilterTagSearchChangedAction(
                source = TagSource.YOURS,
                query = "coz",
            ).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterSheet.selection.shouldBeNull()
        }
    }
}
