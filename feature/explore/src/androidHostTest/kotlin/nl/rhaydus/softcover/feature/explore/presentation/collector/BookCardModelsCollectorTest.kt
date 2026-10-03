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
            stateFlow.value.trendingCards.shouldNotBeNull().map { it.key } shouldBe
                listOf(BookCardKey.Book(bookId = 1))
            stateFlow.value.becauseYouReadCards.shouldNotBeNull().map { it.key } shouldBe
                listOf(BookCardKey.Book(bookId = 2))
            stateFlow.value.upNextCards.shouldNotBeNull().map { it.key } shouldBe
                listOf(BookCardKey.Book(bookId = 3))
            columnOf(stateFlow.value.becauseYouReadCards.shouldNotBeNull().single()).text.byline shouldBe "By Ann"
            columnOf(stateFlow.value.upNextCards.shouldNotBeNull().single()).menuLabel shouldBe "More options"
            job.cancel()
        }

        @Test
        fun `gives each rail its own cover for the same book`() = runTest(UnconfinedTestDispatcher()) {
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
                trendingCovers = mapOf(1 to trendingCover),
                becauseYouReadCovers = mapOf(1 to becauseCover),
                continueSeriesCovers = mapOf(1 to upNextCover),
            )

            // ----- Assert -----
            columnOf(stateFlow.value.trendingCards.shouldNotBeNull().single()).cover.shouldNotBeNull().model shouldBe
                trendingCover
            columnOf(stateFlow.value.becauseYouReadCards.shouldNotBeNull().single()).cover.shouldNotBeNull().model shouldBe
                becauseCover
            columnOf(stateFlow.value.upNextCards.shouldNotBeNull().single()).cover.shouldNotBeNull().model shouldBe
                upNextCover
            job.cancel()
        }

        @Test
        fun `leaves the cover null when a book has no cover model`() = runTest(UnconfinedTestDispatcher()) {
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
            )

            // ----- Assert -----
            columnOf(stateFlow.value.trendingCards.shouldNotBeNull().single()).cover.shouldBeNull()
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
                    trendingCovers = mapOf(
                        1 to trendingCover,
                        2 to trendingCover,
                    ),
                    continueSeriesCovers = mapOf(1 to upNextCover),
                    unreleasedCoverOverlays = mapOf(1 to overlay),
                )

                // ----- Assert -----
                val trending = stateFlow.value.trendingCards.shouldNotBeNull()
                columnOf(trending[0]).cover.shouldNotBeNull().badge shouldBe overlay
                columnOf(trending[1]).cover.shouldNotBeNull().badge.shouldBeNull()
                columnOf(stateFlow.value.upNextCards.shouldNotBeNull().single()).cover.shouldNotBeNull().badge shouldBe
                    overlay
                job.cancel()
            }

        @Test
        fun `recomputes when covers arrive after the books`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                BookCardModelsCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            stateFlow.value = stateFlow.value.copy(
                trendingBooks = listOf(book(id = 1)),
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                trendingCovers = mapOf(1 to trendingCover),
            )

            // ----- Assert -----
            columnOf(stateFlow.value.trendingCards.shouldNotBeNull().single()).cover.shouldNotBeNull().model shouldBe
                trendingCover
            job.cancel()
        }

        @Test
        fun `empties the card lists when the books are cleared and loading is done`() = runTest(UnconfinedTestDispatcher()) {
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

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                trendingBooks = emptyList(),
                loadingTrendingBooks = false,
            )

            // ----- Assert -----
            stateFlow.value.trendingCards shouldBe persistentListOf()
            job.cancel()
        }

        @Test
        fun `keeps every list null while the books are empty and still loading`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val job = launch {
                    BookCardModelsCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(isLoading = true)

                // ----- Assert -----
                stateFlow.value.trendingCards.shouldBeNull()
                stateFlow.value.becauseYouReadCards.shouldBeNull()
                stateFlow.value.upNextCards.shouldBeNull()
                stateFlow.value.queriedBookCards.shouldBeNull()
                job.cancel()
            }

        @Test
        fun `turns the trending rail from null into its cards once the books arrive`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val job = launch {
                    BookCardModelsCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }
                val before = stateFlow.value.trendingCards

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(
                    trendingBooks = listOf(book(id = 1)),
                    loadingTrendingBooks = false,
                )

                // ----- Assert -----
                before.shouldBeNull()
                stateFlow.value.trendingCards.shouldNotBeNull() shouldHaveSize 1
                job.cancel()
            }

        @Test
        fun `keeps the queried cards null while a search is loading`() = runTest(UnconfinedTestDispatcher()) {
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
                queriedBookCovers = mapOf(1 to mockk<CoverUiModel>()),
                isLoading = true,
            )

            // ----- Assert -----
            stateFlow.value.queriedBookCards.shouldBeNull()
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
            val cardsAfterFirstEmit = stateFlow.value.trendingCards

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(searchText = "fantasy")

            // ----- Assert -----
            stateFlow.value.trendingCards shouldBeSameInstanceAs cardsAfterFirstEmit
            job.cancel()
        }

        @Test
        fun `builds search result cards in source order with each book's own cover`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val job = launch {
                    BookCardModelsCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }
                val firstCover = mockk<CoverUiModel>()
                val secondCover = mockk<CoverUiModel>()

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(
                    queriedBooks = listOf(
                        book(id = 1),
                        book(id = 2),
                    ),
                    queriedBookCovers = mapOf(
                        1 to firstCover,
                        2 to secondCover,
                    ),
                )

                // ----- Assert -----
                val cards = stateFlow.value.queriedBookCards.shouldNotBeNull()
                cards.map { it.key } shouldBe listOf(
                    BookCardKey.Book(bookId = 1),
                    BookCardKey.Book(bookId = 2),
                )
                val first = cards[0].variant.shouldBeInstanceOf<BookCardVariant.Row>()
                val second = cards[1].variant.shouldBeInstanceOf<BookCardVariant.Row>()
                first.lead shouldBe BookCardLead.Cover(cover = BookCardCover(model = firstCover))
                second.lead shouldBe BookCardLead.Cover(cover = BookCardCover(model = secondCover))
                job.cancel()
            }

        @Test
        fun `omits a queried book that has no mapped cover from the search result cards`() =
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
                    queriedBooks = listOf(
                        book(id = 1),
                        book(id = 2),
                    ),
                    queriedBookCovers = mapOf(1 to mockk<CoverUiModel>()),
                )

                // ----- Assert -----
                stateFlow.value.queriedBookCards.shouldNotBeNull().map { it.key } shouldBe
                    listOf(BookCardKey.Book(bookId = 1))
                job.cancel()
            }

        @Test
        fun `empties the search result cards when the queried books are cleared`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = stateFlow.value.copy(
                    queriedBooks = listOf(book(id = 1)),
                )
                val job = launch {
                    BookCardModelsCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(queriedBooks = emptyList())

                // ----- Assert -----
                stateFlow.value.queriedBookCards shouldBe persistentListOf()
                job.cancel()
            }
    }
}
