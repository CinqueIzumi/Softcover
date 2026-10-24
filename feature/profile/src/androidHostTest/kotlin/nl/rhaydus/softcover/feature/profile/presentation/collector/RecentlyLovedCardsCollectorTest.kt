package nl.rhaydus.softcover.feature.profile.presentation.collector

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveBooksByIdsUseCase
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverSource
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.LovedBook
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import nl.rhaydus.softcover.core.profile.domain.model.ReadingLife
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.RecentlyLovedEntry
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RecentlyLovedCardsCollectorTest {
    private lateinit var stateFlow: MutableStateFlow<StatsAtlasUiState>
    private lateinit var eventChannel: Channel<StatsAtlasEvent>
    private lateinit var scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>
    private lateinit var libraryBooks: MutableStateFlow<List<Book>>
    private lateinit var observeBooksByIdsUseCase: ObserveBooksByIdsUseCase
    private lateinit var dependencies: StatsAtlasDependencies

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(StatsAtlasUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(StatsAtlasLocalVariables()),
            eventChannel = eventChannel,
        )
        libraryBooks = MutableStateFlow(emptyList())
        observeBooksByIdsUseCase = mockk {
            every {
                this@mockk(any())
            } returns libraryBooks
        }
        dependencies = mockk(relaxed = true) {
            every {
                observeBooksByIdsUseCase
            } returns this@RecentlyLovedCardsCollectorTest.observeBooksByIdsUseCase
        }
    }

    private fun libraryBook(
        id: Int,
        editionUrl: String? = null,
        hasUserBook: Boolean = true,
    ): Book {
        val edition = mockk<BookEdition> {
            every {
                this@mockk.id
            } returns 500 + id
            every {
                url
            } returns editionUrl
            every {
                localImagePath
            } returns null
        }

        return mockk {
            every {
                this@mockk.id
            } returns id
            every {
                title
            } returns "Library $id"
            every {
                currentEdition
            } returns edition
            every {
                defaultEdition
            } returns null
            every {
                coverUrl
            } returns "https://example.com/default$id.jpg"
            every {
                userBook
            } returns if (hasUserBook) mockk<UserBook>() else null
        }
    }

    private fun coverUrlOf(key: BookCardKey): String? {
        val card = stateFlow.value.recentlyLoved.cards.first { it.key == key }
        val column = card.variant.shouldBeInstanceOf<BookCardVariant.Column>()

        return column.cover.shouldNotBeNull().model.source
            .shouldNotBeNull()
            .shouldBeInstanceOf<CoverSource.Remote>()
            .url
    }

    private fun lovedBooks(count: Int): List<LovedBook> = List(count) {
        LovedBook(
            bookId = it + 1,
            coverUrl = "cover$it.jpg",
            title = "Title $it",
            author = "Author $it",
            ratingStars = 4.0,
            monthLabel = "May 2026",
        )
    }

    private fun readingLife(recentlyLoved: List<LovedBook>): ReadingLife = ReadingLife(
        booksByYear = emptyList(),
        pagesByYear = emptyList(),
        pagesByMonth = emptyList(),
        genres = GenreBreakdown(),
        ratings = RatingsDistribution(),
        recentlyLoved = recentlyLoved,
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `shows loading stand-ins while reading life has not loaded`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = RecentlyLovedCardsCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.recentlyLoved.cards.shouldNotBeEmpty()
            stateFlow.value.recentlyLoved.cards.forEach { it.isLoading shouldBe true }
            verify(exactly = 0) { observeBooksByIdsUseCase(any()) }
            job.cancel()
        }

        @Test
        fun `produces no cards once reading life has landed with nothing loved`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = StatsAtlasUiState(
                isLoading = false,
                readingLife = readingLife(emptyList()),
            )
            val collector = RecentlyLovedCardsCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.recentlyLoved.cards.shouldBeEmpty()
            verify(exactly = 0) { observeBooksByIdsUseCase(any()) }
            job.cancel()
        }

        @Test
        fun `keeps only the first three books in order when more are loved`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = StatsAtlasUiState(readingLife = readingLife(lovedBooks(5)))
            val collector = RecentlyLovedCardsCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.recentlyLoved.cards.map { it.key } shouldBe listOf(
                BookCardKey.Book(bookId = 1),
                BookCardKey.Book(bookId = 2),
                BookCardKey.Book(bookId = 3),
            )
            job.cancel()
        }

        @Test
        fun `returns to loading stand-ins when reading life becomes null`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = StatsAtlasUiState(readingLife = readingLife(lovedBooks(2)))
            val collector = RecentlyLovedCardsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(readingLife = null)

            // ----- Assert -----
            stateFlow.value.recentlyLoved.cards.shouldNotBeEmpty()
            stateFlow.value.recentlyLoved.cards.forEach { it.isLoading shouldBe true }
            job.cancel()
        }

        @Test
        fun `resolves each card key back to the loved book it was built from`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val books = lovedBooks(5)
            val library = libraryBook(id = 2)
            libraryBooks.value = listOf(library)
            stateFlow.value = StatsAtlasUiState(readingLife = readingLife(books))
            val collector = RecentlyLovedCardsCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            val recentlyLoved = stateFlow.value.recentlyLoved
            recentlyLoved[BookCardKey.Book(bookId = 2)] shouldBe RecentlyLovedEntry(
                loved = books[1],
                libraryBook = library,
            )
            recentlyLoved[BookCardKey.Book(bookId = 1)] shouldBe RecentlyLovedEntry(
                loved = books[0],
                libraryBook = null,
            )
            recentlyLoved[BookCardKey.Book(bookId = 4)].shouldBeNull()
            job.cancel()
        }

        @Test
        fun `maps the loved books rather than stand-ins while a refresh is still loading`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = StatsAtlasUiState(
                    isLoading = true,
                    readingLife = readingLife(lovedBooks(2)),
                )
                val collector = RecentlyLovedCardsCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                stateFlow.value.recentlyLoved.cards.map { it.key } shouldBe listOf(
                    BookCardKey.Book(bookId = 1),
                    BookCardKey.Book(bookId = 2),
                )
                stateFlow.value.recentlyLoved.cards.forEach { it.isLoading shouldBe false }
                job.cancel()
            }

        @Test
        fun `observes the library books of only the first three loved books`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = StatsAtlasUiState(readingLife = readingLife(lovedBooks(5)))
            val collector = RecentlyLovedCardsCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            verify(exactly = 1) { observeBooksByIdsUseCase(listOf(1, 2, 3)) }
            job.cancel()
        }

        @Test
        fun `uses the library book's picked edition cover over the remote cover`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            libraryBooks.value = listOf(
                libraryBook(
                    id = 1,
                    editionUrl = "https://example.com/picked.jpg",
                ),
            )
            stateFlow.value = StatsAtlasUiState(readingLife = readingLife(lovedBooks(1)))
            val collector = RecentlyLovedCardsCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            coverUrlOf(BookCardKey.Book(bookId = 1)) shouldBe "https://example.com/picked.jpg"
            job.cancel()
        }

        @Test
        fun `falls back to the remote cover when the loved book is absent from Room`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                libraryBooks.value = emptyList()
                stateFlow.value = StatsAtlasUiState(readingLife = readingLife(lovedBooks(1)))
                val collector = RecentlyLovedCardsCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                coverUrlOf(BookCardKey.Book(bookId = 1)) shouldBe "cover0.jpg"
                stateFlow.value.recentlyLoved[BookCardKey.Book(bookId = 1)]
                    .shouldNotBeNull().libraryBook.shouldBeNull()
                job.cancel()
            }

        @Test
        fun `falls back to the remote cover when the Room book has no user book`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                libraryBooks.value = listOf(
                    libraryBook(
                        id = 1,
                        editionUrl = "https://example.com/picked.jpg",
                        hasUserBook = false,
                    ),
                )
                stateFlow.value = StatsAtlasUiState(readingLife = readingLife(lovedBooks(1)))
                val collector = RecentlyLovedCardsCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                coverUrlOf(BookCardKey.Book(bookId = 1)) shouldBe "cover0.jpg"
                stateFlow.value.recentlyLoved[BookCardKey.Book(bookId = 1)]
                    .shouldNotBeNull().libraryBook.shouldBeNull()
                job.cancel()
            }

        @Test
        fun `updates the card when Room emits a book with a changed edition`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            libraryBooks.value = listOf(
                libraryBook(
                    id = 1,
                    editionUrl = "https://example.com/first.jpg",
                ),
            )
            stateFlow.value = StatsAtlasUiState(readingLife = readingLife(lovedBooks(1)))
            val collector = RecentlyLovedCardsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            libraryBooks.value = listOf(
                libraryBook(
                    id = 1,
                    editionUrl = "https://example.com/second.jpg",
                ),
            )

            // ----- Assert -----
            coverUrlOf(BookCardKey.Book(bookId = 1)) shouldBe "https://example.com/second.jpg"
            job.cancel()
        }

        @Test
        fun `keeps the loved order when Room returns the books in another order`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            libraryBooks.value = listOf(
                libraryBook(id = 3),
                libraryBook(id = 1),
                libraryBook(id = 2),
            )
            stateFlow.value = StatsAtlasUiState(readingLife = readingLife(lovedBooks(3)))
            val collector = RecentlyLovedCardsCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.recentlyLoved.cards.map { it.key } shouldBe listOf(
                BookCardKey.Book(bookId = 1),
                BookCardKey.Book(bookId = 2),
                BookCardKey.Book(bookId = 3),
            )
            job.cancel()
        }
    }
}
