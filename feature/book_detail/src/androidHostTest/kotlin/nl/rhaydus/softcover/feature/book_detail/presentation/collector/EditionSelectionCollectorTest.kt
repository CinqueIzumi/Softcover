package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.badge.BadgeVariant
import nl.rhaydus.softcover.core.component.selection.SelectionMarkUiModel
import nl.rhaydus.softcover.core.component.selection.SelectionMarkVariant
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.model.EditionTrailingUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private val TRACKING_BADGE = BadgeUiModel(
    label = "TRACKING NOW",
    tone = BadgeTone.Neutral,
    variant = BadgeVariant.TrackingTag,
)

private val OWNED_BADGE = BadgeUiModel(
    label = "Owned",
    tone = BadgeTone.Scrim,
    variant = BadgeVariant.OwnedCover,
    icon = SoftcoverIcon.Check,
)

private val SELECTED_MARK = SelectionMarkUiModel(
    selected = true,
    variant = SelectionMarkVariant.EditionCard,
)

class EditionSelectionCollectorTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(BookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk(relaxed = true)
    }

    // -------------------------------------------------------------------------
    // Builder helpers — use real data-class instances so overlay/.copy() works
    // -------------------------------------------------------------------------

    private fun buildEdition(id: Int): BookEdition = BookEdition(
        id = id,
        canonicalId = null,
        bookId = 0,
        publisher = null,
        title = null,
        url = null,
        localImagePath = null,
        isbn10 = null,
        isbn13 = null,
        pages = null,
        audioSeconds = null,
        authors = emptyList<Author>(),
        releaseYear = 2020,
        format = "Paperback",
        owned = false,
    )

    private fun buildOwnedList(editionId: Int): BookList = BookList(
        id = 1,
        name = "Owned",
        slug = "owned",
        books = listOf(
            ListBook(
                listBookId = 1,
                listId = 1,
                bookId = 0,
                editionId = editionId,
            ),
        ),
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `marks the current edition Selected when the sheet just opened`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val current = buildEdition(id = 1)
            val other = buildEdition(id = 2)
            stateFlow.value = BookDetailUiState(
                editions = listOf(current, other),
                previewEdition = current,
                selectedEditionId = current.id,
            )
            val collector = EditionSelectionCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.editionTrailingMarks shouldBe mapOf(
                1 to EditionTrailingUiModel.Selected(mark = SELECTED_MARK),
            )
            job.cancel()
        }

        @Test
        fun `marks another selected edition Selected and the current edition Tracking`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val current = buildEdition(id = 1)
            val other = buildEdition(id = 2)
            stateFlow.value = BookDetailUiState(
                editions = listOf(current, other),
                previewEdition = current,
                selectedEditionId = other.id,
            )
            val collector = EditionSelectionCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.editionTrailingMarks shouldBe mapOf(
                2 to EditionTrailingUiModel.Selected(mark = SELECTED_MARK),
                1 to EditionTrailingUiModel.Tracking(badge = TRACKING_BADGE),
            )
            job.cancel()
        }

        @Test
        fun `an edition that is neither selected nor current has no trailing mark`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val current = buildEdition(id = 1)
            val selected = buildEdition(id = 2)
            val untouched = buildEdition(id = 3)
            stateFlow.value = BookDetailUiState(
                editions = listOf(current, selected, untouched),
                previewEdition = current,
                selectedEditionId = selected.id,
            )
            val collector = EditionSelectionCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.editionTrailingMarks.containsKey(untouched.id) shouldBe false
            job.cancel()
        }

        @Test
        fun `sets ownedCoverBadge when the displayed edition is owned`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val displayed = buildEdition(id = 1)
            stateFlow.value = BookDetailUiState(
                previewEdition = displayed,
                userLists = listOf(buildOwnedList(editionId = displayed.id)),
            )
            val collector = EditionSelectionCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.ownedCoverBadge shouldBe OWNED_BADGE
            job.cancel()
        }

        @Test
        fun `leaves ownedCoverBadge null when the displayed edition is not owned`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val displayed = buildEdition(id = 1)
            val ownedElsewhere = buildEdition(id = 2)
            stateFlow.value = BookDetailUiState(
                previewEdition = displayed,
                userLists = listOf(buildOwnedList(editionId = ownedElsewhere.id)),
            )
            val collector = EditionSelectionCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.ownedCoverBadge shouldBe null
            job.cancel()
        }

        @Test
        fun `re-emits trailing marks when selectedEditionId changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val current = buildEdition(id = 1)
            val other = buildEdition(id = 2)
            stateFlow.value = BookDetailUiState(
                editions = listOf(current, other),
                previewEdition = current,
                selectedEditionId = current.id,
            )
            val collector = EditionSelectionCollector()

            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(selectedEditionId = other.id)

            // ----- Assert -----
            stateFlow.value.editionTrailingMarks shouldBe mapOf(
                2 to EditionTrailingUiModel.Selected(mark = SELECTED_MARK),
                1 to EditionTrailingUiModel.Tracking(badge = TRACKING_BADGE),
            )
            job.cancel()
        }
    }
}
