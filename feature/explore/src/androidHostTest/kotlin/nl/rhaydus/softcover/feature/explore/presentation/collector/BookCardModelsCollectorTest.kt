package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.badge.CoverOverlayCorner
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardCover
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookSeries
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.mapper.railStandInCount
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_BECAUSE_YOU_READ
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_TRENDING
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_UP_NEXT
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
        coverUrl = "https://covers.test/$id.jpg",
        authors = authors,
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = bookSeries,
        positionsInSeries = emptyList(),
        isCompilation = false,
        userBook = null,
        userBookRead = null,
    )

    private fun columnOf(card: BookCardUiModel) =
        card.variant.shouldBeInstanceOf<BookCardVariant.Column>()

    @Nested
    inner class OnLaunch {
        @Test
        fun `builds one ordered card list per rail keyed by book id`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                BookCardModelsCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                trendingBooks = listOf(book(id = 1)),
                becauseYouReadBooks = listOf(
                    book(
                        id = 2,
                        authors = listOf(
                            Author(
                                id = 9,
                                name = "Ann",
                            ),
                        ),
                    ),
                ),
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
            stateFlow.value.trending.cards.map { it.key } shouldBe
                listOf(BookCardKey.Book(bookId = 1))
            stateFlow.value.becauseYouRead.cards.map { it.key } shouldBe
                listOf(BookCardKey.Book(bookId = 2))
            stateFlow.value.upNext.cards.map { it.key } shouldBe
                listOf(BookCardKey.Book(bookId = 3))
            columnOf(stateFlow.value.becauseYouRead.cards.single()).text.byline shouldBe "By Ann"
            columnOf(stateFlow.value.upNext.cards.single()).hasMenu shouldBe true
            job.cancel()
        }

        @Test
        fun `resolves each card key back to its book through the set it came from`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val job = launch {
                    BookCardModelsCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }
                val trendingBook = book(id = 1)
                val upNextBook = book(id = 3)
                val searched = book(id = 5)

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(
                    trendingBooks = listOf(trendingBook),
                    continueSeriesBooks = listOf(upNextBook),
                    queriedBooks = listOf(searched),
                )

                // ----- Assert -----
                stateFlow.value.trending[BookCardKey.Book(bookId = 1)] shouldBe trendingBook
                stateFlow.value.upNext[BookCardKey.Book(bookId = 3)] shouldBe upNextBook
                stateFlow.value.searchResults[BookCardKey.Book(bookId = 5)] shouldBe searched
                stateFlow.value.trending[BookCardKey.Book(bookId = 3)].shouldBeNull()
                job.cancel()
            }

        @Test
        fun `builds each rail's cover from the book with that rail's transition surface key`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                BookCardModelsCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            val shared = book(id = 1)

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                trendingBooks = listOf(shared),
                becauseYouReadBooks = listOf(shared),
                continueSeriesBooks = listOf(shared),
            )

            // ----- Assert -----
            columnOf(stateFlow.value.trending.cards.single()).cover.shouldNotBeNull().model shouldBe
                shared.toCoverUiModel(
                    variant = CoverVariant.Raised,
                    shareTransition = true,
                    transitionSurface = SURFACE_TRENDING,
                )
            columnOf(stateFlow.value.becauseYouRead.cards.single()).cover.shouldNotBeNull().model shouldBe
                shared.toCoverUiModel(
                    variant = CoverVariant.Raised,
                    shareTransition = true,
                    transitionSurface = SURFACE_BECAUSE_YOU_READ,
                )
            columnOf(stateFlow.value.upNext.cards.single()).cover.shouldNotBeNull().model shouldBe
                shared.toCoverUiModel(
                    variant = CoverVariant.Raised,
                    shareTransition = true,
                    transitionSurface = SURFACE_UP_NEXT,
                )
            job.cancel()
        }

        @Test
        fun `stamps the shared unreleased overlay onto a book in every rail it appears in`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val job = launch {
                    BookCardModelsCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(
                    trendingBooks = listOf(
                        book(id = 1),
                        book(id = 2),
                    ),
                    continueSeriesBooks = listOf(book(id = 1)),
                    unreleasedCoverOverlays = mapOf(1 to overlay),
                )

                // ----- Assert -----
                val trending = stateFlow.value.trending.cards
                columnOf(trending[0]).cover.shouldNotBeNull().badge shouldBe overlay
                columnOf(trending[1]).cover.shouldNotBeNull().badge.shouldBeNull()
                columnOf(stateFlow.value.upNext.cards.single()).cover.shouldNotBeNull().badge shouldBe
                    overlay
                job.cancel()
            }

        @Test
        fun `emits railStandInCount loading stand-ins per rail while its books are empty and loading`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val job = launch {
                    BookCardModelsCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(
                    trendingBooks = emptyList(),
                    becauseYouReadBooks = emptyList(),
                    continueSeriesBooks = emptyList(),
                    loadingTrendingBooks = true,
                    loadingBecauseYouReadBooks = true,
                    loadingContinueSeriesBooks = true,
                )

                // ----- Assert -----
                listOf(
                    stateFlow.value.trending.cards,
                    stateFlow.value.becauseYouRead.cards,
                    stateFlow.value.upNext.cards,
                ).forEach { cards ->
                    cards shouldHaveSize railStandInCount
                    cards.all { it.isLoading } shouldBe true
                    cards.map { it.key }.distinct() shouldHaveSize railStandInCount
                }
                job.cancel()
            }

        @Test
        fun `emits no stand-ins for a rail whose books landed empty`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                BookCardModelsCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                trendingBooks = emptyList(),
                becauseYouReadBooks = emptyList(),
                continueSeriesBooks = emptyList(),
                loadingTrendingBooks = false,
                loadingBecauseYouReadBooks = false,
                loadingContinueSeriesBooks = false,
            )

            // ----- Assert -----
            stateFlow.value.trending.cards shouldBe persistentListOf()
            stateFlow.value.becauseYouRead.cards shouldBe persistentListOf()
            stateFlow.value.upNext.cards shouldBe persistentListOf()
            job.cancel()
        }

        @Test
        fun `keeps real cards when books are present even while still loading`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val job = launch {
                    BookCardModelsCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(
                    trendingBooks = listOf(book(id = 1)),
                    loadingTrendingBooks = true,
                )

                // ----- Assert -----
                stateFlow.value.trending.cards.map { it.key } shouldBe listOf(BookCardKey.Book(bookId = 1))
                job.cancel()
            }

        @Test
        fun `emits six loading search stand-ins while a search is loading`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                BookCardModelsCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                queriedBooks = listOf(book(id = 1)),
                isLoading = true,
            )

            // ----- Assert -----
            val cards = stateFlow.value.searchResults.cards
            cards shouldHaveSize 6
            cards.all { it.isLoading } shouldBe true
            cards.map { it.key }.distinct() shouldHaveSize 6
            job.cancel()
        }

        @Test
        fun `unrelated state change does not recompute the cards`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(
                trendingBooks = listOf(book(id = 1)),
            )
            val job = launch {
                BookCardModelsCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            val cardsAfterFirstEmit = stateFlow.value.trending.cards

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(searchText = "fantasy")

            // ----- Assert -----
            stateFlow.value.trending.cards shouldBeSameInstanceAs cardsAfterFirstEmit
            job.cancel()
        }

        @Test
        fun `builds search result cards in source order, each with its own book's cover`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val job = launch {
                    BookCardModelsCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }
                val first = book(id = 1)
                val second = book(id = 2)

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(
                    queriedBooks = listOf(
                        first,
                        second,
                    ),
                )

                // ----- Assert -----
                val cards = stateFlow.value.searchResults.cards
                cards.map { it.key } shouldBe listOf(
                    BookCardKey.Book(bookId = 1),
                    BookCardKey.Book(bookId = 2),
                )
                val firstRow = cards[0].variant.shouldBeInstanceOf<BookCardVariant.Row>()
                val secondRow = cards[1].variant.shouldBeInstanceOf<BookCardVariant.Row>()
                firstRow.lead shouldBe BookCardLead.Cover(
                    cover = BookCardCover(
                        model = first.toCoverUiModel(
                            variant = CoverVariant.Flat,
                            shareTransition = true,
                            transitionSurface = null,
                        ),
                    ),
                )
                secondRow.lead shouldBe BookCardLead.Cover(
                    cover = BookCardCover(
                        model = second.toCoverUiModel(
                            variant = CoverVariant.Flat,
                            shareTransition = true,
                            transitionSurface = null,
                        ),
                    ),
                )
                job.cancel()
            }
    }
}
