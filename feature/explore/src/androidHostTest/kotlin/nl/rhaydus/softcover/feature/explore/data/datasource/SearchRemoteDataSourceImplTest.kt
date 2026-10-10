package nl.rhaydus.softcover.feature.explore.data.datasource

import app.cash.turbine.test
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.GetBooksByGenreTagQuery
import nl.rhaydus.softcover.GetBooksByIdsQuery
import nl.rhaydus.softcover.GetBooksByMoodTagQuery
import nl.rhaydus.softcover.GetIdsForQuery
import nl.rhaydus.softcover.core.book.data.mapper.toBook
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.network.helper.safeQuery
import nl.rhaydus.softcover.feature.explore.data.mapper.toTypesenseSort
import nl.rhaydus.softcover.feature.explore.domain.model.ExploreSortMode
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.fragment.BookDetailFragment
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

// Mirrors SearchRemoteDataSourceImpl's private MOOD_BOOKS_LIMIT / SEARCH_RESULTS_PAGE_SIZE
// constants (both 25) - kept here as literals since those constants aren't exposed to callers.
private const val MOOD_BOOKS_LIMIT = 25
private const val SEARCH_RESULTS_PAGE_SIZE = 25

class SearchRemoteDataSourceImplTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun mockStatics() {
            mockkStatic("nl.rhaydus.softcover.core.network.helper.ApolloExtensionsKt")
            mockkStatic("nl.rhaydus.softcover.core.book.data.mapper.BookMapperKt")
            mockkObject(GetBooksByIdsQuery.Data.Book.Companion)
            mockkObject(GetBooksByMoodTagQuery.Data.Taggable_count.Book.Companion)
            mockkObject(GetBooksByGenreTagQuery.Data.Taggable_count.Book.Companion)
        }

        @JvmStatic
        @AfterAll
        fun unmockStatics() {
            unmockkAll()
        }
    }

    private lateinit var apolloClient: ApolloClient
    private lateinit var dataSource: SearchRemoteDataSourceImpl

    @BeforeEach
    fun setUp() {
        apolloClient = mockk()
        dataSource = SearchRemoteDataSourceImpl(apolloClient = apolloClient)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    private fun stubBook(id: Int = 1): Book = mockk {
        every {
            this@mockk.id
        } returns id
    }

    private fun stubBookEntry(
        book: Book,
    ): GetBooksByIdsQuery.Data.Book {
        val bookEntry = mockk<GetBooksByIdsQuery.Data.Book>()
        val bookDetailFragment = mockk<BookDetailFragment>()

        every {
            with(GetBooksByIdsQuery.Data.Book.Companion) { bookEntry.bookDetailFragment() }
        } returns bookDetailFragment

        every {
            bookDetailFragment.toBook()
        } returns book

        return bookEntry
    }

    private fun stubIdsQuery(
        name: String,
        page: Int,
        ids: List<Int?>,
    ) {
        val idsQueryData = mockk<GetIdsForQuery.Data>()
        val searchData = mockk<GetIdsForQuery.Data.Search>()

        every {
            idsQueryData.search
        } returns searchData

        every {
            searchData.ids
        } returns ids

        coEvery {
            apolloClient.safeQuery(
                query = GetIdsForQuery(
                    query = name,
                    sort = Optional.Present(ExploreSortMode.RELEVANCE.toTypesenseSort()),
                    page = if (page > 1) Optional.Present(page) else Optional.Absent,
                ),
            )
        } returns idsQueryData
    }

    private fun stubBooksQuery(
        ids: List<Int>,
        books: List<GetBooksByIdsQuery.Data.Book>,
    ) {
        val booksQueryData = mockk<GetBooksByIdsQuery.Data>()

        every {
            booksQueryData.books
        } returns books

        coEvery {
            apolloClient.safeQuery(
                query = GetBooksByIdsQuery(ids = ids),
                fetchPolicy = any(),
            )
        } returns booksQueryData
    }

    private fun stubMoodBookRow(book: Book?): GetBooksByMoodTagQuery.Data.Taggable_count {
        val row = mockk<GetBooksByMoodTagQuery.Data.Taggable_count>()

        if (book == null) {
            every {
                row.book
            } returns null
            return row
        }

        val bookEntry = mockk<GetBooksByMoodTagQuery.Data.Taggable_count.Book>()
        val bookDetailFragment = mockk<BookDetailFragment>()

        every {
            row.book
        } returns bookEntry

        every {
            with(GetBooksByMoodTagQuery.Data.Taggable_count.Book.Companion) { bookEntry.bookDetailFragment() }
        } returns bookDetailFragment

        every {
            bookDetailFragment.toBook()
        } returns book

        return row
    }

    private fun stubMoodQuery(
        mood: MoodTag,
        page: Int,
        rows: List<GetBooksByMoodTagQuery.Data.Taggable_count>,
    ) {
        val offset = (page - 1).coerceAtLeast(0) * MOOD_BOOKS_LIMIT
        val responseData = mockk<GetBooksByMoodTagQuery.Data>()

        every {
            responseData.taggable_counts
        } returns rows

        coEvery {
            apolloClient.safeQuery(
                query = GetBooksByMoodTagQuery(
                    slug = mood.slug,
                    limit = MOOD_BOOKS_LIMIT,
                    offset = if (offset > 0) Optional.Present(offset) else Optional.Absent,
                ),
            )
        } returns responseData
    }

    private fun stubGenreBookRow(book: Book?): GetBooksByGenreTagQuery.Data.Taggable_count {
        val row = mockk<GetBooksByGenreTagQuery.Data.Taggable_count>()

        if (book == null) {
            every {
                row.book
            } returns null
            return row
        }

        val bookEntry = mockk<GetBooksByGenreTagQuery.Data.Taggable_count.Book>()
        val bookDetailFragment = mockk<BookDetailFragment>()

        every {
            row.book
        } returns bookEntry

        every {
            with(GetBooksByGenreTagQuery.Data.Taggable_count.Book.Companion) { bookEntry.bookDetailFragment() }
        } returns bookDetailFragment

        every {
            bookDetailFragment.toBook()
        } returns book

        return row
    }

    private fun stubGenreQuery(
        genre: String,
        limit: Int,
        rows: List<GetBooksByGenreTagQuery.Data.Taggable_count>,
    ) {
        val responseData = mockk<GetBooksByGenreTagQuery.Data>()

        every {
            responseData.taggable_counts
        } returns rows

        coEvery {
            apolloClient.safeQuery(
                query = GetBooksByGenreTagQuery(
                    genre = genre,
                    limit = limit,
                ),
            )
        } returns responseData
    }


    @Nested
    inner class SearchForName {
        @Test
        fun `orders queriedBooks by the id order of the search result`() = runTest {
            // ----- Arrange -----
            val ids = listOf(3, 1, 2)
            stubIdsQuery(
                name = "dune",
                page = 1,
                ids = ids,
            )
            stubBooksQuery(
                ids = ids,
                books = listOf(1, 2, 3).map { id -> stubBookEntry(book = stubBook(id = id)) },
            )

            // ----- Act -----
            dataSource.searchForName(
                name = "dune",
                userId = 1,
                sortMode = ExploreSortMode.RELEVANCE,
            )

            // ----- Assert -----
            dataSource.queriedBooks.first().map { it.id } shouldBe listOf(3, 1, 2)
        }

        @Test
        fun `throws when search returns null ids`() = runTest {
            // ----- Arrange -----
            val idsQueryData = mockk<GetIdsForQuery.Data>()

            coEvery {
                apolloClient.safeQuery(query = any<GetIdsForQuery>())
            } returns idsQueryData

            every {
                idsQueryData.search
            } returns null

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.searchForName(
                    name = "something",
                    userId = 1,
                    sortMode = ExploreSortMode.RELEVANCE,
                )
            }
        }

        @Test
        fun `filters null ids before fetching books and drops entries without a book fragment`() = runTest {
            // ----- Arrange -----
            stubIdsQuery(
                name = "mixed",
                page = 1,
                ids = listOf(10, null, 20),
            )
            val entryWithoutFragment = mockk<GetBooksByIdsQuery.Data.Book>()
            every {
                with(GetBooksByIdsQuery.Data.Book.Companion) { entryWithoutFragment.bookDetailFragment() }
            } returns null
            stubBooksQuery(
                ids = listOf(10, 20),
                books = listOf(stubBookEntry(book = stubBook(id = 10)), entryWithoutFragment),
            )

            // ----- Act -----
            dataSource.searchForName(
                name = "mixed",
                userId = 1,
                sortMode = ExploreSortMode.RELEVANCE,
            )

            // ----- Assert -----
            dataSource.queriedBooks.first().map { it.id } shouldBe listOf(10)
        }

        @Test
        fun `sets queriedBooksHasMore true when the raw id count reaches the page size`() = runTest {
            // ----- Arrange -----
            val ids = List(SEARCH_RESULTS_PAGE_SIZE) { it + 1 }
            stubIdsQuery(
                name = "epic",
                page = 1,
                ids = ids,
            )
            stubBooksQuery(
                ids = ids,
                books = ids.map { id -> stubBookEntry(book = stubBook(id = id)) },
            )

            // ----- Act -----
            dataSource.searchForName(
                name = "epic",
                userId = 1,
                sortMode = ExploreSortMode.RELEVANCE,
            )

            // ----- Assert -----
            dataSource.queriedBooksHasMore.first() shouldBe true
        }

        @Test
        fun `sets queriedBooksHasMore false and keeps queriedBooks empty when no ids match`() = runTest {
            // ----- Arrange -----
            stubIdsQuery(
                name = "nothing",
                page = 1,
                ids = emptyList(),
            )

            // ----- Act -----
            dataSource.searchForName(
                name = "nothing",
                userId = 1,
                sortMode = ExploreSortMode.RELEVANCE,
            )

            // ----- Assert -----
            dataSource.queriedBooks.first() shouldBe emptyList()
            dataSource.queriedBooksHasMore.first() shouldBe false
        }

        @Test
        fun `appends later pages and dedupes ids shared with the previous page`() = runTest {
            // ----- Arrange -----
            stubIdsQuery(
                name = "series",
                page = 1,
                ids = listOf(1, 2),
            )
            stubBooksQuery(
                ids = listOf(1, 2),
                books = listOf(1, 2).map { id -> stubBookEntry(book = stubBook(id = id)) },
            )
            dataSource.searchForName(
                name = "series",
                userId = 1,
                sortMode = ExploreSortMode.RELEVANCE,
                page = 1,
            )
            stubIdsQuery(
                name = "series",
                page = 2,
                ids = listOf(2, 3),
            )
            stubBooksQuery(
                ids = listOf(2, 3),
                books = listOf(2, 3).map { id -> stubBookEntry(book = stubBook(id = id)) },
            )

            // ----- Act -----
            dataSource.searchForName(
                name = "series",
                userId = 1,
                sortMode = ExploreSortMode.RELEVANCE,
                page = 2,
            )

            // ----- Assert -----
            dataSource.queriedBooks.first().map { it.id } shouldBe listOf(1, 2, 3)
        }

        @Test
        fun `preserves the previous queriedBooks when a later page returns no ids`() = runTest {
            // ----- Arrange -----
            stubIdsQuery(
                name = "series",
                page = 1,
                ids = listOf(1),
            )
            stubBooksQuery(
                ids = listOf(1),
                books = listOf(stubBookEntry(book = stubBook(id = 1))),
            )
            dataSource.searchForName(
                name = "series",
                userId = 1,
                sortMode = ExploreSortMode.RELEVANCE,
                page = 1,
            )
            stubIdsQuery(
                name = "series",
                page = 2,
                ids = emptyList(),
            )

            // ----- Act -----
            dataSource.searchForName(
                name = "series",
                userId = 1,
                sortMode = ExploreSortMode.RELEVANCE,
                page = 2,
            )

            // ----- Assert -----
            dataSource.queriedBooks.first().map { it.id } shouldBe listOf(1)
        }

        @Test
        fun `keeps the previous queriedBooks until a fresh search lands and then replaces them`() = runTest {
            // ----- Arrange -----
            stubIdsQuery(
                name = "first",
                page = 1,
                ids = listOf(1),
            )
            stubBooksQuery(
                ids = listOf(1),
                books = listOf(stubBookEntry(book = stubBook(id = 1))),
            )
            dataSource.searchForName(
                name = "first",
                userId = 1,
                sortMode = ExploreSortMode.RELEVANCE,
                page = 1,
            )
            stubIdsQuery(
                name = "second",
                page = 1,
                ids = listOf(2),
            )
            stubBooksQuery(
                ids = listOf(2),
                books = listOf(stubBookEntry(book = stubBook(id = 2))),
            )

            // ----- Act & Assert -----
            dataSource.queriedBooks.test {
                awaitItem().map { it.id } shouldBe listOf(1)

                dataSource.searchForName(
                    name = "second",
                    userId = 1,
                    sortMode = ExploreSortMode.RELEVANCE,
                    page = 1,
                )

                awaitItem().map { it.id } shouldBe listOf(2)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `writes an empty queriedBooks on a page one search with no match, replacing the old list`() = runTest {
            // ----- Arrange -----
            stubIdsQuery(
                name = "first",
                page = 1,
                ids = listOf(1),
            )
            stubBooksQuery(
                ids = listOf(1),
                books = listOf(stubBookEntry(book = stubBook(id = 1))),
            )
            dataSource.searchForName(
                name = "first",
                userId = 1,
                sortMode = ExploreSortMode.RELEVANCE,
                page = 1,
            )
            stubIdsQuery(
                name = "nothing",
                page = 1,
                ids = emptyList(),
            )

            // ----- Act -----
            dataSource.searchForName(
                name = "nothing",
                userId = 1,
                sortMode = ExploreSortMode.RELEVANCE,
                page = 1,
            )

            // ----- Assert -----
            dataSource.queriedBooks.first() shouldBe emptyList()
        }
    }

    @Nested
    inner class SearchByMood {
        private val mood = MoodTag(
            id = 1,
            label = "Cozy",
            slug = "cozy",
            bookCount = 12,
        )

        @Test
        fun `dedupes books that appear across multiple taggable_counts rows`() = runTest {
            // ----- Arrange -----
            val book1 = stubBook(id = 1)
            stubMoodQuery(
                mood = mood,
                page = 1,
                rows = listOf(stubMoodBookRow(book1), stubMoodBookRow(book1)),
            )

            // ----- Act -----
            dataSource.searchByMood(
                mood = mood,
                page = 1,
            )

            // ----- Assert -----
            dataSource.queriedBooks.first().map { it.id } shouldBe listOf(1)
        }

        @Test
        fun `reports hasMore true from the raw row count even when dedupe shrinks the page below the limit`() = runTest {
            // ----- Arrange -----
            val rows = List(MOOD_BOOKS_LIMIT) { index -> stubMoodBookRow(book = stubBook(id = index % 3)) }
            stubMoodQuery(
                mood = mood,
                page = 1,
                rows = rows,
            )

            // ----- Act -----
            dataSource.searchByMood(
                mood = mood,
                page = 1,
            )

            // ----- Assert -----
            dataSource.queriedBooksHasMore.first() shouldBe true
        }

        @Test
        fun `reports hasMore false when the raw row count is below the page limit`() = runTest {
            // ----- Arrange -----
            stubMoodQuery(
                mood = mood,
                page = 1,
                rows = listOf(stubMoodBookRow(book = stubBook(id = 1))),
            )

            // ----- Act -----
            dataSource.searchByMood(
                mood = mood,
                page = 1,
            )

            // ----- Assert -----
            dataSource.queriedBooksHasMore.first() shouldBe false
        }

        @Test
        fun `appends later pages and dedupes a book from an earlier page that reappears`() = runTest {
            // ----- Arrange -----
            stubMoodQuery(
                mood = mood,
                page = 1,
                rows = listOf(stubMoodBookRow(stubBook(id = 1)), stubMoodBookRow(stubBook(id = 2))),
            )
            dataSource.searchByMood(
                mood = mood,
                page = 1,
            )
            stubMoodQuery(
                mood = mood,
                page = 2,
                rows = listOf(stubMoodBookRow(stubBook(id = 2)), stubMoodBookRow(stubBook(id = 3))),
            )

            // ----- Act -----
            dataSource.searchByMood(
                mood = mood,
                page = 2,
            )

            // ----- Assert -----
            dataSource.queriedBooks.first().map { it.id } shouldBe listOf(1, 2, 3)
        }

        @Test
        fun `keeps the previous queriedBooks until a fresh search lands and then replaces them`() = runTest {
            // ----- Arrange -----
            stubMoodQuery(
                mood = mood,
                page = 1,
                rows = listOf(stubMoodBookRow(book = stubBook(id = 1))),
            )
            dataSource.searchByMood(
                mood = mood,
                page = 1,
            )
            stubMoodQuery(
                mood = mood,
                page = 1,
                rows = listOf(stubMoodBookRow(book = stubBook(id = 2))),
            )

            // ----- Act & Assert -----
            dataSource.queriedBooks.test {
                awaitItem().map { it.id } shouldBe listOf(1)

                dataSource.searchByMood(
                    mood = mood,
                    page = 1,
                )

                awaitItem().map { it.id } shouldBe listOf(2)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `resets queriedBooksHasMore to true when a fresh search starts`() = runTest {
            // ----- Arrange -----
            stubMoodQuery(
                mood = mood,
                page = 1,
                rows = listOf(stubMoodBookRow(book = stubBook(id = 1))),
            )
            dataSource.searchByMood(
                mood = mood,
                page = 1,
            )

            // ----- Act & Assert -----
            dataSource.queriedBooksHasMore.test {
                awaitItem() shouldBe false

                dataSource.searchByMood(
                    mood = mood,
                    page = 1,
                )

                awaitItem() shouldBe true
                awaitItem() shouldBe false
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `writes an empty queriedBooks when a page one mood search returns no rows`() = runTest {
            // ----- Arrange -----
            stubMoodQuery(
                mood = mood,
                page = 1,
                rows = listOf(stubMoodBookRow(book = stubBook(id = 1))),
            )
            dataSource.searchByMood(
                mood = mood,
                page = 1,
            )
            stubMoodQuery(
                mood = mood,
                page = 1,
                rows = emptyList(),
            )

            // ----- Act -----
            dataSource.searchByMood(
                mood = mood,
                page = 1,
            )

            // ----- Assert -----
            dataSource.queriedBooks.first() shouldBe emptyList()
        }
    }

    @Nested
    inner class FetchBooksByGenre {
        @Test
        fun `dedupes books across rows and excludes rows whose book is null`() = runTest {
            // ----- Arrange -----
            val book1 = stubBook(id = 1)
            stubGenreQuery(
                genre = "Fantasy",
                limit = 50,
                rows = listOf(stubGenreBookRow(book1), stubGenreBookRow(book1), stubGenreBookRow(book = null)),
            )

            // ----- Act -----
            val result = dataSource.fetchBooksByGenre(
                genre = "Fantasy",
                limit = 50,
            )

            // ----- Assert -----
            result.map { it.id } shouldBe listOf(1)
        }
    }

    @Nested
    inner class ClearSearchResults {
        @Test
        fun `resets queriedBooks to empty and queriedBooksHasMore to true`() = runTest {
            // ----- Arrange -----
            val mood = MoodTag(
                id = 1,
                label = "Cozy",
                slug = "cozy",
                bookCount = 12,
            )
            stubMoodQuery(
                mood = mood,
                page = 1,
                rows = listOf(stubMoodBookRow(book = stubBook(id = 1))),
            )
            dataSource.searchByMood(
                mood = mood,
                page = 1,
            )

            // ----- Act -----
            dataSource.clearSearchResults()

            // ----- Assert -----
            dataSource.queriedBooks.first() shouldBe emptyList()
            dataSource.queriedBooksHasMore.first() shouldBe true
        }
    }
}
