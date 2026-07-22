package nl.rhaydus.softcover.feature.explore.data.datasource

import app.cash.turbine.test
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import com.apollographql.cache.normalized.FetchPolicy
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
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

// Mirrors SearchRemoteDataSourceImpl's private MOOD_BOOKS_LIMIT / SEARCH_RESULTS_PAGE_SIZE
// constants (both 25) - kept here as literals since those constants aren't exposed to callers.
private const val MOOD_BOOKS_LIMIT = 25
private const val SEARCH_RESULTS_PAGE_SIZE = 25

class SearchRemoteDataSourceImplTest {
    private lateinit var apolloClient: ApolloClient
    private lateinit var dataSource: SearchRemoteDataSourceImpl

    @BeforeEach
    fun setUp() {
        apolloClient = mockk()
        dataSource = SearchRemoteDataSourceImpl(apolloClient = apolloClient)

        mockkStatic("nl.rhaydus.softcover.core.network.helper.ApolloExtensionsKt")
        mockkStatic("nl.rhaydus.softcover.core.book.data.mapper.BookMapperKt")
        mockkObject(GetBooksByIdsQuery.Data.Book.Companion)
        mockkObject(GetBooksByMoodTagQuery.Data.Taggable_count.Book.Companion)
        mockkObject(GetBooksByGenreTagQuery.Data.Taggable_count.Book.Companion)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
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
    inner class QueriedBooks {
        @Test
        fun `emits empty list before any search is performed`() = runTest {
            // ----- Act & Assert -----
            dataSource.queriedBooks.test {
                awaitItem() shouldBe emptyList()
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class SearchForName {
        @Test
        fun `updates queriedBooks with books sorted by id order from the search result`() = runTest {
            // ----- Arrange -----
            val name = "dune"
            val userId = 42
            val idsList = listOf(3, 1, 2)
            val book1 = stubBook(id = 1)
            val book2 = stubBook(id = 2)
            val book3 = stubBook(id = 3)
            val bookEntry1 = stubBookEntry(book = book1)
            val bookEntry2 = stubBookEntry(book = book2)
            val bookEntry3 = stubBookEntry(book = book3)

            val idsQueryData = mockk<GetIdsForQuery.Data>()
            val searchData = mockk<GetIdsForQuery.Data.Search>()

            coEvery {
                apolloClient.safeQuery(
                    query = GetIdsForQuery(
                        query = name,
                        sort = Optional.Present(ExploreSortMode.RELEVANCE.toTypesenseSort()),
                    ),
                )
            } returns idsQueryData

            every {
                idsQueryData.search
            } returns searchData

            every {
                searchData.ids
            } returns listOf(3, 1, 2)

            val booksQueryData = mockk<GetBooksByIdsQuery.Data>()

            coEvery {
                apolloClient.safeQuery(
                    query = GetBooksByIdsQuery(ids = idsList),
                    fetchPolicy = any(),
                )
            } returns booksQueryData

            every {
                booksQueryData.books
            } returns listOf(bookEntry1, bookEntry2, bookEntry3)

            // ----- Act -----
            dataSource.searchForName(
                name = name,
                userId = userId,
                sortMode = ExploreSortMode.RELEVANCE,
            )

            // ----- Assert -----
            dataSource.queriedBooks.test {
                val result = awaitItem()
                result.map { it.id } shouldBe listOf(3, 1, 2)
                cancelAndIgnoreRemainingEvents()
            }
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
        fun `updates queriedBooks to empty list when books query returns no books`() = runTest {
            // ----- Arrange -----
            val name = "obscure title"
            val matchingIds = listOf(99)
            val idsQueryData = mockk<GetIdsForQuery.Data>()
            val searchData = mockk<GetIdsForQuery.Data.Search>()

            coEvery {
                apolloClient.safeQuery(
                    query = GetIdsForQuery(
                        query = name,
                        sort = Optional.Present(ExploreSortMode.RELEVANCE.toTypesenseSort()),
                    ),
                )
            } returns idsQueryData

            every {
                idsQueryData.search
            } returns searchData

            every {
                searchData.ids
            } returns listOf(99)

            val booksQueryData = mockk<GetBooksByIdsQuery.Data>()

            coEvery {
                apolloClient.safeQuery(
                    query = GetBooksByIdsQuery(ids = matchingIds),
                    fetchPolicy = any(),
                )
            } returns booksQueryData

            every {
                booksQueryData.books
            } returns emptyList()

            // ----- Act -----
            dataSource.searchForName(
                name = name,
                userId = 1,
                sortMode = ExploreSortMode.RELEVANCE,
            )

            // ----- Assert -----
            dataSource.queriedBooks.test {
                awaitItem() shouldBe emptyList()
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `filters null ids from the search result before fetching books`() = runTest {
            // ----- Arrange -----
            val name = "mixed ids"
            val userId = 1
            val nonNullIds = listOf(10, 20)
            val book10 = stubBook(id = 10)
            val book20 = stubBook(id = 20)
            val bookEntry10 = stubBookEntry(book = book10)
            val bookEntry20 = stubBookEntry(book = book20)

            val idsQueryData = mockk<GetIdsForQuery.Data>()
            val searchData = mockk<GetIdsForQuery.Data.Search>()

            coEvery {
                apolloClient.safeQuery(
                    query = GetIdsForQuery(
                        query = name,
                        sort = Optional.Present(ExploreSortMode.RELEVANCE.toTypesenseSort()),
                    ),
                )
            } returns idsQueryData

            every {
                idsQueryData.search
            } returns searchData

            every {
                searchData.ids
            } returns listOf(10, null, 20)

            val booksQueryData = mockk<GetBooksByIdsQuery.Data>()

            coEvery {
                apolloClient.safeQuery(
                    query = GetBooksByIdsQuery(ids = nonNullIds),
                    fetchPolicy = any(),
                )
            } returns booksQueryData

            every {
                booksQueryData.books
            } returns listOf(bookEntry10, bookEntry20)

            // ----- Act -----
            dataSource.searchForName(
                name = name,
                userId = userId,
                sortMode = ExploreSortMode.RELEVANCE,
            )

            // ----- Assert -----
            dataSource.queriedBooks.test {
                val result = awaitItem()
                result.map { it.id } shouldBe listOf(10, 20)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `excludes book entries whose bookFragment returns null`() = runTest {
            // ----- Arrange -----
            val name = "fragment nulls"
            val userId = 1
            val matchingIds = listOf(7, 8)
            val book7 = stubBook(id = 7)
            val bookEntry7 = stubBookEntry(book = book7)
            val bookEntryNull = mockk<GetBooksByIdsQuery.Data.Book>()

            val idsQueryData = mockk<GetIdsForQuery.Data>()
            val searchData = mockk<GetIdsForQuery.Data.Search>()

            coEvery {
                apolloClient.safeQuery(
                    query = GetIdsForQuery(
                        query = name,
                        sort = Optional.Present(ExploreSortMode.RELEVANCE.toTypesenseSort()),
                    ),
                )
            } returns idsQueryData

            every {
                idsQueryData.search
            } returns searchData

            every {
                searchData.ids
            } returns listOf(7, 8)

            val booksQueryData = mockk<GetBooksByIdsQuery.Data>()

            coEvery {
                apolloClient.safeQuery(
                    query = GetBooksByIdsQuery(ids = matchingIds),
                    fetchPolicy = any(),
                )
            } returns booksQueryData

            every {
                booksQueryData.books
            } returns listOf(bookEntry7, bookEntryNull)

            every {
                with(GetBooksByIdsQuery.Data.Book.Companion) { bookEntryNull.bookDetailFragment() }
            } returns null

            // ----- Act -----
            dataSource.searchForName(
                name = name,
                userId = userId,
                sortMode = ExploreSortMode.RELEVANCE,
            )

            // ----- Assert -----
            dataSource.queriedBooks.test {
                val result = awaitItem()
                result.map { it.id } shouldBe listOf(7)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `sets queriedBooksHasMore true when the raw id count reaches the page size`() = runTest {
            // ----- Arrange -----
            val name = "epic"
            val ids = List(SEARCH_RESULTS_PAGE_SIZE) { it + 1 }
            val books = ids.map { id -> stubBookEntry(book = stubBook(id = id)) }
            stubIdsQuery(
                name = name,
                page = 1,
                ids = ids,
            )
            stubBooksQuery(
                ids = ids,
                books = books,
            )

            // ----- Act -----
            dataSource.searchForName(
                name = name,
                userId = 1,
                sortMode = ExploreSortMode.RELEVANCE,
            )

            // ----- Assert -----
            dataSource.queriedBooksHasMore.test {
                awaitItem() shouldBe true
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `sets queriedBooksHasMore false when the raw id count is below the page size`() = runTest {
            // ----- Arrange -----
            val name = "obscure"
            val ids = listOf(1, 2, 3)
            val books = ids.map { id -> stubBookEntry(book = stubBook(id = id)) }
            stubIdsQuery(
                name = name,
                page = 1,
                ids = ids,
            )
            stubBooksQuery(
                ids = ids,
                books = books,
            )

            // ----- Act -----
            dataSource.searchForName(
                name = name,
                userId = 1,
                sortMode = ExploreSortMode.RELEVANCE,
            )

            // ----- Assert -----
            dataSource.queriedBooksHasMore.test {
                awaitItem() shouldBe false
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `appends to queriedBooks on a later page and dedupes ids shared with the previous page`() = runTest {
            // ----- Arrange -----
            val name = "series"
            val page1Ids = listOf(1, 2)
            val page1Books = page1Ids.map { id -> stubBookEntry(book = stubBook(id = id)) }
            stubIdsQuery(
                name = name,
                page = 1,
                ids = page1Ids,
            )
            stubBooksQuery(
                ids = page1Ids,
                books = page1Books,
            )

            dataSource.searchForName(
                name = name,
                userId = 1,
                sortMode = ExploreSortMode.RELEVANCE,
                page = 1,
            )

            val page2Ids = listOf(2, 3)
            val page2Books = page2Ids.map { id -> stubBookEntry(book = stubBook(id = id)) }
            stubIdsQuery(
                name = name,
                page = 2,
                ids = page2Ids,
            )
            stubBooksQuery(
                ids = page2Ids,
                books = page2Books,
            )

            // ----- Act -----
            dataSource.searchForName(
                name = name,
                userId = 1,
                sortMode = ExploreSortMode.RELEVANCE,
                page = 2,
            )

            // ----- Assert -----
            dataSource.queriedBooks.test {
                awaitItem().map { it.id } shouldBe listOf(1, 2, 3)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `resets queriedBooks to empty before running a fresh (page 1) search`() = runTest {
            // ----- Arrange -----
            val name = "first"
            stubIdsQuery(
                name = name,
                page = 1,
                ids = listOf(1),
            )
            stubBooksQuery(
                ids = listOf(1),
                books = listOf(stubBookEntry(book = stubBook(id = 1))),
            )

            dataSource.searchForName(
                name = name,
                userId = 1,
                sortMode = ExploreSortMode.RELEVANCE,
                page = 1,
            )

            val secondName = "second"
            stubIdsQuery(
                name = secondName,
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
                    name = secondName,
                    userId = 1,
                    sortMode = ExploreSortMode.RELEVANCE,
                    page = 1,
                )

                awaitItem() shouldBe emptyList()
                awaitItem().map { it.id } shouldBe listOf(2)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `resets queriedBooksHasMore to true before running a fresh (page 1) search`() = runTest {
            // ----- Arrange -----
            val name = "first"
            stubIdsQuery(
                name = name,
                page = 1,
                ids = listOf(1),
            )
            stubBooksQuery(
                ids = listOf(1),
                books = listOf(stubBookEntry(book = stubBook(id = 1))),
            )

            dataSource.searchForName(
                name = name,
                userId = 1,
                sortMode = ExploreSortMode.RELEVANCE,
                page = 1,
            )

            val secondName = "second"
            stubIdsQuery(
                name = secondName,
                page = 1,
                ids = listOf(2),
            )
            stubBooksQuery(
                ids = listOf(2),
                books = listOf(stubBookEntry(book = stubBook(id = 2))),
            )

            // ----- Act & Assert -----
            dataSource.queriedBooksHasMore.test {
                awaitItem() shouldBe false

                dataSource.searchForName(
                    name = secondName,
                    userId = 1,
                    sortMode = ExploreSortMode.RELEVANCE,
                    page = 1,
                )

                awaitItem() shouldBe true
                awaitItem() shouldBe false
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `leaves queriedBooks empty when a fresh search returns no matching ids`() = runTest {
            // ----- Arrange -----
            val name = "nothing"
            stubIdsQuery(
                name = name,
                page = 1,
                ids = emptyList(),
            )

            // ----- Act -----
            dataSource.searchForName(
                name = name,
                userId = 1,
                sortMode = ExploreSortMode.RELEVANCE,
                page = 1,
            )

            // ----- Assert -----
            dataSource.queriedBooks.test {
                awaitItem() shouldBe emptyList()
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `sets queriedBooksHasMore false when a fresh search returns no matching ids`() = runTest {
            // ----- Arrange -----
            val name = "nothing"
            stubIdsQuery(
                name = name,
                page = 1,
                ids = emptyList(),
            )

            // ----- Act -----
            dataSource.searchForName(
                name = name,
                userId = 1,
                sortMode = ExploreSortMode.RELEVANCE,
                page = 1,
            )

            // ----- Assert -----
            dataSource.queriedBooksHasMore.test {
                awaitItem() shouldBe false
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `preserves the previous queriedBooks when a later page returns no matching ids`() = runTest {
            // ----- Arrange -----
            val name = "series"
            stubIdsQuery(
                name = name,
                page = 1,
                ids = listOf(1),
            )
            stubBooksQuery(
                ids = listOf(1),
                books = listOf(stubBookEntry(book = stubBook(id = 1))),
            )

            dataSource.searchForName(
                name = name,
                userId = 1,
                sortMode = ExploreSortMode.RELEVANCE,
                page = 1,
            )

            stubIdsQuery(
                name = name,
                page = 2,
                ids = emptyList(),
            )

            // ----- Act -----
            dataSource.searchForName(
                name = name,
                userId = 1,
                sortMode = ExploreSortMode.RELEVANCE,
                page = 2,
            )

            // ----- Assert -----
            dataSource.queriedBooks.test {
                awaitItem().map { it.id } shouldBe listOf(1)
                cancelAndIgnoreRemainingEvents()
            }
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
            dataSource.queriedBooks.test {
                awaitItem().map { it.id } shouldBe listOf(1)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `reports hasMore true from the raw row count even when dedupe shrinks the page below the limit`() = runTest {
            // ----- Arrange -----
            // A full page of raw rows (MOOD_BOOKS_LIMIT) that all resolve to the same 3 distinct books.
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
            dataSource.queriedBooksHasMore.test {
                awaitItem() shouldBe true
                cancelAndIgnoreRemainingEvents()
            }
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
            dataSource.queriedBooksHasMore.test {
                awaitItem() shouldBe false
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `replaces queriedBooks on page 1 and appends on a later page`() = runTest {
            // ----- Arrange -----
            val book1 = stubBook(id = 1)
            stubMoodQuery(
                mood = mood,
                page = 1,
                rows = listOf(stubMoodBookRow(book1)),
            )
            dataSource.searchByMood(
                mood = mood,
                page = 1,
            )

            val book2 = stubBook(id = 2)
            val book3 = stubBook(id = 3)
            stubMoodQuery(
                mood = mood,
                page = 2,
                rows = listOf(stubMoodBookRow(book2), stubMoodBookRow(book3)),
            )

            // ----- Act -----
            dataSource.searchByMood(
                mood = mood,
                page = 2,
            )

            // ----- Assert -----
            dataSource.queriedBooks.test {
                awaitItem().map { it.id } shouldBe listOf(1, 2, 3)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `dedupes the combined list when a book from an earlier page reappears on a later page`() = runTest {
            // ----- Arrange -----
            val book1 = stubBook(id = 1)
            val book2 = stubBook(id = 2)
            stubMoodQuery(
                mood = mood,
                page = 1,
                rows = listOf(stubMoodBookRow(book1), stubMoodBookRow(book2)),
            )
            dataSource.searchByMood(
                mood = mood,
                page = 1,
            )

            val book2Again = stubBook(id = 2)
            val book3 = stubBook(id = 3)
            stubMoodQuery(
                mood = mood,
                page = 2,
                rows = listOf(stubMoodBookRow(book2Again), stubMoodBookRow(book3)),
            )

            // ----- Act -----
            dataSource.searchByMood(
                mood = mood,
                page = 2,
            )

            // ----- Assert -----
            dataSource.queriedBooks.test {
                awaitItem().map { it.id } shouldBe listOf(1, 2, 3)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `resets queriedBooks to empty before running a fresh search`() = runTest {
            // ----- Arrange -----
            val book1 = stubBook(id = 1)
            stubMoodQuery(
                mood = mood,
                page = 1,
                rows = listOf(stubMoodBookRow(book1)),
            )
            dataSource.searchByMood(
                mood = mood,
                page = 1,
            )

            val book2 = stubBook(id = 2)
            stubMoodQuery(
                mood = mood,
                page = 1,
                rows = listOf(stubMoodBookRow(book2)),
            )

            // ----- Act & Assert -----
            dataSource.queriedBooks.test {
                awaitItem().map { it.id } shouldBe listOf(1)

                dataSource.searchByMood(
                    mood = mood,
                    page = 1,
                )

                awaitItem() shouldBe emptyList()
                awaitItem().map { it.id } shouldBe listOf(2)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `resets queriedBooksHasMore to true before running a fresh search`() = runTest {
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
    }

    @Nested
    inner class FetchBooksByGenre {
        @Test
        fun `dedupes books that appear across multiple taggable_counts rows`() = runTest {
            // ----- Arrange -----
            val book1 = stubBook(id = 1)
            stubGenreQuery(
                genre = "Fantasy",
                limit = 50,
                rows = listOf(stubGenreBookRow(book1), stubGenreBookRow(book1)),
            )

            // ----- Act -----
            val result = dataSource.fetchBooksByGenre(
                genre = "Fantasy",
                limit = 50,
            )

            // ----- Assert -----
            result.map { it.id } shouldBe listOf(1)
        }

        @Test
        fun `requests the given overfetch limit`() = runTest {
            // ----- Arrange -----
            stubGenreQuery(
                genre = "Sci-Fi",
                limit = 100,
                rows = emptyList(),
            )

            // ----- Act -----
            dataSource.fetchBooksByGenre(
                genre = "Sci-Fi",
                limit = 100,
            )

            // ----- Assert -----
            coVerify {
                apolloClient.safeQuery(
                    query = GetBooksByGenreTagQuery(
                        genre = "Sci-Fi",
                        limit = 100,
                    ),
                )
            }
        }

        @Test
        fun `excludes rows whose book is null`() = runTest {
            // ----- Arrange -----
            val book1 = stubBook(id = 1)
            stubGenreQuery(
                genre = "Horror",
                limit = 50,
                rows = listOf(stubGenreBookRow(book1), stubGenreBookRow(book = null)),
            )

            // ----- Act -----
            val result = dataSource.fetchBooksByGenre(
                genre = "Horror",
                limit = 50,
            )

            // ----- Assert -----
            result.map { it.id } shouldBe listOf(1)
        }
    }

    @Nested
    inner class ClearSearchResults {
        private val mood = MoodTag(
            id = 1,
            label = "Cozy",
            slug = "cozy",
            bookCount = 12,
        )

        @Test
        fun `resets queriedBooks to empty`() = runTest {
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
            dataSource.queriedBooks.test {
                awaitItem().map { it.id } shouldBe listOf(1)

                dataSource.clearSearchResults()

                awaitItem() shouldBe emptyList()
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `resets queriedBooksHasMore to true`() = runTest {
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

                dataSource.clearSearchResults()

                awaitItem() shouldBe true
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
