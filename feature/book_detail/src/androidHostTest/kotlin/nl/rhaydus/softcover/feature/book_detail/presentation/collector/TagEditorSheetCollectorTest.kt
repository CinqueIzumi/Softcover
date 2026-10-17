package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.toChipSet
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.state.EmptyStateUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.tagEditorSheetScaffoldFor
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TagEditorSheetCollectorTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk(relaxed = true)
    }

    private fun bookTitled(title: String): Book = mockk<Book>().also {
        every {
            it.title
        } returns title
    }

    private fun TestScope.launchCollector() {
        backgroundScope.launch {
            TagEditorSheetCollector().onLaunch(
                scope = scope,
                dependencies = dependencies,
            )
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `builds tagEditorSheet from the book title and cover`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val cover = mockk<CoverUiModel>()
            stateFlow.value = buildBookDetailUiState().copy(
                book = bookTitled("Dune"),
                tagEditorCover = cover,
            )

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.tagEditorSheet.shouldNotBeNull().scaffold shouldBe tagEditorSheetScaffoldFor(
                bookTitle = "Dune",
                cover = cover,
            )
        }

        @Test
        fun `updates tagEditorSheet when the cover arrives`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val cover = mockk<CoverUiModel>()
            stateFlow.value = buildBookDetailUiState().copy(book = bookTitled("Dune"))
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(tagEditorCover = cover)

            // ----- Assert -----
            stateFlow.value.tagEditorSheet.shouldNotBeNull().scaffold shouldBe tagEditorSheetScaffoldFor(
                bookTitle = "Dune",
                cover = cover,
            )
        }

        @Test
        fun `clears tagEditorSheet when the book goes away`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(book = bookTitled("Dune"))
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = null)

            // ----- Assert -----
            stateFlow.value.tagEditorSheet.shouldBeNull()
        }

        @Test
        fun `carries the add block and the tag groups from state into tagEditorSheet`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val moodChip = ChipUiModel(
                    key = "MOOD",
                    label = "Mood",
                )
                val categoryChips = persistentListOf(moodChip)
                val suggestion = ChipUiModel(
                    key = "suggestion",
                    label = "cozy",
                )
                val cozyTag = UserTag(
                    name = "cozy",
                    category = TagCategory.MOOD,
                )
                val emptyState = EmptyStateUiModel(
                    title = "Nothing",
                    body = "Add one",
                )
                stateFlow.value = buildBookDetailUiState().copy(
                    book = bookTitled("Dune"),
                    tagEditorCategory = TagCategory.MOOD,
                    tagEditorCategoryChips = categoryChips,
                    tagSuggestionChips = listOf(suggestion to cozyTag).toChipSet(),
                    tagEditorEmptyState = emptyState,
                    newlyAddedTagKey = "MOOD:cozy",
                )

                // ----- Act -----
                launchCollector()

                // ----- Assert -----
                val sheet = stateFlow.value.tagEditorSheet.shouldNotBeNull()
                sheet.categoryChips shouldBe categoryChips
                sheet.selectedCategoryKey shouldBe "MOOD"
                sheet.suggestionChips shouldBe listOf(suggestion)
                sheet.emptyState shouldBe emptyState
                sheet.newlyAddedTagKey shouldBe "MOOD:cozy"
            }

        @Test
        fun `follows the selected category as it changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(book = bookTitled("Dune"))
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(tagEditorCategory = TagCategory.GENRE)

            // ----- Assert -----
            stateFlow.value.tagEditorSheet.shouldNotBeNull().selectedCategoryKey shouldBe "GENRE"
        }
    }
}
