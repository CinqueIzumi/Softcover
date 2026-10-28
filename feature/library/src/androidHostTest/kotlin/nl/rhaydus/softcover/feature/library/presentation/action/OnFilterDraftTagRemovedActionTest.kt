package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.HardcoverTagKey
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterDraft
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterOptions
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilters
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryTagOption
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.TagSelection
import nl.rhaydus.softcover.feature.library.presentation.state.UserTagKey
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnFilterDraftTagRemovedActionTest {
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    private val tabId = "list-10"
    private val cozy = UserTagKey(
        category = TagCategory.MOOD,
        name = "cozy",
    )
    private val tense = UserTagKey(
        category = TagCategory.MOOD,
        name = "tense",
    )
    private val fiction = HardcoverTagKey(
        id = 1,
        name = "Fiction",
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
        fun `removes an included tag`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateWith(
                filters = LibraryFilters(yourTags = TagSelection(included = persistentSetOf(cozy))),
            )

            // ----- Act -----
            OnFilterDraftTagRemovedAction(key = "yours:include:MOOD:cozy").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft?.filters shouldBe LibraryFilters()
        }

        @Test
        fun `removes a left out tag`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateWith(
                filters = LibraryFilters(hardcoverTags = TagSelection(excluded = persistentSetOf(fiction))),
            )

            // ----- Act -----
            OnFilterDraftTagRemovedAction(key = "hardcover:include:1").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft?.filters shouldBe LibraryFilters()
        }

        @Test
        fun `removes a stale tag the shelf no longer carries`() = runTest {
            // ----- Arrange -----
            val gone = UserTagKey(
                category = TagCategory.MOOD,
                name = "gone",
            )
            stateFlow.value = stateWith(
                filters = LibraryFilters(yourTags = TagSelection(excluded = persistentSetOf(gone))),
            )

            // ----- Act -----
            OnFilterDraftTagRemovedAction(key = "yours:include:MOOD:gone").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft?.filters shouldBe LibraryFilters()
        }

        @Test
        fun `leaves the other chosen tags alone`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateWith(
                filters = LibraryFilters(
                    hardcoverTags = TagSelection(included = persistentSetOf(fiction)),
                    yourTags = TagSelection(
                        included = persistentSetOf(cozy),
                        excluded = persistentSetOf(tense),
                    ),
                ),
            )

            // ----- Act -----
            OnFilterDraftTagRemovedAction(key = "yours:exclude:MOOD:cozy").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft?.filters shouldBe LibraryFilters(
                hardcoverTags = TagSelection(included = persistentSetOf(fiction)),
                yourTags = TagSelection(excluded = persistentSetOf(tense)),
            )
        }

        @Test
        fun `no-op when filterDraft is null`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(filterDraft = null)

            // ----- Act -----
            OnFilterDraftTagRemovedAction(key = "yours:include:MOOD:cozy").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft shouldBe null
        }
    }

    private fun stateWith(filters: LibraryFilters): LibraryUiState = LibraryUiState(
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
                    LibraryTagOption(
                        key = tense,
                        category = TagCategory.MOOD,
                        count = 1,
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
