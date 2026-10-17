package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.badge.BadgeSize
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.HeroEditionUiModel
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private val OWNED_BADGE = BadgeUiModel(
    label = "Owned",
    tone = BadgeTone.Scrim,
    size = BadgeSize.Regular,
    icon = SoftcoverIcon.Check,
)

class EditionSelectionCollectorTest {
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
        fun `sets ownedCoverBadge when the displayed edition is owned`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val displayed = buildEdition(id = 1)
            stateFlow.value = buildBookDetailUiState().copy(
                previewEdition = displayed,
                userLists = persistentListOf(buildOwnedList(editionId = displayed.id)),
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
            stateFlow.value = buildBookDetailUiState().copy(
                previewEdition = displayed,
                userLists = persistentListOf(buildOwnedList(editionId = ownedElsewhere.id)),
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
        fun `sets heroEdition from the displayed edition`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                previewEdition = buildEdition(id = 1).copy(pages = 200),
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
            stateFlow.value.heroEdition shouldBe HeroEditionUiModel(
                authors = "",
                length = "200",
                isAudiobook = false,
            )
            job.cancel()
        }

        @Test
        fun `leaves heroEdition null while no edition is displayed`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = EditionSelectionCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.heroEdition shouldBe null
            job.cancel()
        }

        @Test
        fun `updates heroEdition when the displayed edition changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                previewEdition = buildEdition(id = 1).copy(pages = 200),
            )
            val collector = EditionSelectionCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.update { it.copy(previewEdition = buildEdition(id = 2).copy(pages = 450)) }

            // ----- Assert -----
            stateFlow.value.heroEdition shouldBe HeroEditionUiModel(
                authors = "",
                length = "450",
                isAudiobook = false,
            )
            job.cancel()
        }
    }
}
