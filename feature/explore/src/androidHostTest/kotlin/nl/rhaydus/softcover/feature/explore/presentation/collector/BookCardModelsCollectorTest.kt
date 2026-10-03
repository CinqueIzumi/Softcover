package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.badge.CoverOverlayCorner
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookSeries
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookCardModelsCollectorTest {
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>

    private val trendingCover = mockk<CoverUiModel>()
    private val becauseCover = mockk<CoverUiModel>()
    private val upNextCover = mockk<CoverUiModel>()

    private val overlay = CoverOverlayUiModel(
        badge = BadgeUiModel(
            label = "Out soon",
            tone = BadgeTone.Primary,
        ),
        corner = CoverOverlayCorner.TopStart,
        grayscale = false,
    )

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(ExploreScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ExploreLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk<ExploreDependencies>(relaxed = true)
    }

    private fun book(
        id: Int,
        title: String = "Title $id",
        authors: List<Author> = emptyList(),
        bookSeries: BookSeries? = null,
    ): Book = Book(
        id = id,
        title = title,
        editions = emptyList(),
        defaultEdition = null,
        rating = 0.0,
        description = "",
        releaseYear = 2020,
        coverUrl = "",
        authors = authors,
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = bookSeries,
        positionsInSeries = emptyList(),
        isCompilation = false,
        userBook = null,
        userBookRead = null,
    )

    private fun stackedOf(card: BookCardUiModel) =
        card.variant.shouldBeInstanceOf<BookCardVariant.Stacked>()

    @Nested
    inner class OnLaunch {
        @Test
        fun `builds one card map per rail keyed by book id`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch { BookCardModelsCollector().onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                trendingBooks = listOf(book(id = 1)),
                becauseYouReadBooks = listOf(book(
                    id = 2,
                    authors = listOf(Author(
                        id = 9,
                        name = "Ann",
                    ),),
                ),),
                continueSeriesBooks = listOf(
                    book(
                        id = 3,
                        bookSeries = BookSeries(
                            id = 4,
                            name = "Earthsea",
                            amountOfBooks = 3,
                        ),
                    ),
                ),
            )

            // ----- Assert -----
            stateFlow.value.trendingCards.keys shouldBe setOf(1)
            stateFlow.value.becauseYouReadCards.keys shouldBe setOf(2)
            stateFlow.value.upNextCards.keys shouldBe setOf(3)
            stateFlow.value.trendingCards.getValue(1).key shouldBe BookCardKey.Book(bookId = 1)
            stackedOf(stateFlow.value.becauseYouReadCards.getValue(2)).text.byline shouldBe "By Ann"
            stackedOf(stateFlow.value.upNextCards.getValue(3)).menuLabel shouldBe "More options"
            job.cancel()
        }

        @Test
        fun `gives each rail its own cover for the same book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch { BookCardModelsCollector().onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }
            val shared = book(id = 1)

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                trendingBooks = listOf(shared),
                becauseYouReadBooks = listOf(shared),
                continueSeriesBooks = listOf(shared),
                trendingCovers = mapOf(1 to trendingCover),
                becauseYouReadCovers = mapOf(1 to becauseCover),
                continueSeriesCovers = mapOf(1 to upNextCover),
            )

            // ----- Assert -----
            stackedOf(stateFlow.value.trendingCards.getValue(1)).cover shouldBe trendingCover
            stackedOf(stateFlow.value.becauseYouReadCards.getValue(1)).cover shouldBe becauseCover
            stackedOf(stateFlow.value.upNextCards.getValue(1)).cover shouldBe upNextCover
            job.cancel()
        }

        @Test
        fun `leaves the cover null when a book has no cover model`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch { BookCardModelsCollector().onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(trendingBooks = listOf(book(id = 1)))

            // ----- Assert -----
            stackedOf(stateFlow.value.trendingCards.getValue(1)).cover.shouldBeNull()
            job.cancel()
        }

        @Test
        fun `stamps the shared unreleased overlay onto a book in every rail it appears in`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val job = launch { BookCardModelsCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                ) }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(
                    trendingBooks = listOf(book(id = 1), book(id = 2)),
                    continueSeriesBooks = listOf(book(id = 1)),
                    unreleasedCoverOverlays = mapOf(1 to overlay),
                )

                // ----- Assert -----
                stateFlow.value.trendingCards.getValue(1).decorations.coverBadge shouldBe overlay
                stateFlow.value.trendingCards.getValue(2).decorations.coverBadge.shouldBeNull()
                stateFlow.value.upNextCards.getValue(1).decorations.coverBadge shouldBe overlay
                job.cancel()
            }

        @Test
        fun `recomputes when covers arrive after the books`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch { BookCardModelsCollector().onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }
            stateFlow.value = stateFlow.value.copy(trendingBooks = listOf(book(id = 1)))

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(trendingCovers = mapOf(1 to trendingCover))

            // ----- Assert -----
            stackedOf(stateFlow.value.trendingCards.getValue(1)).cover.shouldNotBeNull() shouldBe trendingCover
            job.cancel()
        }

        @Test
        fun `empties the card maps when the books are cleared`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(trendingBooks = listOf(book(id = 1)))
            val job = launch { BookCardModelsCollector().onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(trendingBooks = emptyList())

            // ----- Assert -----
            stateFlow.value.trendingCards shouldBe persistentMapOf()
            job.cancel()
        }

        @Test
        fun `unrelated state change does not recompute the cards`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(trendingBooks = listOf(book(id = 1)))
            val job = launch { BookCardModelsCollector().onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }
            val cardsAfterFirstEmit = stateFlow.value.trendingCards

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(searchText = "fantasy")

            // ----- Assert -----
            (stateFlow.value.trendingCards === cardsAfterFirstEmit) shouldBe true
            job.cancel()
        }
    }
}
