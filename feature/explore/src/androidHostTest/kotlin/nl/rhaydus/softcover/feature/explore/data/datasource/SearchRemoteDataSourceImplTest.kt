package nl.rhaydus.softcover.feature.explore.data.datasource

import app.cash.turbine.test
import com.apollographql.apollo.ApolloClient
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import com.apollographql.apollo.cache.normalized.FetchPolicy
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.GetBooksByIdsQuery
import nl.rhaydus.softcover.GetIdsForQuery
import nl.rhaydus.softcover.core.book.data.mapper.toBook
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.network.helper.safeQuery
import nl.rhaydus.softcover.fragment.BookDetailFragment
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

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
                apolloClient.safeQuery(query = GetIdsForQuery(query = name))
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
                apolloClient.safeQuery(query = GetIdsForQuery(query = name))
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
                apolloClient.safeQuery(query = GetIdsForQuery(query = name))
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
                apolloClient.safeQuery(query = GetIdsForQuery(query = name))
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
            )

            // ----- Assert -----
            dataSource.queriedBooks.test {
                val result = awaitItem()
                result.map { it.id } shouldBe listOf(7)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
