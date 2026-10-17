package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.badge.CoverOverlayCorner
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardCover
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.transition.BookTransitionElement
import nl.rhaydus.softcover.core.component.transition.BookTransitionKey
import nl.rhaydus.softcover.core.component.transition.bookTransitionKey
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
        rating: Double = 0.0,
    ): Book = Book(
        id = id,
        title = title,
        editions = emptyList(),
        defaultEdition = null,
        rating = rating,
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
                trendingBooks = persistentListOf(book(id = 1)),
                becauseYouReadBooks = persistentListOf(
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
                continueSeriesBooks = persistentListOf(
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
                    trendingBooks = persistentListOf(trendingBook),
                    continueSeriesBooks = persistentListOf(upNextBook),
                    queriedBooks = persistentListOf(searched),
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
                trendingBooks = persistentListOf(shared),
                becauseYouReadBooks = persistentListOf(shared),
                continueSeriesBooks = persistentListOf(shared),
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
                    trendingBooks = persistentListOf(
                        book(id = 1),
                        book(id = 2),
                    ),
                    continueSeriesBooks = persistentListOf(book(id = 1)),
                    unreleasedCoverOverlays = persistentMapOf(1 to overlay),
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
                    trendingBooks = persistentListOf(),
                    becauseYouReadBooks = persistentListOf(),
                    continueSeriesBooks = persistentListOf(),
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
                trendingBooks = persistentListOf(),
                becauseYouReadBooks = persistentListOf(),
                continueSeriesBooks = persistentListOf(),
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
                    trendingBooks = persistentListOf(book(id = 1)),
                    loadingTrendingBooks = true,
                )

                // ----- Assert -----
                stateFlow.value.trending.cards.map { it.key } shouldBe listOf(BookCardKey.Book(bookId = 1))
                job.cancel()
            }

        @Test
        fun `emits six loading search stand-ins while a search is loading with no results on screen`() =
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
                    queriedBooks = persistentListOf(),
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
        fun `keeps the mapped search cards instead of stand-ins while a refresh is loading`() =
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
                    queriedBooks = persistentListOf(book(id = 1), book(id = 2)),
                    isLoading = true,
                )

                // ----- Assert -----
                val cards = stateFlow.value.searchResults.cards
                cards.map { it.key } shouldBe listOf(
                    BookCardKey.Book(bookId = 1),
                    BookCardKey.Book(bookId = 2),
                )
                cards.none { it.isLoading } shouldBe true
                job.cancel()
            }

        @Test
        fun `emits the mapped search cards when not loading`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                BookCardModelsCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                queriedBooks = persistentListOf(book(id = 7)),
                isLoading = false,
            )

            // ----- Assert -----
            val cards = stateFlow.value.searchResults.cards
            cards.map { it.key } shouldBe listOf(BookCardKey.Book(bookId = 7))
            cards.none { it.isLoading } shouldBe true
            job.cancel()
        }

        @Test
        fun `unrelated state change does not recompute the cards`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(
                trendingBooks = persistentListOf(book(id = 1)),
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
                    queriedBooks = persistentListOf(
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

    @Nested
    inner class TransitionKeys {
        private fun titleKey(
            bookId: Int,
            surface: String?,
        ): BookTransitionKey = bookTransitionKey(
            element = BookTransitionElement.Title,
            bookId = bookId,
            surface = surface,
        )

        private fun ratingKey(
            bookId: Int,
            surface: String?,
        ): BookTransitionKey = bookTransitionKey(
            element = BookTransitionElement.Rating,
            bookId = bookId,
            surface = surface,
        )

        private fun BookCardUiModel.ratingKeys(): List<BookTransitionKey?> = columnOf(this).text.supportingLines
            .flatten()
            .filterIsInstance<BookCardLinePart.Rating>()
            .map { it.transitionKey }

        @Test
        fun `each rail's title key carries that rail's surface, matching its cover key surface`() =
            runTest(UnconfinedTestDispatcher()) {
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
                    trendingBooks = persistentListOf(shared),
                    becauseYouReadBooks = persistentListOf(shared),
                    continueSeriesBooks = persistentListOf(shared),
                )

                // ----- Assert -----
                val trending = columnOf(stateFlow.value.trending.cards.single())
                val becauseYouRead = columnOf(stateFlow.value.becauseYouRead.cards.single())
                val upNext = columnOf(stateFlow.value.upNext.cards.single())
                trending.text.titleTransitionKey shouldBe titleKey(
                    bookId = 1,
                    surface = SURFACE_TRENDING,
                )
                becauseYouRead.text.titleTransitionKey shouldBe titleKey(
                    bookId = 1,
                    surface = SURFACE_BECAUSE_YOU_READ,
                )
                upNext.text.titleTransitionKey shouldBe titleKey(
                    bookId = 1,
                    surface = SURFACE_UP_NEXT,
                )
                trending.cover.shouldNotBeNull().model.sharedTransitionKey shouldBe bookTransitionKey(
                    element = BookTransitionElement.Cover,
                    bookId = 1,
                    surface = SURFACE_TRENDING,
                )
                job.cancel()
            }

        @Test
        fun `one book on three rails gets three distinct title keys`() = runTest(UnconfinedTestDispatcher()) {
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
                trendingBooks = persistentListOf(shared),
                becauseYouReadBooks = persistentListOf(shared),
                continueSeriesBooks = persistentListOf(shared),
            )

            // ----- Assert -----
            val keys = listOf(
                columnOf(stateFlow.value.trending.cards.single()).text.titleTransitionKey,
                columnOf(stateFlow.value.becauseYouRead.cards.single()).text.titleTransitionKey,
                columnOf(stateFlow.value.upNext.cards.single()).text.titleTransitionKey,
            )
            keys.toSet() shouldHaveSize 3
            job.cancel()
        }

        @Test
        fun `the trending rating key carries the trending surface`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                BookCardModelsCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                trendingBooks = persistentListOf(
                    book(
                        id = 1,
                        rating = 4.5,
                    ),
                ),
            )

            // ----- Assert -----
            stateFlow.value.trending.cards.single().ratingKeys() shouldBe listOf(
                ratingKey(
                    bookId = 1,
                    surface = SURFACE_TRENDING,
                ),
            )
            job.cancel()
        }

        @Test
        fun `search results carry unsuffixed title and rating keys`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                BookCardModelsCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                queriedBooks = persistentListOf(
                    book(
                        id = 5,
                        rating = 4.5,
                    ),
                ),
            )

            // ----- Assert -----
            val row = stateFlow.value.searchResults.cards.single().variant
                .shouldBeInstanceOf<BookCardVariant.Row>()
            row.text.titleTransitionKey shouldBe titleKey(
                bookId = 5,
                surface = null,
            )
            val ratings = row.text.supportingLines.flatten().filterIsInstance<BookCardLinePart.Rating>()
            ratings.map { it.transitionKey } shouldBe listOf(
                ratingKey(
                    bookId = 5,
                    surface = null,
                ),
            )
            job.cancel()
        }
    }

    @Nested
    inner class BecauseYouReadGenreControl {
        private fun TestScope.launchCollector() = launch {
            BookCardModelsCollector().onLaunch(
                scope = scope,
                dependencies = dependencies,
            )
        }

        @Test
        fun `is hidden when there is no genre even with books`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                becauseYouReadGenre = null,
                becauseYouReadBooks = persistentListOf(book(id = 1)),
            )

            // ----- Assert -----
            stateFlow.value.becauseYouReadGenreControl.visible shouldBe false
            job.cancel()
        }

        @Test
        fun `is hidden when the genre is set but loading ended with no books`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                becauseYouReadGenre = "Horror",
                loadingBecauseYouReadBooks = false,
                becauseYouReadBooks = persistentListOf(),
            )

            // ----- Assert -----
            stateFlow.value.becauseYouReadGenreControl.visible shouldBe false
            job.cancel()
        }

        @Test
        fun `is visible and loading while the genre's books load`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                becauseYouReadGenre = "Horror",
                loadingBecauseYouReadBooks = true,
                becauseYouReadBooks = persistentListOf(),
            )

            // ----- Assert -----
            val control = stateFlow.value.becauseYouReadGenreControl
            control.visible shouldBe true
            control.isLoading shouldBe true
            job.cancel()
        }

        @Test
        fun `is visible and not loading once books exist even while loading continues`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val job = launchCollector()

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(
                    becauseYouReadGenre = "Horror",
                    loadingBecauseYouReadBooks = true,
                    becauseYouReadBooks = persistentListOf(book(id = 1)),
                )

                // ----- Assert -----
                val control = stateFlow.value.becauseYouReadGenreControl
                control.visible shouldBe true
                control.isLoading shouldBe false
                job.cancel()
            }

        @Test
        fun `keeps the label, options and sheet state the sheet collector wrote`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = stateFlow.value.copy(
                    becauseYouReadGenreControl = stateFlow.value.becauseYouReadGenreControl.copy(
                        label = "GENRE · HORROR",
                        hasOptions = true,
                        sheetVisible = true,
                    ),
                )
                val job = launchCollector()

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(
                    becauseYouReadGenre = "Horror",
                    becauseYouReadBooks = persistentListOf(book(id = 1)),
                )

                // ----- Assert -----
                val control = stateFlow.value.becauseYouReadGenreControl
                control.label shouldBe "GENRE · HORROR"
                control.hasOptions shouldBe true
                control.sheetVisible shouldBe true
                job.cancel()
            }
    }
}
