package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterDraft
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.TAG_ALL_CATEGORY_KEY
import nl.rhaydus.softcover.feature.library.presentation.state.TagSectionDraft
import nl.rhaydus.softcover.feature.library.presentation.state.TagSource
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnFilterTagCategoryPickedActionTest {
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
            OnFilterTagCategoryPickedAction(
                source = TagSource.YOURS,
                key = TagCategory.MOOD.name,
            ).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft.shouldBeNull()
        }

        @Test
        fun `picks the category in the source's section only`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(filterDraft = LibraryFilterDraft(tabId = "all"))

            // ----- Act -----
            OnFilterTagCategoryPickedAction(
                source = TagSource.HARDCOVER,
                key = TagCategory.GENRE.name,
            ).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val draft = stateFlow.value.filterDraft.shouldNotBeNull()
            draft.hardcoverTagsSection.category shouldBe TagCategory.GENRE
            draft.yourTagsSection.category shouldBe null
        }

        @Test
        fun `picking leaves the section's search as it is`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                filterDraft = LibraryFilterDraft(
                    tabId = "all",
                    yourTagsSection = TagSectionDraft(
                        search = "coz",
                    ),
                ),
            )

            // ----- Act -----
            OnFilterTagCategoryPickedAction(
                source = TagSource.YOURS,
                key = TagCategory.MOOD.name,
            ).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft?.yourTagsSection shouldBe TagSectionDraft(
                search = "coz",
                category = TagCategory.MOOD,
            )
        }

        @Test
        fun `the All key clears the picked category`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                filterDraft = LibraryFilterDraft(
                    tabId = "all",
                    yourTagsSection = TagSectionDraft(category = TagCategory.MOOD),
                ),
            )

            // ----- Act -----
            OnFilterTagCategoryPickedAction(
                source = TagSource.YOURS,
                key = TAG_ALL_CATEGORY_KEY,
            ).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft?.yourTagsSection?.category.shouldBeNull()
        }
    }
}
