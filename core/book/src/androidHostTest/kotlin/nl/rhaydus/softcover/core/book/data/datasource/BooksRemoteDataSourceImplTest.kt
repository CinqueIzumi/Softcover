package nl.rhaydus.softcover.core.book.data.datasource

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.api.Query
import com.apollographql.cache.normalized.FetchPolicy
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.CapturingSlot
import io.mockk.coEvery
import io.mockk.clearAllMocks
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.CreateBookMutation
import nl.rhaydus.softcover.GetBookByIdQuery
import nl.rhaydus.softcover.GetBookIdByEditionIdQuery
import nl.rhaydus.softcover.GetBooksByIdsQuery
import nl.rhaydus.softcover.GetEditionsByBookIdQuery
import nl.rhaydus.softcover.GetEditionsByIdsQuery
import nl.rhaydus.softcover.GetEditionsByIsbnsQuery
import nl.rhaydus.softcover.GetTrendingBookIdsQuery
import nl.rhaydus.softcover.GetUserBooksQuery
import nl.rhaydus.softcover.InsertUserBookWithStatusMutation
import nl.rhaydus.softcover.MarkBookAsReadMutation
import nl.rhaydus.softcover.MarkBookAsWantToReadMutation
import nl.rhaydus.softcover.RemoveUserBookMutation
import nl.rhaydus.softcover.UpdateBookEditionMutation
import nl.rhaydus.softcover.UpdateReadingProgressMutation
import nl.rhaydus.softcover.UpdateReadingProgressMutation.Data.Update_user_book_read.User_book_read.Companion.userBookReadFragment
import nl.rhaydus.softcover.UpdateUserBookRatingMutation
import nl.rhaydus.softcover.UpdateUserBookReviewMutation
import nl.rhaydus.softcover.UpdateUserBookStatusMutation
import nl.rhaydus.softcover.core.book.baseBook
import nl.rhaydus.softcover.core.book.data.mapper.toBook
import nl.rhaydus.softcover.core.book.data.mapper.toBookEdition
import nl.rhaydus.softcover.core.book.domain.model.CreatedBook
import nl.rhaydus.softcover.core.book.domain.model.IsbnEditionMatch
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.ReadingJournal
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.ReviewParagraph
import nl.rhaydus.softcover.core.domain.model.ReviewRun
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.network.helper.safeMutation
import nl.rhaydus.softcover.core.network.helper.safeQuery
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.time.Instant

class BooksRemoteDataSourceImplTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun mockSharedStatics() {
            mockkStatic("nl.rhaydus.softcover.core.network.helper.ApolloExtensionsKt")
            mockkStatic("nl.rhaydus.softcover.core.book.data.mapper.BookMapperKt")
            mockkObject(UpdateReadingProgressMutation.Data.Update_user_book_read.User_book_read.Companion)
            mockkObject(GetBooksByIdsQuery.Data.Book.Companion)
            mockkObject(GetEditionsByBookIdQuery.Data.Edition.Companion)
            mockkObject(GetEditionsByIdsQuery.Data.Edition.Companion)
        }

        @JvmStatic
        @AfterAll
        fun unmockSharedStatics() {
            unmockkAll()
        }
    }

    private lateinit var apolloClient: ApolloClient
    private lateinit var appDispatchers: AppDispatchers
    private lateinit var dataSource: BooksRemoteDataSourceImpl

    @BeforeEach
    fun setUp() {
        apolloClient = mockk()

        val dispatcher = UnconfinedTestDispatcher()
        appDispatchers = AppDispatchers(
            main = dispatcher,
            io = dispatcher,
            default = dispatcher,
        )

        dataSource = BooksRemoteDataSourceImpl(
            apolloClient = apolloClient,
            appDispatchers = appDispatchers,
        )

        clearAllMocks(objectMocks = false)
    }

    private fun stubBook(
        id: Int = 1,
        canonicalId: Int? = null,
        userBook: UserBook? = null,
        userBookRead: UserBookRead? = null,
    ): Book = mockk {
        every {
            this@mockk.id
        } returns id

        every {
            this@mockk.canonicalId
        } returns canonicalId

        every {
            this@mockk.userBook
        } returns userBook

        every {
            this@mockk.userBookRead
        } returns userBookRead
    }

    private fun stubUserBook(
        id: Int = 1,
        journals: List<ReadingJournal> = emptyList(),
    ): UserBook = mockk(relaxed = true) {
        every {
            this@mockk.id
        } returns id

        every {
            this@mockk.journals
        } returns journals
    }

    private fun stubUserBookRead(
        id: Int = 1,
        startedAt: String? = null,
        finishedAt: String? = null,
        currentSeconds: Int? = null,
    ): UserBookRead = mockk {
        every {
            this@mockk.id
        } returns id

        every {
            this@mockk.startedAt
        } returns startedAt

        every {
            this@mockk.finishedAt
        } returns finishedAt

        every {
            this@mockk.currentSeconds
        } returns currentSeconds
    }

    private fun stubBookEdition(id: Int = 10): BookEdition = mockk {
        every {
            this@mockk.id
        } returns id
    }

    @Nested
    inner class FetchBookById {
        @Test
        fun `returns mapped book when safeQuery succeeds and canonicalId is null`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val expectedBook = stubBook(
                id = bookId,
                canonicalId = null,
            )
            val queryData = mockk<GetBookByIdQuery.Data>()
            val dataBook = mockk<GetBookByIdQuery.Data.Book>()

            coEvery {
                apolloClient.safeQuery(
                    query = any<GetBookByIdQuery>(),
                    fetchPolicy = any(),
                )
            } returns queryData

            every {
                queryData.books
            } returns listOf(dataBook)

            every {
                dataBook.toBook()
            } returns expectedBook

            // ----- Act -----
            val result = dataSource.fetchBookById(id = bookId)

            // ----- Assert -----
            result shouldBe expectedBook
        }

        @Test
        fun `refetches canonical book when canonicalId differs from id and clears canonicalId`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val canonicalId = 99
            val firstBook = stubBook(
                id = bookId,
                canonicalId = canonicalId,
            )
            val canonicalBook = stubBook(
                id = canonicalId,
                canonicalId = null,
            )
            val canonicalBookCleared = stubBook(
                id = canonicalId,
                canonicalId = null,
            )

            val firstQueryData = mockk<GetBookByIdQuery.Data>()
            val canonicalQueryData = mockk<GetBookByIdQuery.Data>()
            val firstDataBook = mockk<GetBookByIdQuery.Data.Book>()
            val canonicalDataBook = mockk<GetBookByIdQuery.Data.Book>()

            coEvery {
                apolloClient.safeQuery(
                    query = GetBookByIdQuery(id = bookId),
                    fetchPolicy = any(),
                )
            } returns firstQueryData

            coEvery {
                apolloClient.safeQuery(
                    query = GetBookByIdQuery(id = canonicalId),
                    fetchPolicy = any(),
                )
            } returns canonicalQueryData

            every {
                firstQueryData.books
            } returns listOf(firstDataBook)

            every {
                canonicalQueryData.books
            } returns listOf(canonicalDataBook)

            every {
                firstDataBook.toBook()
            } returns firstBook

            every {
                canonicalDataBook.toBook()
            } returns canonicalBook

            every {
                canonicalBook.copy(canonicalId = null)
            } returns canonicalBookCleared

            // ----- Act -----
            val result = dataSource.fetchBookById(id = bookId)

            // ----- Assert -----
            result shouldBe canonicalBookCleared
            coVerify {
                apolloClient.safeQuery(
                    query = GetBookByIdQuery(id = canonicalId),
                    fetchPolicy = any(),
                )
            }
        }

        @Test
        fun `does not refetch when canonicalId equals book id`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val book = stubBook(
                id = bookId,
                canonicalId = bookId,
            )
            val queryData = mockk<GetBookByIdQuery.Data>()
            val dataBook = mockk<GetBookByIdQuery.Data.Book>()

            coEvery {
                apolloClient.safeQuery(
                    query = any<GetBookByIdQuery>(),
                    fetchPolicy = any(),
                )
            } returns queryData

            every {
                queryData.books
            } returns listOf(dataBook)

            every {
                dataBook.toBook()
            } returns book

            // ----- Act -----
            val result = dataSource.fetchBookById(id = bookId)

            // ----- Assert -----
            result shouldBe book
            coVerify(exactly = 1) {
                apolloClient.safeQuery(
                    query = any<GetBookByIdQuery>(),
                    fetchPolicy = any(),
                )
            }
        }

        @Test
        fun `throws when toBook maps the fragment to null`() = runTest {
            // ----- Arrange -----
            val queryData = mockk<GetBookByIdQuery.Data>()
            val dataBook = mockk<GetBookByIdQuery.Data.Book>()

            coEvery {
                apolloClient.safeQuery(
                    query = any<GetBookByIdQuery>(),
                    fetchPolicy = any(),
                )
            } returns queryData

            every {
                queryData.books
            } returns listOf(dataBook)

            every {
                dataBook.toBook()
            } returns null

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.fetchBookById(id = 1)
            }
        }

        @Test
        fun `throws BookNotFoundException when books list is empty`() = runTest {
            // ----- Arrange -----
            val bookId = 1
            val queryData = mockk<GetBookByIdQuery.Data>()

            coEvery {
                apolloClient.safeQuery(
                    query = any<GetBookByIdQuery>(),
                    fetchPolicy = any(),
                )
            } returns queryData

            every {
                queryData.books
            } returns emptyList()

            // ----- Act & Assert -----
            val thrown = shouldThrow<BookNotFoundException> {
                dataSource.fetchBookById(id = bookId)
            }

            thrown.bookId shouldBe bookId
        }
    }

    @Nested
    inner class FetchBookIdForEdition {
        @Test
        fun `returns book_id from the first edition row`() = runTest {
            // ----- Arrange -----
            val editionId = 42
            val expectedBookId = 7

            val queryData = mockk<GetBookIdByEditionIdQuery.Data>()
            val editionRow = mockk<GetBookIdByEditionIdQuery.Data.Edition>()

            coEvery {
                apolloClient.safeQuery(
                    query = any<GetBookIdByEditionIdQuery>(),
                    fetchPolicy = FetchPolicy.NetworkFirst,
                )
            } returns queryData

            every {
                queryData.editions
            } returns listOf(editionRow)

            every {
                editionRow.book_id
            } returns expectedBookId

            // ----- Act -----
            val result = dataSource.fetchBookIdForEdition(editionId = editionId)

            // ----- Assert -----
            result shouldBe expectedBookId

            coVerify(exactly = 1) {
                apolloClient.safeQuery(
                    query = any<GetBookIdByEditionIdQuery>(),
                    fetchPolicy = FetchPolicy.NetworkFirst,
                )
            }
        }

        @Test
        fun `returns null when the editions list is empty`() = runTest {
            // ----- Arrange -----
            val editionId = 42
            val queryData = mockk<GetBookIdByEditionIdQuery.Data>()

            coEvery {
                apolloClient.safeQuery(
                    query = any<GetBookIdByEditionIdQuery>(),
                    fetchPolicy = FetchPolicy.NetworkFirst,
                )
            } returns queryData

            every {
                queryData.editions
            } returns emptyList()

            // ----- Act -----
            val result = dataSource.fetchBookIdForEdition(editionId = editionId)

            // ----- Assert -----
            result shouldBe null
        }
    }

    @Nested
    inner class FetchBooksByIds {
        @Test
        fun `returns empty list immediately when ids input is empty`() = runTest {
            // ----- Arrange -----
            // (no Apollo call expected — short-circuit path)

            // ----- Act -----
            val result = dataSource.fetchBooksByIds(ids = emptyList())

            // ----- Assert -----
            result shouldBe emptyList()
            coVerify(exactly = 0) {
                apolloClient.safeQuery(
                    query = any<GetBooksByIdsQuery>(),
                    fetchPolicy = any(),
                )
            }
        }

        @Test
        fun `returns mapped books for each entry returned by the query`() = runTest {
            // ----- Arrange -----
            val ids = listOf(1, 2)
            val expectedBook1 = stubBook(id = 1)
            val expectedBook2 = stubBook(id = 2)
            val queryData = mockk<GetBooksByIdsQuery.Data>()
            val bookEntry1 = mockk<GetBooksByIdsQuery.Data.Book>()
            val bookEntry2 = mockk<GetBooksByIdsQuery.Data.Book>()

            coEvery {
                apolloClient.safeQuery(
                    query = any<GetBooksByIdsQuery>(),
                    fetchPolicy = any(),
                )
            } returns queryData

            every {
                queryData.books
            } returns listOf(bookEntry1, bookEntry2)

            every {
                with(GetBooksByIdsQuery.Data.Book.Companion) { bookEntry1.bookDetailFragment() }
            } returns mockk {
                every {
                    toBook()
                } returns expectedBook1
            }

            every {
                with(GetBooksByIdsQuery.Data.Book.Companion) { bookEntry2.bookDetailFragment() }
            } returns mockk {
                every {
                    toBook()
                } returns expectedBook2
            }

            // ----- Act -----
            val result = dataSource.fetchBooksByIds(ids = ids)

            // ----- Assert -----
            result shouldBe listOf(expectedBook1, expectedBook2)
        }

        @Test
        fun `filters out entries whose bookDetailFragment returns null`() = runTest {
            // ----- Arrange -----
            val ids = listOf(1, 2)
            val queryData = mockk<GetBooksByIdsQuery.Data>()
            val bookEntry = mockk<GetBooksByIdsQuery.Data.Book>()

            coEvery {
                apolloClient.safeQuery(
                    query = any<GetBooksByIdsQuery>(),
                    fetchPolicy = any(),
                )
            } returns queryData

            every {
                queryData.books
            } returns listOf(bookEntry)

            every {
                with(GetBooksByIdsQuery.Data.Book.Companion) { bookEntry.bookDetailFragment() }
            } returns null

            // ----- Act -----
            val result = dataSource.fetchBooksByIds(ids = ids)

            // ----- Assert -----
            result shouldBe emptyList()
        }

        @Test
        fun `splits more than 200 ids into one request per 200 ids`() = runTest {
            // ----- Arrange -----
            val ids = (1..201).toList()
            val queryData = mockk<GetBooksByIdsQuery.Data>()

            coEvery {
                apolloClient.safeQuery(
                    query = any<GetBooksByIdsQuery>(),
                    fetchPolicy = any(),
                )
            } returns queryData

            every {
                queryData.books
            } returns emptyList()

            // ----- Act -----
            dataSource.fetchBooksByIds(ids = ids)

            // ----- Assert -----
            coVerify(exactly = 1) {
                apolloClient.safeQuery(
                    query = match<GetBooksByIdsQuery> { it.ids == ids.take(200) },
                    fetchPolicy = any(),
                )
            }
            coVerify(exactly = 1) {
                apolloClient.safeQuery(
                    query = match<GetBooksByIdsQuery> { it.ids == listOf(201) },
                    fetchPolicy = any(),
                )
            }
        }

        @Test
        fun `concatenates the books of every batch in request order`() = runTest {
            // ----- Arrange -----
            val ids = (1..201).toList()
            val expectedBook1 = stubBook(id = 1)
            val expectedBook201 = stubBook(id = 201)
            val firstBatchData = mockk<GetBooksByIdsQuery.Data>()
            val secondBatchData = mockk<GetBooksByIdsQuery.Data>()
            val firstEntry = mockk<GetBooksByIdsQuery.Data.Book>()
            val secondEntry = mockk<GetBooksByIdsQuery.Data.Book>()

            coEvery {
                apolloClient.safeQuery(
                    query = match<GetBooksByIdsQuery> { it.ids.size == 200 },
                    fetchPolicy = any(),
                )
            } returns firstBatchData

            coEvery {
                apolloClient.safeQuery(
                    query = match<GetBooksByIdsQuery> { it.ids.size == 1 },
                    fetchPolicy = any(),
                )
            } returns secondBatchData

            every {
                firstBatchData.books
            } returns listOf(firstEntry)

            every {
                secondBatchData.books
            } returns listOf(secondEntry)

            every {
                with(GetBooksByIdsQuery.Data.Book.Companion) { firstEntry.bookDetailFragment() }
            } returns mockk {
                every {
                    toBook()
                } returns expectedBook1
            }

            every {
                with(GetBooksByIdsQuery.Data.Book.Companion) { secondEntry.bookDetailFragment() }
            } returns mockk {
                every {
                    toBook()
                } returns expectedBook201
            }

            // ----- Act -----
            val result = dataSource.fetchBooksByIds(ids = ids)

            // ----- Assert -----
            result shouldBe listOf(expectedBook1, expectedBook201)
        }
    }

    @Nested
    inner class GetEditionsByBookId {
        @Test
        fun `returns mapped editions for each edition entry returned by the query`() = runTest {
            // ----- Arrange -----
            val bookId = 5
            val expectedEdition = stubBookEdition(id = 10)
            val queryData = mockk<GetEditionsByBookIdQuery.Data>()
            val editionEntry = mockk<GetEditionsByBookIdQuery.Data.Edition>()

            coEvery {
                apolloClient.safeQuery(
                    query = any<GetEditionsByBookIdQuery>(),
                    fetchPolicy = any(),
                )
            } returns queryData

            every {
                queryData.editions
            } returns listOf(editionEntry)

            every {
                with(GetEditionsByBookIdQuery.Data.Edition.Companion) {
                    editionEntry.editionDetailFragment()
                }
            } returns mockk {
                every {
                    toBookEdition()
                } returns expectedEdition
            }

            // ----- Act -----
            val result = dataSource.getEditionsByBookId(bookId = bookId)

            // ----- Assert -----
            result shouldBe listOf(expectedEdition)
        }

        @Test
        fun `filters out editions whose editionDetailFragment companion extension returns null`() = runTest {
            // ----- Arrange -----
            val bookId = 5
            val queryData = mockk<GetEditionsByBookIdQuery.Data>()
            val editionEntry = mockk<GetEditionsByBookIdQuery.Data.Edition>()

            coEvery {
                apolloClient.safeQuery(
                    query = any<GetEditionsByBookIdQuery>(),
                    fetchPolicy = any(),
                )
            } returns queryData

            every {
                queryData.editions
            } returns listOf(editionEntry)

            every {
                with(GetEditionsByBookIdQuery.Data.Edition.Companion) {
                    editionEntry.editionDetailFragment()
                }
            } returns null

            // ----- Act -----
            val result = dataSource.getEditionsByBookId(bookId = bookId)

            // ----- Assert -----
            result shouldBe emptyList()
        }
    }

    @Nested
    inner class FetchEditionsByIds {
        @Test
        fun `returns empty list immediately when ids input is empty`() = runTest {
            // ----- Arrange -----
            // (no Apollo call expected — short-circuit path)

            // ----- Act -----
            val result = dataSource.fetchEditionsByIds(ids = emptyList())

            // ----- Assert -----
            result shouldBe emptyList()
            coVerify(exactly = 0) {
                apolloClient.safeQuery(
                    query = any<GetEditionsByIdsQuery>(),
                    fetchPolicy = any(),
                )
            }
        }

        @Test
        fun `returns mapped editions for each edition entry returned by the query`() = runTest {
            // ----- Arrange -----
            val ids = listOf(10, 20)
            val expectedEdition1 = stubBookEdition(id = 10)
            val expectedEdition2 = stubBookEdition(id = 20)
            val queryData = mockk<GetEditionsByIdsQuery.Data>()
            val editionEntry1 = mockk<GetEditionsByIdsQuery.Data.Edition>()
            val editionEntry2 = mockk<GetEditionsByIdsQuery.Data.Edition>()

            coEvery {
                apolloClient.safeQuery(
                    query = any<GetEditionsByIdsQuery>(),
                    fetchPolicy = any(),
                )
            } returns queryData

            every {
                queryData.editions
            } returns listOf(editionEntry1, editionEntry2)

            every {
                with(GetEditionsByIdsQuery.Data.Edition.Companion) {
                    editionEntry1.editionDetailFragment()
                }
            } returns mockk {
                every {
                    toBookEdition()
                } returns expectedEdition1
            }

            every {
                with(GetEditionsByIdsQuery.Data.Edition.Companion) {
                    editionEntry2.editionDetailFragment()
                }
            } returns mockk {
                every {
                    toBookEdition()
                } returns expectedEdition2
            }

            // ----- Act -----
            val result = dataSource.fetchEditionsByIds(ids = ids)

            // ----- Assert -----
            result shouldBe listOf(expectedEdition1, expectedEdition2)
        }

        @Test
        fun `filters out entries whose editionDetailFragment companion extension returns null`() = runTest {
            // ----- Arrange -----
            val ids = listOf(10)
            val queryData = mockk<GetEditionsByIdsQuery.Data>()
            val editionEntry = mockk<GetEditionsByIdsQuery.Data.Edition>()

            coEvery {
                apolloClient.safeQuery(
                    query = any<GetEditionsByIdsQuery>(),
                    fetchPolicy = any(),
                )
            } returns queryData

            every {
                queryData.editions
            } returns listOf(editionEntry)

            every {
                with(GetEditionsByIdsQuery.Data.Edition.Companion) {
                    editionEntry.editionDetailFragment()
                }
            } returns null

            // ----- Act -----
            val result = dataSource.fetchEditionsByIds(ids = ids)

            // ----- Assert -----
            result shouldBe emptyList()
        }

        @Test
        fun `splits more than 200 ids into one request per 200 ids`() = runTest {
            // ----- Arrange -----
            val ids = (1..201).toList()
            val queryData = mockk<GetEditionsByIdsQuery.Data>()

            coEvery {
                apolloClient.safeQuery(
                    query = any<GetEditionsByIdsQuery>(),
                    fetchPolicy = any(),
                )
            } returns queryData

            every {
                queryData.editions
            } returns emptyList()

            // ----- Act -----
            dataSource.fetchEditionsByIds(ids = ids)

            // ----- Assert -----
            coVerify(exactly = 1) {
                apolloClient.safeQuery(
                    query = match<GetEditionsByIdsQuery> { it.ids == ids.take(200) },
                    fetchPolicy = any(),
                )
            }
            coVerify(exactly = 1) {
                apolloClient.safeQuery(
                    query = match<GetEditionsByIdsQuery> { it.ids == listOf(201) },
                    fetchPolicy = any(),
                )
            }
        }
    }

    @Nested
    inner class FetchTrendingBooks {
        private fun trendingIdsData(ids: List<Int?>?): GetTrendingBookIdsQuery.Data {
            val trending = ids?.let { idsValue ->
                mockk<GetTrendingBookIdsQuery.Data.Books_trending> {
                    every {
                        this@mockk.ids
                    } returns idsValue
                }
            }

            return mockk {
                every {
                    books_trending
                } returns trending
            }
        }

        private fun stubTrendingIdsQuery(data: GetTrendingBookIdsQuery.Data) {
            coEvery {
                apolloClient.safeQuery(
                    query = match<Query<GetTrendingBookIdsQuery.Data>> { it is GetTrendingBookIdsQuery },
                    fetchPolicy = any(),
                )
            } returns data
        }

        private fun stubBooksByIdsEntry(book: Book): GetBooksByIdsQuery.Data.Book {
            val entry = mockk<GetBooksByIdsQuery.Data.Book>()

            every {
                with(GetBooksByIdsQuery.Data.Book.Companion) { entry.bookDetailFragment() }
            } returns mockk {
                every {
                    toBook()
                } returns book
            }

            return entry
        }

        private fun stubBooksByIdsQuery(entries: List<GetBooksByIdsQuery.Data.Book>) {
            val booksData = mockk<GetBooksByIdsQuery.Data> {
                every {
                    books
                } returns entries
            }

            coEvery {
                apolloClient.safeQuery(
                    query = match<Query<GetBooksByIdsQuery.Data>> { it is GetBooksByIdsQuery },
                    fetchPolicy = any(),
                )
            } returns booksData
        }

        @Test
        fun `returns an empty list without fetching books when there is no trending set`() = runTest {
            // ----- Arrange -----
            stubTrendingIdsQuery(trendingIdsData(ids = null))

            // ----- Act -----
            val result = dataSource.fetchTrendingBooks()

            // ----- Assert -----
            result shouldBe emptyList()
            coVerify(exactly = 0) {
                apolloClient.safeQuery(
                    query = match<Query<GetBooksByIdsQuery.Data>> { it is GetBooksByIdsQuery },
                    fetchPolicy = any(),
                )
            }
        }

        @Test
        fun `returns an empty list without fetching books when the trending ids are empty`() = runTest {
            // ----- Arrange -----
            stubTrendingIdsQuery(trendingIdsData(ids = emptyList()))

            // ----- Act -----
            val result = dataSource.fetchTrendingBooks()

            // ----- Assert -----
            result shouldBe emptyList()
            coVerify(exactly = 0) {
                apolloClient.safeQuery(
                    query = match<Query<GetBooksByIdsQuery.Data>> { it is GetBooksByIdsQuery },
                    fetchPolicy = any(),
                )
            }
        }

        @Test
        fun `requests the non-null trending ids and restores the trending rank of the returned books`() = runTest {
            // ----- Arrange -----
            val first = stubBook(id = 30)
            val second = stubBook(id = 10)
            val third = stubBook(id = 20)

            stubTrendingIdsQuery(trendingIdsData(ids = listOf(30, null, 10, 20)))
            stubBooksByIdsQuery(
                listOf(
                    stubBooksByIdsEntry(third),
                    stubBooksByIdsEntry(first),
                    stubBooksByIdsEntry(second),
                ),
            )

            // ----- Act -----
            val result = dataSource.fetchTrendingBooks()

            // ----- Assert -----
            result shouldBe listOf(first, second, third)
            coVerify(exactly = 1) {
                apolloClient.safeQuery(
                    query = match<Query<GetBooksByIdsQuery.Data>> {
                        it is GetBooksByIdsQuery && it.ids == listOf(30, 10, 20)
                    },
                    fetchPolicy = any(),
                )
            }
        }

        @Test
        fun `sorts a returned book whose id is not in the trending set last`() = runTest {
            // ----- Arrange -----
            val redirected = stubBook(id = 99)
            val ranked = stubBook(id = 10)

            stubTrendingIdsQuery(trendingIdsData(ids = listOf(10, 20)))
            stubBooksByIdsQuery(
                listOf(
                    stubBooksByIdsEntry(redirected),
                    stubBooksByIdsEntry(ranked),
                ),
            )

            // ----- Act -----
            val result = dataSource.fetchTrendingBooks()

            // ----- Assert -----
            result shouldBe listOf(ranked, redirected)
        }

        @Test
        fun `drops entries whose bookDetailFragment is null`() = runTest {
            // ----- Arrange -----
            val ranked = stubBook(id = 10)
            val unmappable = mockk<GetBooksByIdsQuery.Data.Book>()

            every {
                with(GetBooksByIdsQuery.Data.Book.Companion) { unmappable.bookDetailFragment() }
            } returns null

            stubTrendingIdsQuery(trendingIdsData(ids = listOf(10, 20)))
            stubBooksByIdsQuery(
                listOf(
                    stubBooksByIdsEntry(ranked),
                    unmappable,
                ),
            )

            // ----- Act -----
            val result = dataSource.fetchTrendingBooks()

            // ----- Assert -----
            result shouldBe listOf(ranked)
        }
    }

    @Nested
    inner class MarkBookAsWantToRead {
        @Test
        fun `throws when mutation returns null insert_user_book wrapper`() = runTest {
            // ----- Arrange -----
            val mutationData = mockk<MarkBookAsWantToReadMutation.Data>()

            coEvery {
                apolloClient.safeMutation(mutation = any<MarkBookAsWantToReadMutation>())
            } returns mutationData

            every {
                mutationData.insert_user_book
            } returns null

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.markBookAsWantToRead(bookId = 5)
            }
        }

        @Test
        fun `returns mapped book when mutation succeeds`() = runTest {
            // ----- Arrange -----
            val expectedBook = stubBook()
            val mutationData = mockk<MarkBookAsWantToReadMutation.Data>()
            val insertUserBook = mockk<MarkBookAsWantToReadMutation.Data.Insert_user_book>()
            val userBookEntry = mockk<MarkBookAsWantToReadMutation.Data.Insert_user_book.User_book>()

            coEvery {
                apolloClient.safeMutation(mutation = any<MarkBookAsWantToReadMutation>())
            } returns mutationData

            every {
                mutationData.insert_user_book
            } returns insertUserBook

            every {
                insertUserBook.user_book
            } returns userBookEntry

            every {
                userBookEntry.toBook()
            } returns expectedBook

            // ----- Act -----
            val result = dataSource.markBookAsWantToRead(bookId = 5)

            // ----- Assert -----
            result shouldBe expectedBook
        }

        @Test
        fun `sets edition_id to Optional presentIfNotNull when editionId is non-null`() = runTest {
            // ----- Arrange -----
            val bookId = 5
            val editionId = 42
            val expectedBook = stubBook()
            val mutationData = mockk<MarkBookAsWantToReadMutation.Data>()
            val insertUserBook = mockk<MarkBookAsWantToReadMutation.Data.Insert_user_book>()
            val userBookEntry = mockk<MarkBookAsWantToReadMutation.Data.Insert_user_book.User_book>()

            coEvery {
                apolloClient.safeMutation(mutation = any<MarkBookAsWantToReadMutation>())
            } returns mutationData

            every {
                mutationData.insert_user_book
            } returns insertUserBook

            every {
                insertUserBook.user_book
            } returns userBookEntry

            every {
                userBookEntry.toBook()
            } returns expectedBook

            // ----- Act -----
            dataSource.markBookAsWantToRead(
                bookId = bookId,
                editionId = editionId,
            )

            // ----- Assert -----
            coVerify {
                apolloClient.safeMutation(
                    mutation = match<MarkBookAsWantToReadMutation> {
                        it.`object`.edition_id == Optional.presentIfNotNull(editionId)
                    },
                )
            }
        }

        @Test
        fun `sets edition_id to Optional Absent when editionId is null`() = runTest {
            // ----- Arrange -----
            val bookId = 5
            val expectedBook = stubBook()
            val mutationData = mockk<MarkBookAsWantToReadMutation.Data>()
            val insertUserBook = mockk<MarkBookAsWantToReadMutation.Data.Insert_user_book>()
            val userBookEntry = mockk<MarkBookAsWantToReadMutation.Data.Insert_user_book.User_book>()

            coEvery {
                apolloClient.safeMutation(mutation = any<MarkBookAsWantToReadMutation>())
            } returns mutationData

            every {
                mutationData.insert_user_book
            } returns insertUserBook

            every {
                insertUserBook.user_book
            } returns userBookEntry

            every {
                userBookEntry.toBook()
            } returns expectedBook

            // ----- Act -----
            dataSource.markBookAsWantToRead(
                bookId = bookId,
                editionId = null,
            )

            // ----- Assert -----
            coVerify {
                apolloClient.safeMutation(
                    mutation = match<MarkBookAsWantToReadMutation> {
                        it.`object`.edition_id == Optional.Absent
                    },
                )
            }
        }
    }

    private fun stubUpdateUserBookStatus(result: Book = stubBook()) {
        val mutationData = mockk<UpdateUserBookStatusMutation.Data>()
        val updateUserBook = mockk<UpdateUserBookStatusMutation.Data.Update_user_book>()
        val userBookEntry = mockk<UpdateUserBookStatusMutation.Data.Update_user_book.User_book>()

        coEvery {
            apolloClient.safeMutation(mutation = any<UpdateUserBookStatusMutation>())
        } returns mutationData

        every {
            mutationData.update_user_book
        } returns updateUserBook

        every {
            updateUserBook.user_book
        } returns userBookEntry

        every {
            userBookEntry.toBook()
        } returns result
    }

    @Nested
    inner class MarkBookAsReading {
        @Test
        fun `returns mapped book when mutation succeeds`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook(id = 7)
            val book = stubBook(userBook = userBook)
            val expectedBook = stubBook()
            val mutationData = mockk<UpdateUserBookStatusMutation.Data>()
            val updateUserBook = mockk<UpdateUserBookStatusMutation.Data.Update_user_book>()
            val userBookEntry = mockk<UpdateUserBookStatusMutation.Data.Update_user_book.User_book>()

            every {
                book.currentEdition
            } returns stubBookEdition()

            coEvery {
                apolloClient.safeMutation(mutation = any<UpdateUserBookStatusMutation>())
            } returns mutationData

            every {
                mutationData.update_user_book
            } returns updateUserBook

            every {
                updateUserBook.user_book
            } returns userBookEntry

            every {
                userBookEntry.toBook()
            } returns expectedBook

            // ----- Act -----
            val result = dataSource.markBookAsReading(book = book)

            // ----- Assert -----
            result shouldBe expectedBook
        }

        @Test
        fun `creates a new user_book via insert_user_book when book has no userBook`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBook = null)
            val expectedBook = stubBook()
            val mutationData = mockk<InsertUserBookWithStatusMutation.Data>()
            val insertUserBook = mockk<InsertUserBookWithStatusMutation.Data.Insert_user_book>()
            val userBookEntry = mockk<InsertUserBookWithStatusMutation.Data.Insert_user_book.User_book>()

            coEvery {
                apolloClient.safeMutation(mutation = any<InsertUserBookWithStatusMutation>())
            } returns mutationData

            every {
                mutationData.insert_user_book
            } returns insertUserBook

            every {
                insertUserBook.user_book
            } returns userBookEntry

            every {
                userBookEntry.toBook()
            } returns expectedBook

            // ----- Act -----
            val result = dataSource.markBookAsReading(book = book)

            // ----- Assert -----
            result shouldBe expectedBook
        }

        @Test
        fun `throws when insert mutation returns null insert_user_book wrapper`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBook = null)
            val mutationData = mockk<InsertUserBookWithStatusMutation.Data>()

            coEvery {
                apolloClient.safeMutation(mutation = any<InsertUserBookWithStatusMutation>())
            } returns mutationData

            every {
                mutationData.insert_user_book
            } returns null

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.markBookAsReading(book = book)
            }
        }

        @Test
        fun `inserts with CURRENTLY_READING status and a user_date when book has no userBook`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBook = null)
            val expectedBook = stubBook()
            val mutationData = mockk<InsertUserBookWithStatusMutation.Data>()
            val insertUserBook = mockk<InsertUserBookWithStatusMutation.Data.Insert_user_book>()
            val userBookEntry = mockk<InsertUserBookWithStatusMutation.Data.Insert_user_book.User_book>()

            coEvery {
                apolloClient.safeMutation(mutation = any<InsertUserBookWithStatusMutation>())
            } returns mutationData

            every {
                mutationData.insert_user_book
            } returns insertUserBook

            every {
                insertUserBook.user_book
            } returns userBookEntry

            every {
                userBookEntry.toBook()
            } returns expectedBook

            // ----- Act -----
            dataSource.markBookAsReading(book = book)

            // ----- Assert -----
            coVerify {
                apolloClient.safeMutation(
                    mutation = match<InsertUserBookWithStatusMutation> {
                        it.userBookCreateInput.status_id == Optional.present(UserBookStatus.CURRENTLY_READING.code) &&
                            it.userBookCreateInput.user_date != Optional.Absent
                    },
                )
            }
        }

        @Test
        fun `sets edition_id to Optional presentIfNotNull when editionId is non-null and book has no userBook`() =
            runTest {
                // ----- Arrange -----
                val book = stubBook(userBook = null)
                val editionId = 42
                val expectedBook = stubBook()
                val mutationData = mockk<InsertUserBookWithStatusMutation.Data>()
                val insertUserBook = mockk<InsertUserBookWithStatusMutation.Data.Insert_user_book>()
                val userBookEntry =
                    mockk<InsertUserBookWithStatusMutation.Data.Insert_user_book.User_book>()

                coEvery {
                    apolloClient.safeMutation(mutation = any<InsertUserBookWithStatusMutation>())
                } returns mutationData

                every {
                    mutationData.insert_user_book
                } returns insertUserBook

                every {
                    insertUserBook.user_book
                } returns userBookEntry

                every {
                    userBookEntry.toBook()
                } returns expectedBook

                // ----- Act -----
                dataSource.markBookAsReading(
                    book = book,
                    editionId = editionId,
                )

                // ----- Assert -----
                coVerify {
                    apolloClient.safeMutation(
                        mutation = match<InsertUserBookWithStatusMutation> {
                            it.userBookCreateInput.edition_id == Optional.presentIfNotNull(editionId)
                        },
                    )
                }
            }

        @Test
        fun `updates an existing user_book with CURRENTLY_READING status and a user_date`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook(id = 7)
            val book = stubBook(userBook = userBook)

            every {
                book.currentEdition
            } returns stubBookEdition()

            stubUpdateUserBookStatus()

            // ----- Act -----
            dataSource.markBookAsReading(book = book)

            // ----- Assert -----
            coVerify {
                apolloClient.safeMutation(
                    mutation = match<UpdateUserBookStatusMutation> {
                        it.id == 7 &&
                            it.`object`.status_id == Optional.present(UserBookStatus.CURRENTLY_READING.code) &&
                            it.`object`.user_date != Optional.Absent
                    },
                )
            }
        }

        @Test
        fun `throws when mutation returns null update_user_book wrapper`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook(id = 7)
            val book = stubBook(userBook = userBook)
            val mutationData = mockk<UpdateUserBookStatusMutation.Data>()

            every {
                book.currentEdition
            } returns stubBookEdition()

            coEvery {
                apolloClient.safeMutation(mutation = any<UpdateUserBookStatusMutation>())
            } returns mutationData

            every {
                mutationData.update_user_book
            } returns null

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.markBookAsReading(book = book)
            }
        }
    }

    @Nested
    inner class MarkBookAsPaused {
        @Test
        fun `updates an existing user_book with PAUSED status and a user_date`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook(id = 7)
            val book = stubBook(userBook = userBook)
            val expectedBook = stubBook()

            every {
                book.currentEdition
            } returns stubBookEdition()

            stubUpdateUserBookStatus(result = expectedBook)

            // ----- Act -----
            val result = dataSource.markBookAsPaused(book = book)

            // ----- Assert -----
            result shouldBe expectedBook
            coVerify {
                apolloClient.safeMutation(
                    mutation = match<UpdateUserBookStatusMutation> {
                        it.id == 7 &&
                            it.`object`.status_id == Optional.present(UserBookStatus.PAUSED.code) &&
                            it.`object`.user_date != Optional.Absent
                    },
                )
            }
        }

        @Test
        fun `inserts with PAUSED status and a user_date when book has no userBook`() = runTest {
            // ----- Arrange -----
            val bookId = 5
            val book = stubBook(
                id = bookId,
                userBook = null,
            )
            val expectedBook = stubBook()
            val mutationData = mockk<InsertUserBookWithStatusMutation.Data>()
            val insertUserBook = mockk<InsertUserBookWithStatusMutation.Data.Insert_user_book>()
            val userBookEntry = mockk<InsertUserBookWithStatusMutation.Data.Insert_user_book.User_book>()

            coEvery {
                apolloClient.safeMutation(mutation = any<InsertUserBookWithStatusMutation>())
            } returns mutationData

            every {
                mutationData.insert_user_book
            } returns insertUserBook

            every {
                insertUserBook.user_book
            } returns userBookEntry

            every {
                userBookEntry.toBook()
            } returns expectedBook

            // ----- Act -----
            val result = dataSource.markBookAsPaused(book = book)

            // ----- Assert -----
            result shouldBe expectedBook
            coVerify {
                apolloClient.safeMutation(
                    mutation = match<InsertUserBookWithStatusMutation> {
                        it.userBookCreateInput.book_id == bookId &&
                            it.userBookCreateInput.status_id == Optional.present(UserBookStatus.PAUSED.code) &&
                            it.userBookCreateInput.user_date != Optional.Absent
                    },
                )
            }
        }

        @Test
        fun `passes the editionId to the insert when book has no userBook`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBook = null)
            val editionId = 42
            val mutationData = mockk<InsertUserBookWithStatusMutation.Data>()
            val insertUserBook = mockk<InsertUserBookWithStatusMutation.Data.Insert_user_book>()
            val userBookEntry = mockk<InsertUserBookWithStatusMutation.Data.Insert_user_book.User_book>()

            coEvery {
                apolloClient.safeMutation(mutation = any<InsertUserBookWithStatusMutation>())
            } returns mutationData

            every {
                mutationData.insert_user_book
            } returns insertUserBook

            every {
                insertUserBook.user_book
            } returns userBookEntry

            every {
                userBookEntry.toBook()
            } returns stubBook()

            // ----- Act -----
            dataSource.markBookAsPaused(
                book = book,
                editionId = editionId,
            )

            // ----- Assert -----
            coVerify {
                apolloClient.safeMutation(
                    mutation = match<InsertUserBookWithStatusMutation> {
                        it.userBookCreateInput.edition_id == Optional.presentIfNotNull(editionId)
                    },
                )
            }
        }

        @Test
        fun `throws when update mutation returns null update_user_book wrapper`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBook = stubUserBook(id = 7))
            val mutationData = mockk<UpdateUserBookStatusMutation.Data>()

            coEvery {
                apolloClient.safeMutation(mutation = any<UpdateUserBookStatusMutation>())
            } returns mutationData

            every {
                mutationData.update_user_book
            } returns null

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.markBookAsPaused(book = book)
            }
        }

        @Test
        fun `throws when insert mutation returns null insert_user_book wrapper`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBook = null)
            val mutationData = mockk<InsertUserBookWithStatusMutation.Data>()

            coEvery {
                apolloClient.safeMutation(mutation = any<InsertUserBookWithStatusMutation>())
            } returns mutationData

            every {
                mutationData.insert_user_book
            } returns null

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.markBookAsPaused(book = book)
            }
        }
    }

    @Nested
    inner class UpdateBookRating {
        @Test
        fun `returns the mapped book and issues UpdateUserBookRatingMutation for the user book id`() = runTest {
            // ----- Arrange -----
            val userBookId = 42
            val userBook = stubUserBook(id = userBookId)
            val expectedBook = stubBook()
            val mutationData = mockk<UpdateUserBookRatingMutation.Data>()
            val updateUserBook = mockk<UpdateUserBookRatingMutation.Data.Update_user_book>()
            val userBookEntry = mockk<UpdateUserBookRatingMutation.Data.Update_user_book.User_book>()

            coEvery {
                apolloClient.safeMutation(mutation = any<UpdateUserBookRatingMutation>())
            } returns mutationData

            every {
                mutationData.update_user_book
            } returns updateUserBook

            every {
                updateUserBook.user_book
            } returns userBookEntry

            every {
                userBookEntry.toBook()
            } returns expectedBook

            // ----- Act -----
            val result = dataSource.updateBookRating(
                userBook = userBook,
                rating = 4.5,
            )

            // ----- Assert -----
            result shouldBe expectedBook
            coVerify {
                apolloClient.safeMutation(
                    mutation = match<UpdateUserBookRatingMutation> { it.id == userBookId },
                )
            }
        }

        @Test
        fun `throws when mutation returns null update_user_book wrapper`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook(id = 7)
            val mutationData = mockk<UpdateUserBookRatingMutation.Data>()

            coEvery {
                apolloClient.safeMutation(mutation = any<UpdateUserBookRatingMutation>())
            } returns mutationData

            every {
                mutationData.update_user_book
            } returns null

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.updateBookRating(
                    userBook = userBook,
                    rating = 4.5,
                )
            }
        }
    }

    @Nested
    inner class RemoveBookFromLibrary {
        @Test
        fun `calls safeMutation with the user book id from the given book`() = runTest {
            // ----- Arrange -----
            val userBookId = 99
            val userBook = stubUserBook(id = userBookId)
            val book = stubBook(userBook = userBook)
            val mutationData = mockk<RemoveUserBookMutation.Data>()

            coEvery {
                apolloClient.safeMutation(mutation = any<RemoveUserBookMutation>())
            } returns mutationData

            // ----- Act -----
            dataSource.removeBookFromLibrary(book = book)

            // ----- Assert -----
            coVerify {
                apolloClient.safeMutation(mutation = RemoveUserBookMutation(id = userBookId))
            }
        }

        @Test
        fun `throws when book has no userBook`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBook = null)

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.removeBookFromLibrary(book = book)
            }
        }
    }

    @Nested
    inner class InitializeBooks {
        @Test
        fun `returns mapped books for each user_book entry returned by the query`() = runTest {
            // ----- Arrange -----
            val userId = 1
            val expectedBook = stubBook()
            val queryData = mockk<GetUserBooksQuery.Data>()
            val meEntry = mockk<GetUserBooksQuery.Data.Me>()
            val userBookEntry = mockk<GetUserBooksQuery.Data.Me.User_book>()

            coEvery {
                apolloClient.safeQuery(
                    query = any<GetUserBooksQuery>(),
                    fetchPolicy = any(),
                )
            } returns queryData

            every {
                queryData.me
            } returns listOf(meEntry)

            every {
                meEntry.user_books
            } returns listOf(userBookEntry)

            every {
                userBookEntry.toBook()
            } returns expectedBook

            // ----- Act -----
            val result = dataSource.initializeBooks(userId = userId)

            // ----- Assert -----
            result shouldBe listOf(expectedBook)
        }

        @Test
        fun `throws when me list is empty`() = runTest {
            // ----- Arrange -----
            val queryData = mockk<GetUserBooksQuery.Data>()

            coEvery {
                apolloClient.safeQuery(
                    query = any<GetUserBooksQuery>(),
                    fetchPolicy = any(),
                )
            } returns queryData

            every {
                queryData.me
            } returns emptyList()

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.initializeBooks(userId = 1)
            }
        }

        @Test
        fun `filters out entries whose toBook maps to null`() = runTest {
            // ----- Arrange -----
            val userId = 1
            val queryData = mockk<GetUserBooksQuery.Data>()
            val meEntry = mockk<GetUserBooksQuery.Data.Me>()
            val userBookEntry = mockk<GetUserBooksQuery.Data.Me.User_book>()

            coEvery {
                apolloClient.safeQuery(
                    query = any<GetUserBooksQuery>(),
                    fetchPolicy = any(),
                )
            } returns queryData

            every {
                queryData.me
            } returns listOf(meEntry)

            every {
                meEntry.user_books
            } returns listOf(userBookEntry)

            every {
                userBookEntry.toBook()
            } returns null

            // ----- Act -----
            val result = dataSource.initializeBooks(userId = userId)

            // ----- Assert -----
            result shouldBe emptyList()
        }

        @Test
        fun `skips GetBooksByIdsQuery call when no user book has a differing canonicalId`() = runTest {
            // ----- Arrange -----
            val userId = 1
            val bookWithoutCanonical = stubBook(
                id = 10,
                canonicalId = null,
            )
            val queryData = mockk<GetUserBooksQuery.Data>()
            val meEntry = mockk<GetUserBooksQuery.Data.Me>()
            val userBookEntry = mockk<GetUserBooksQuery.Data.Me.User_book>()

            coEvery {
                apolloClient.safeQuery(
                    query = any<GetUserBooksQuery>(),
                    fetchPolicy = any(),
                )
            } returns queryData

            every {
                queryData.me
            } returns listOf(meEntry)

            every {
                meEntry.user_books
            } returns listOf(userBookEntry)

            every {
                userBookEntry.toBook()
            } returns bookWithoutCanonical

            // ----- Act -----
            val result = dataSource.initializeBooks(userId = userId)

            // ----- Assert -----
            result shouldBe listOf(bookWithoutCanonical)
            coVerify(exactly = 0) {
                apolloClient.safeQuery(
                    query = match<Query<GetBooksByIdsQuery.Data>> { it is GetBooksByIdsQuery },
                    fetchPolicy = any(),
                )
            }
        }

        @Test
        fun `replaces a merged book's metadata with the canonical book's and keeps the user's own data`() = runTest {
            // ----- Arrange -----
            val original = baseBook.copy(
                id = 10,
                canonicalId = 20,
                title = "Original Title",
                ratingsCount = 5,
            )
            val canonical = baseBook.copy(
                id = 20,
                title = "Canonical Title",
                rating = 4.5,
                description = "Canonical description",
                releaseYear = 2020,
                coverUrl = "https://covers.example.com/canonical.jpg",
                authors = listOf(
                    Author(
                        id = 2,
                        name = "Canonical Author",
                    ),
                ),
                usersCount = 500,
                ratingsCount = 999,
                positionsInSeries = listOf(1.0),
                isCompilation = true,
                userBook = null,
                userBookRead = null,
            )

            stubUserBooksQuery(userBooks = listOf(original))
            stubCanonicalBooksQuery(canonicalBooks = listOf(canonical))

            // ----- Act -----
            val result = dataSource.initializeBooks(userId = 1).single()

            // ----- Assert -----
            result.id shouldBe canonical.id
            result.canonicalId shouldBe null
            result.title shouldBe canonical.title
            result.rating shouldBe canonical.rating
            result.description shouldBe canonical.description
            result.releaseYear shouldBe canonical.releaseYear
            result.coverUrl shouldBe canonical.coverUrl
            result.authors shouldBe canonical.authors
            result.usersCount shouldBe canonical.usersCount
            result.ratingsCount shouldBe canonical.ratingsCount
            result.positionsInSeries shouldBe canonical.positionsInSeries
            result.isCompilation shouldBe canonical.isCompilation
            result.userBook shouldBe original.userBook
            result.userBookRead shouldBe original.userBookRead
        }

        @Test
        fun `fetches the distinct canonical ids in one batch and merges only the books that have one`() = runTest {
            // ----- Arrange -----
            val first = baseBook.copy(
                id = 10,
                canonicalId = 20,
            )
            val plain = baseBook.copy(
                id = 11,
                canonicalId = null,
            )
            val second = baseBook.copy(
                id = 12,
                canonicalId = 20,
            )
            val third = baseBook.copy(
                id = 13,
                canonicalId = 30,
            )
            val canonical20 = baseBook.copy(id = 20)
            val canonical30 = baseBook.copy(id = 30)

            stubUserBooksQuery(userBooks = listOf(first, plain, second, third))
            stubCanonicalBooksQuery(canonicalBooks = listOf(canonical20, canonical30))

            // ----- Act -----
            val result = dataSource.initializeBooks(userId = 1)

            // ----- Assert -----
            result.map { it.id } shouldBe listOf(20, 11, 20, 30)
            coVerify(exactly = 1) {
                apolloClient.safeQuery(
                    query = match<Query<GetBooksByIdsQuery.Data>> {
                        it is GetBooksByIdsQuery && it.ids == listOf(20, 30)
                    },
                    fetchPolicy = any(),
                )
            }
        }

        @Test
        fun `keeps the pre-merge book when its canonical is missing from the response`() = runTest {
            // ----- Arrange -----
            val book = baseBook.copy(
                id = 10,
                canonicalId = 20,
            )

            stubUserBooksQuery(userBooks = listOf(book))
            stubCanonicalBooksQuery(canonicalBooks = emptyList())

            // ----- Act -----
            val result = dataSource.initializeBooks(userId = 1)

            // ----- Assert -----
            result shouldBe listOf(book)
        }

        private fun stubUserBooksQuery(userBooks: List<Book>) {
            val queryData = mockk<GetUserBooksQuery.Data>()
            val meEntry = mockk<GetUserBooksQuery.Data.Me>()
            val entries = userBooks.map { book ->
                mockk<GetUserBooksQuery.Data.Me.User_book> {
                    every {
                        toBook()
                    } returns book
                }
            }

            coEvery {
                apolloClient.safeQuery(
                    query = match<Query<GetUserBooksQuery.Data>> { it is GetUserBooksQuery },
                    fetchPolicy = any(),
                )
            } returns queryData

            every {
                queryData.me
            } returns listOf(meEntry)

            every {
                meEntry.user_books
            } returns entries
        }

        private fun stubCanonicalBooksQuery(canonicalBooks: List<Book>) {
            val queryData = mockk<GetBooksByIdsQuery.Data>()
            val entries = canonicalBooks.map { canonical ->
                mockk<GetBooksByIdsQuery.Data.Book>().also { entry ->
                    every {
                        with(GetBooksByIdsQuery.Data.Book.Companion) { entry.bookDetailFragment() }
                    } returns mockk {
                        every {
                            toBook()
                        } returns canonical
                    }
                }
            }

            coEvery {
                apolloClient.safeQuery(
                    query = match<Query<GetBooksByIdsQuery.Data>> { it is GetBooksByIdsQuery },
                    fetchPolicy = any(),
                )
            } returns queryData

            every {
                queryData.books
            } returns entries
        }
    }

    @Nested
    inner class UpdateBookProgress {
        @Test
        fun `throws when book has no userBook`() = runTest {
            // ----- Arrange -----
            val book = stubBook(
                userBook = null,
                userBookRead = stubUserBookRead(),
            )

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.updateBookProgress(
                    book = book,
                    newPage = 50,
                )
            }
        }

        @Test
        fun `throws when book has no userBookRead`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook(id = 3)
            val book = stubBook(
                userBook = userBook,
                userBookRead = null,
            )

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.updateBookProgress(
                    book = book,
                    newPage = 50,
                )
            }
        }

        @Test
        fun `throws when mutation returns null userBookRead fragment`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook(id = 3)
            val userBookRead = stubUserBookRead(id = 5)
            val book = stubBook(
                userBook = userBook,
                userBookRead = userBookRead,
            )
            val mutationData = mockk<UpdateReadingProgressMutation.Data>()
            val updateUserBookRead = mockk<UpdateReadingProgressMutation.Data.Update_user_book_read>()
            val userBookReadEntry = mockk<UpdateReadingProgressMutation.Data.Update_user_book_read.User_book_read>()

            every {
                userBook.editionId
            } returns 10

            every {
                userBookRead.currentPage
            } returns 50

            every {
                userBookRead.progress
            } returns 0f

            coEvery {
                apolloClient.safeMutation(mutation = any<UpdateReadingProgressMutation>())
            } returns mutationData

            every {
                mutationData.update_user_book_read
            } returns updateUserBookRead

            every {
                updateUserBookRead.user_book_read
            } returns userBookReadEntry

            every {
                userBookReadEntry.userBookReadFragment()
            } returns null

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.updateBookProgress(
                    book = book,
                    newPage = 50,
                )
            }
        }

        @Test
        fun `returns updated book copy with new page and appended journal entry on success`() = runTest {
            // ----- Arrange -----
            val newPage = 120
            val existingJournals = listOf(
                ReadingJournal(
                    updatedAt = "2024-01-01T00:00:00.000000",
                    event = "status_updated",
                ),
            )
            val userBook = stubUserBook(
                id = 3,
                journals = existingJournals,
            )
            val userBookRead = stubUserBookRead(id = 5)
            val book = stubBook(
                userBook = userBook,
                userBookRead = userBookRead,
            )
            val mutationData = mockk<UpdateReadingProgressMutation.Data>()
            val updateUserBookRead = mockk<UpdateReadingProgressMutation.Data.Update_user_book_read>()
            val userBookReadEntry = mockk<UpdateReadingProgressMutation.Data.Update_user_book_read.User_book_read>()
            val userBookGqlEntry = mockk<UpdateReadingProgressMutation.Data.Update_user_book_read.User_book_read.User_book>()
            val expectedBook = stubBook()

            every {
                userBook.editionId
            } returns 10

            coEvery {
                apolloClient.safeMutation(mutation = any<UpdateReadingProgressMutation>())
            } returns mutationData

            every {
                mutationData.update_user_book_read
            } returns updateUserBookRead

            every {
                updateUserBookRead.user_book_read
            } returns userBookReadEntry

            every {
                userBookReadEntry.user_book
            } returns userBookGqlEntry

            every {
                userBookGqlEntry.toBook()
            } returns expectedBook

            // ----- Act -----
            val result = dataSource.updateBookProgress(
                book = book,
                newPage = newPage,
            )

            // ----- Assert -----
            result shouldBe expectedBook
        }

        @Test
        fun `omits progress_pages and sends progress_seconds when newSeconds is provided`() = runTest {
            // ----- Arrange -----
            val newSeconds = 3600
            val userBook = stubUserBook(id = 3)
            val userBookRead = stubUserBookRead(id = 5)
            val book = stubBook(
                userBook = userBook,
                userBookRead = userBookRead,
            )
            val mutationData = mockk<UpdateReadingProgressMutation.Data>()
            val updateUserBookRead = mockk<UpdateReadingProgressMutation.Data.Update_user_book_read>()
            val userBookReadEntry = mockk<UpdateReadingProgressMutation.Data.Update_user_book_read.User_book_read>()
            val userBookGqlEntry = mockk<UpdateReadingProgressMutation.Data.Update_user_book_read.User_book_read.User_book>()
            val expectedBook = stubBook()

            every {
                userBook.editionId
            } returns 10

            coEvery {
                apolloClient.safeMutation(mutation = any<UpdateReadingProgressMutation>())
            } returns mutationData

            every {
                mutationData.update_user_book_read
            } returns updateUserBookRead

            every {
                updateUserBookRead.user_book_read
            } returns userBookReadEntry

            every {
                userBookReadEntry.user_book
            } returns userBookGqlEntry

            every {
                userBookGqlEntry.toBook()
            } returns expectedBook

            // ----- Act -----
            dataSource.updateBookProgress(
                book = book,
                newSeconds = newSeconds,
            )

            // ----- Assert -----
            coVerify {
                apolloClient.safeMutation(
                    mutation = match<UpdateReadingProgressMutation> { mutation ->
                        val input = mutation.datesReadInput
                        input.progress_pages is Optional.Absent &&
                            input.progress_seconds.getOrNull() == newSeconds
                    },
                )
            }
        }

        @Test
        fun `sends progress_pages and omits progress_seconds when newPage is provided`() = runTest {
            // ----- Arrange -----
            val newPage = 75
            val userBook = stubUserBook(id = 3)
            val userBookRead = stubUserBookRead(id = 5)
            val book = stubBook(
                userBook = userBook,
                userBookRead = userBookRead,
            )
            val mutationData = mockk<UpdateReadingProgressMutation.Data>()
            val updateUserBookRead = mockk<UpdateReadingProgressMutation.Data.Update_user_book_read>()
            val userBookReadEntry = mockk<UpdateReadingProgressMutation.Data.Update_user_book_read.User_book_read>()
            val userBookGqlEntry = mockk<UpdateReadingProgressMutation.Data.Update_user_book_read.User_book_read.User_book>()
            val expectedBook = stubBook()

            every {
                userBook.editionId
            } returns 10

            coEvery {
                apolloClient.safeMutation(mutation = any<UpdateReadingProgressMutation>())
            } returns mutationData

            every {
                mutationData.update_user_book_read
            } returns updateUserBookRead

            every {
                updateUserBookRead.user_book_read
            } returns userBookReadEntry

            every {
                userBookReadEntry.user_book
            } returns userBookGqlEntry

            every {
                userBookGqlEntry.toBook()
            } returns expectedBook

            // ----- Act -----
            dataSource.updateBookProgress(
                book = book,
                newPage = newPage,
            )

            // ----- Assert -----
            coVerify {
                apolloClient.safeMutation(
                    mutation = match<UpdateReadingProgressMutation> { mutation ->
                        val input = mutation.datesReadInput
                        input.progress_pages.getOrNull() == newPage &&
                            input.progress_seconds is Optional.Absent
                    },
                )
            }
        }

        @Test
        fun `sends action_at and action when actionAt is provided`() = runTest {
            // ----- Arrange -----
            val actionAt = "2026-07-21T21:00:00Z"
            val userBook = stubUserBook(id = 3)
            val userBookRead = stubUserBookRead(id = 5)
            val book = stubBook(
                userBook = userBook,
                userBookRead = userBookRead,
            )
            val mutationData = mockk<UpdateReadingProgressMutation.Data>()
            val updateUserBookRead = mockk<UpdateReadingProgressMutation.Data.Update_user_book_read>()
            val userBookReadEntry = mockk<UpdateReadingProgressMutation.Data.Update_user_book_read.User_book_read>()
            val userBookGqlEntry = mockk<UpdateReadingProgressMutation.Data.Update_user_book_read.User_book_read.User_book>()
            val expectedBook = stubBook()

            every {
                userBook.editionId
            } returns 10

            coEvery {
                apolloClient.safeMutation(mutation = any<UpdateReadingProgressMutation>())
            } returns mutationData

            every {
                mutationData.update_user_book_read
            } returns updateUserBookRead

            every {
                updateUserBookRead.user_book_read
            } returns userBookReadEntry

            every {
                userBookReadEntry.user_book
            } returns userBookGqlEntry

            every {
                userBookGqlEntry.toBook()
            } returns expectedBook

            // ----- Act -----
            dataSource.updateBookProgress(
                book = book,
                newPage = 75,
                actionAt = actionAt,
            )

            // ----- Assert -----
            coVerify {
                apolloClient.safeMutation(
                    mutation = match<UpdateReadingProgressMutation> { mutation ->
                        val input = mutation.datesReadInput
                        input.action_at.getOrNull() == actionAt &&
                            input.action.getOrNull() == "progress_updated"
                    },
                )
            }
        }

        @Test
        fun `omits action_at and action when actionAt is not provided`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook(id = 3)
            val userBookRead = stubUserBookRead(id = 5)
            val book = stubBook(
                userBook = userBook,
                userBookRead = userBookRead,
            )
            val mutationData = mockk<UpdateReadingProgressMutation.Data>()
            val updateUserBookRead = mockk<UpdateReadingProgressMutation.Data.Update_user_book_read>()
            val userBookReadEntry = mockk<UpdateReadingProgressMutation.Data.Update_user_book_read.User_book_read>()
            val userBookGqlEntry = mockk<UpdateReadingProgressMutation.Data.Update_user_book_read.User_book_read.User_book>()
            val expectedBook = stubBook()

            every {
                userBook.editionId
            } returns 10

            coEvery {
                apolloClient.safeMutation(mutation = any<UpdateReadingProgressMutation>())
            } returns mutationData

            every {
                mutationData.update_user_book_read
            } returns updateUserBookRead

            every {
                updateUserBookRead.user_book_read
            } returns userBookReadEntry

            every {
                userBookReadEntry.user_book
            } returns userBookGqlEntry

            every {
                userBookGqlEntry.toBook()
            } returns expectedBook

            // ----- Act -----
            dataSource.updateBookProgress(
                book = book,
                newPage = 75,
            )

            // ----- Assert -----
            coVerify {
                apolloClient.safeMutation(
                    mutation = match<UpdateReadingProgressMutation> { mutation ->
                        val input = mutation.datesReadInput
                        input.action_at is Optional.Absent && input.action is Optional.Absent
                    },
                )
            }
        }
    }

    @Nested
    inner class ReplayUpdateBookProgress {
        @Test
        fun `sends action_at and action when actionAt is provided`() = runTest {
            // ----- Arrange -----
            val actionAt = "2026-07-21T21:00:00Z"

            coEvery {
                apolloClient.safeMutation(mutation = any<UpdateReadingProgressMutation>())
            } returns mockk(relaxed = true)

            // ----- Act -----
            dataSource.replayUpdateBookProgress(
                userBookReadId = 5,
                editionId = 10,
                progressPages = 75,
                progressSeconds = null,
                startedAt = "2026-01-01",
                finishedAt = null,
                actionAt = actionAt,
            )

            // ----- Assert -----
            coVerify {
                apolloClient.safeMutation(
                    mutation = match<UpdateReadingProgressMutation> { mutation ->
                        val input = mutation.datesReadInput
                        input.action_at.getOrNull() == actionAt &&
                            input.action.getOrNull() == "progress_updated"
                    },
                )
            }
        }

        @Test
        fun `omits action_at and action when actionAt is not provided`() = runTest {
            // ----- Arrange -----
            coEvery {
                apolloClient.safeMutation(mutation = any<UpdateReadingProgressMutation>())
            } returns mockk(relaxed = true)

            // ----- Act -----
            dataSource.replayUpdateBookProgress(
                userBookReadId = 5,
                editionId = 10,
                progressPages = 75,
                progressSeconds = null,
                startedAt = "2026-01-01",
                finishedAt = null,
                actionAt = null,
            )

            // ----- Assert -----
            coVerify {
                apolloClient.safeMutation(
                    mutation = match<UpdateReadingProgressMutation> { mutation ->
                        val input = mutation.datesReadInput
                        input.action_at is Optional.Absent && input.action is Optional.Absent
                    },
                )
            }
        }
    }

    @Nested
    inner class MarkBookAsRead {
        @Test
        fun `throws when mutation returns null insert_user_book wrapper`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            val mutationData = mockk<MarkBookAsReadMutation.Data>()

            every {
                book.id
            } returns 11

            coEvery {
                apolloClient.safeMutation(mutation = any<MarkBookAsReadMutation>())
            } returns mutationData

            every {
                mutationData.insert_user_book
            } returns null

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.markBookAsRead(book = book)
            }
        }

        @Test
        fun `returns mapped book when mutation succeeds`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            val expectedBook = stubBook()
            val mutationData = mockk<MarkBookAsReadMutation.Data>()
            val insertUserBook = mockk<MarkBookAsReadMutation.Data.Insert_user_book>()
            val userBookEntry = mockk<MarkBookAsReadMutation.Data.Insert_user_book.User_book>()

            every {
                book.id
            } returns 11

            coEvery {
                apolloClient.safeMutation(mutation = any<MarkBookAsReadMutation>())
            } returns mutationData

            every {
                mutationData.insert_user_book
            } returns insertUserBook

            every {
                insertUserBook.user_book
            } returns userBookEntry

            every {
                userBookEntry.toBook()
            } returns expectedBook

            // ----- Act -----
            val result = dataSource.markBookAsRead(book = book)

            // ----- Assert -----
            result shouldBe expectedBook
        }

        @Test
        fun `sets edition_id to Optional presentIfNotNull when editionId is non-null`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            val editionId = 77
            val expectedBook = stubBook()
            val mutationData = mockk<MarkBookAsReadMutation.Data>()
            val insertUserBook = mockk<MarkBookAsReadMutation.Data.Insert_user_book>()
            val userBookEntry = mockk<MarkBookAsReadMutation.Data.Insert_user_book.User_book>()

            every {
                book.id
            } returns 11

            coEvery {
                apolloClient.safeMutation(mutation = any<MarkBookAsReadMutation>())
            } returns mutationData

            every {
                mutationData.insert_user_book
            } returns insertUserBook

            every {
                insertUserBook.user_book
            } returns userBookEntry

            every {
                userBookEntry.toBook()
            } returns expectedBook

            // ----- Act -----
            dataSource.markBookAsRead(
                book = book,
                editionId = editionId,
            )

            // ----- Assert -----
            coVerify {
                apolloClient.safeMutation(
                    mutation = match<MarkBookAsReadMutation> {
                        it.userBookCreateInput.edition_id == Optional.presentIfNotNull(editionId)
                    },
                )
            }
        }

        @Test
        fun `sets user_date to actionAt's local date and action_at present when actionAt is provided`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            val actionAt = "2026-07-21T21:00:00Z"
            val expectedUserDate = Instant.parse(actionAt).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
            val expectedBook = stubBook()
            val mutationData = mockk<MarkBookAsReadMutation.Data>()
            val insertUserBook = mockk<MarkBookAsReadMutation.Data.Insert_user_book>()
            val userBookEntry = mockk<MarkBookAsReadMutation.Data.Insert_user_book.User_book>()

            every {
                book.id
            } returns 11

            coEvery {
                apolloClient.safeMutation(mutation = any<MarkBookAsReadMutation>())
            } returns mutationData

            every {
                mutationData.insert_user_book
            } returns insertUserBook

            every {
                insertUserBook.user_book
            } returns userBookEntry

            every {
                userBookEntry.toBook()
            } returns expectedBook

            // ----- Act -----
            dataSource.markBookAsRead(
                book = book,
                actionAt = actionAt,
            )

            // ----- Assert -----
            coVerify {
                apolloClient.safeMutation(
                    mutation = match<MarkBookAsReadMutation> {
                        it.userBookCreateInput.user_date == Optional.present(expectedUserDate) &&
                            it.userBookCreateInput.action_at == Optional.present(actionAt)
                    },
                )
            }
        }

        @Test
        fun `sets user_date to today and action_at Absent when actionAt is not provided`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            val expectedUserDate = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
            val expectedBook = stubBook()
            val mutationData = mockk<MarkBookAsReadMutation.Data>()
            val insertUserBook = mockk<MarkBookAsReadMutation.Data.Insert_user_book>()
            val userBookEntry = mockk<MarkBookAsReadMutation.Data.Insert_user_book.User_book>()

            every {
                book.id
            } returns 11

            coEvery {
                apolloClient.safeMutation(mutation = any<MarkBookAsReadMutation>())
            } returns mutationData

            every {
                mutationData.insert_user_book
            } returns insertUserBook

            every {
                insertUserBook.user_book
            } returns userBookEntry

            every {
                userBookEntry.toBook()
            } returns expectedBook

            // ----- Act -----
            dataSource.markBookAsRead(book = book)

            // ----- Assert -----
            coVerify {
                apolloClient.safeMutation(
                    mutation = match<MarkBookAsReadMutation> {
                        it.userBookCreateInput.user_date == Optional.present(expectedUserDate) &&
                            it.userBookCreateInput.action_at is Optional.Absent
                    },
                )
            }
        }
    }

    @Nested
    inner class ReplayMarkBookAsRead {
        @Test
        fun `sets user_date to actionAt's local date and action_at present when actionAt is provided`() = runTest {
            // ----- Arrange -----
            val actionAt = "2026-07-21T21:00:00Z"
            val expectedUserDate = Instant.parse(actionAt).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()

            coEvery {
                apolloClient.safeMutation(mutation = any<MarkBookAsReadMutation>())
            } returns mockk(relaxed = true)

            // ----- Act -----
            dataSource.replayMarkBookAsRead(
                bookId = 11,
                userDate = "2026-01-01",
                actionAt = actionAt,
            )

            // ----- Assert -----
            coVerify {
                apolloClient.safeMutation(
                    mutation = match<MarkBookAsReadMutation> {
                        it.userBookCreateInput.user_date == Optional.present(expectedUserDate) &&
                            it.userBookCreateInput.action_at == Optional.present(actionAt)
                    },
                )
            }
        }

        @Test
        fun `sets user_date to the given userDate and action_at Absent when actionAt is not provided`() = runTest {
            // ----- Arrange -----
            val userDate = "2026-01-01"

            coEvery {
                apolloClient.safeMutation(mutation = any<MarkBookAsReadMutation>())
            } returns mockk(relaxed = true)

            // ----- Act -----
            dataSource.replayMarkBookAsRead(
                bookId = 11,
                userDate = userDate,
                actionAt = null,
            )

            // ----- Assert -----
            coVerify {
                apolloClient.safeMutation(
                    mutation = match<MarkBookAsReadMutation> {
                        it.userBookCreateInput.user_date == Optional.present(userDate) &&
                            it.userBookCreateInput.action_at is Optional.Absent
                    },
                )
            }
        }
    }

    @Nested
    inner class UpdateBookEdition {
        @Test
        fun `sends the user book's fields with the new edition id and returns the mapped book`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook(id = 3)
            val newEditionId = 88
            val expectedBook = stubBook()
            val mutationData = mockk<UpdateBookEditionMutation.Data>()
            val updateUserBook = mockk<UpdateBookEditionMutation.Data.Update_user_book>()
            val userBookEntry = mockk<UpdateBookEditionMutation.Data.Update_user_book.User_book>()

            every {
                userBook.dateAdded
            } returns "2024-01-01"

            every {
                userBook.lastReadDate
            } returns null

            every {
                userBook.rating
            } returns null

            every {
                userBook.referrerUserId
            } returns null

            every {
                userBook.reviewHasSpoilers
            } returns false

            every {
                userBook.reviewedAt
            } returns null

            every {
                userBook.status
            } returns BookStatus.WantToRead

            coEvery {
                apolloClient.safeMutation(mutation = any<UpdateBookEditionMutation>())
            } returns mutationData

            every {
                mutationData.update_user_book
            } returns updateUserBook

            every {
                updateUserBook.user_book
            } returns userBookEntry

            every {
                userBookEntry.toBook()
            } returns expectedBook

            // ----- Act -----
            val result = dataSource.updateBookEdition(
                userBook = userBook,
                newEditionId = newEditionId,
            )

            // ----- Assert -----
            result shouldBe expectedBook
            coVerify(exactly = 1) {
                apolloClient.safeMutation(
                    mutation = match<UpdateBookEditionMutation> {
                        it.id == 3 &&
                            it.`object`.edition_id == Optional.present(newEditionId) &&
                            it.`object`.date_added == Optional.Present("2024-01-01") &&
                            it.`object`.status_id == Optional.present(1) &&
                            it.`object`.review_has_spoilers == Optional.Present(false)
                    },
                )
            }
        }

        @Test
        fun `throws when mutation returns null user book`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook(id = 3)
            val newEditionId = 88
            val mutationData = mockk<UpdateBookEditionMutation.Data>()
            val updateUserBook = mockk<UpdateBookEditionMutation.Data.Update_user_book>()

            every {
                userBook.dateAdded
            } returns "2024-01-01"

            every {
                userBook.lastReadDate
            } returns null

            every {
                userBook.rating
            } returns null

            every {
                userBook.referrerUserId
            } returns null

            every {
                userBook.reviewHasSpoilers
            } returns false

            every {
                userBook.reviewedAt
            } returns null

            every {
                userBook.status
            } returns BookStatus.WantToRead

            coEvery {
                apolloClient.safeMutation(mutation = any<UpdateBookEditionMutation>())
            } returns mutationData

            every {
                mutationData.update_user_book
            } returns updateUserBook

            every {
                updateUserBook.user_book
            } returns null

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.updateBookEdition(
                    userBook = userBook,
                    newEditionId = newEditionId,
                )
            }
        }

        @Test
        fun `omits status_id when the user book status is None`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook(id = 3)
            val mutation = slot<UpdateBookEditionMutation>()

            every {
                userBook.status
            } returns BookStatus.None

            stubUpdateBookEditionMutation(mutation = mutation)

            // ----- Act -----
            dataSource.updateBookEdition(
                userBook = userBook,
                newEditionId = 88,
            )

            // ----- Assert -----
            mutation.captured.`object`.status_id shouldBe Optional.Absent
        }

        @Test
        fun `sends the status code as status_id for a real status`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook(id = 3)
            val mutation = slot<UpdateBookEditionMutation>()

            every {
                userBook.status
            } returns BookStatus.Paused

            stubUpdateBookEditionMutation(mutation = mutation)

            // ----- Act -----
            dataSource.updateBookEdition(
                userBook = userBook,
                newEditionId = 88,
            )

            // ----- Assert -----
            mutation.captured.`object`.status_id shouldBe Optional.present(BookStatus.Paused.code)
        }

        private fun stubUpdateBookEditionMutation(mutation: CapturingSlot<UpdateBookEditionMutation>) {
            val mutationData = mockk<UpdateBookEditionMutation.Data>()
            val updateUserBook = mockk<UpdateBookEditionMutation.Data.Update_user_book>()
            val userBookEntry = mockk<UpdateBookEditionMutation.Data.Update_user_book.User_book>()

            coEvery {
                apolloClient.safeMutation(mutation = capture(mutation))
            } returns mutationData

            every {
                mutationData.update_user_book
            } returns updateUserBook

            every {
                updateUserBook.user_book
            } returns userBookEntry

            every {
                userBookEntry.toBook()
            } returns stubBook()
        }
    }

    @Nested
    inner class ReplayUpdateBookRating {
        @Test
        fun `issues UpdateUserBookRatingMutation with the correct userBookId and rating`() = runTest {
            // ----- Arrange -----
            val userBookId = 42
            val rating = 4.5

            coEvery {
                apolloClient.safeMutation(mutation = any<UpdateUserBookRatingMutation>())
            } returns mockk(relaxed = true)

            // ----- Act -----
            dataSource.replayUpdateBookRating(
                userBookId = userBookId,
                rating = rating,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                apolloClient.safeMutation(
                    mutation = match<UpdateUserBookRatingMutation> { it.id == userBookId },
                )
            }
        }
    }

    @Nested
    inner class ReplayUpdateUserBookStatus {
        @Test
        fun `issues UpdateUserBookStatusMutation with the userBookId, status code and user_date`() = runTest {
            // ----- Arrange -----
            val userBookId = 42
            val userDate = "2026-07-21"

            coEvery {
                apolloClient.safeMutation(mutation = any<UpdateUserBookStatusMutation>())
            } returns mockk(relaxed = true)

            // ----- Act -----
            dataSource.replayUpdateUserBookStatus(
                userBookId = userBookId,
                statusCode = UserBookStatus.PAUSED.code,
                userDate = userDate,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                apolloClient.safeMutation(
                    mutation = match<UpdateUserBookStatusMutation> {
                        it.id == userBookId &&
                            it.`object`.status_id == Optional.present(UserBookStatus.PAUSED.code) &&
                            it.`object`.user_date == Optional.present(userDate)
                    },
                )
            }
        }
    }

    @Nested
    inner class UpdateBookReview {
        @Test
        fun `returns the mapped book and issues UpdateUserBookReviewMutation for the user book id`() = runTest {
            // ----- Arrange -----
            val userBookId = 42
            val userBook = stubUserBook(id = userBookId)
            val expectedBook = stubBook()
            val mutationData = mockk<UpdateUserBookReviewMutation.Data>()
            val updateUserBook = mockk<UpdateUserBookReviewMutation.Data.Update_user_book>()
            val userBookEntry = mockk<UpdateUserBookReviewMutation.Data.Update_user_book.User_book>()

            coEvery {
                apolloClient.safeMutation(mutation = any<UpdateUserBookReviewMutation>())
            } returns mutationData

            every {
                mutationData.update_user_book
            } returns updateUserBook

            every {
                updateUserBook.user_book
            } returns userBookEntry

            every {
                userBookEntry.toBook()
            } returns expectedBook

            // ----- Act -----
            val result = dataSource.updateBookReview(
                userBook = userBook,
                review = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("Great read"))))),
                hasSpoilers = false,
                reviewedAt = "2026-05-30",
            )

            // ----- Assert -----
            result shouldBe expectedBook
            coVerify {
                apolloClient.safeMutation(
                    mutation = match<UpdateUserBookReviewMutation> { it.id == userBookId },
                )
            }
        }

        @Test
        fun `throws when mutation returns null update_user_book wrapper`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook(id = 7)
            val mutationData = mockk<UpdateUserBookReviewMutation.Data>()

            coEvery {
                apolloClient.safeMutation(mutation = any<UpdateUserBookReviewMutation>())
            } returns mutationData

            every {
                mutationData.update_user_book
            } returns null

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.updateBookReview(
                    userBook = userBook,
                    review = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("Great read"))))),
                    hasSpoilers = false,
                    reviewedAt = "2026-05-30",
                )
            }
        }
    }

    @Nested
    inner class FetchEditionMatchesForIsbns {
        @Test
        fun `no matching editions — returns emptyMap`() = runTest {
            // ----- Arrange -----
            val isbn = "0000000000000"
            val queryData = mockk<GetEditionsByIsbnsQuery.Data>()

            coEvery {
                apolloClient.safeQuery(
                    query = any<GetEditionsByIsbnsQuery>(),
                    fetchPolicy = FetchPolicy.NetworkFirst,
                )
            } returns queryData

            every {
                queryData.editions
            } returns emptyList()

            // ----- Act -----
            val result = dataSource.fetchEditionMatchesForIsbns(isbns = listOf(isbn))

            // ----- Assert -----
            result shouldBe emptyMap()
        }

        @Test
        fun `multiple isbns — one matches on isbn13 and another on isbn10, both correctly keyed`() = runTest {
            // ----- Arrange -----
            val isbn13 = "9780451524935"
            val isbn10 = "0141439513"
            val queryData = mockk<GetEditionsByIsbnsQuery.Data>()
            val isbn13Row = mockk<GetEditionsByIsbnsQuery.Data.Edition>()
            val isbn10Row = mockk<GetEditionsByIsbnsQuery.Data.Edition>()

            coEvery {
                apolloClient.safeQuery(
                    query = any<GetEditionsByIsbnsQuery>(),
                    fetchPolicy = FetchPolicy.NetworkFirst,
                )
            } returns queryData

            every {
                queryData.editions
            } returns listOf(isbn13Row, isbn10Row)
            every {
                isbn13Row.book_id
            } returns 10
            every {
                isbn13Row.id
            } returns 110
            every {
                isbn13Row.isbn_13
            } returns isbn13
            every {
                isbn13Row.isbn_10
            } returns null
            every {
                isbn10Row.book_id
            } returns 20
            every {
                isbn10Row.id
            } returns 220
            every {
                isbn10Row.isbn_13
            } returns null
            every {
                isbn10Row.isbn_10
            } returns isbn10

            // ----- Act -----
            val result = dataSource.fetchEditionMatchesForIsbns(isbns = listOf(isbn13, isbn10))

            // ----- Assert -----
            result shouldBe mapOf(
                isbn13 to IsbnEditionMatch(
                    bookId = 10,
                    editionId = 110,
                ),
                isbn10 to IsbnEditionMatch(
                    bookId = 20,
                    editionId = 220,
                ),
            )
        }

        @Test
        fun `unmatched requested isbn is absent while the other requested isbn is present`() = runTest {
            // ----- Arrange -----
            val matchedIsbn = "9780451524935"
            val unmatchedIsbn = "0000000000000"
            val queryData = mockk<GetEditionsByIsbnsQuery.Data>()
            val editionRow = mockk<GetEditionsByIsbnsQuery.Data.Edition>()

            coEvery {
                apolloClient.safeQuery(
                    query = any<GetEditionsByIsbnsQuery>(),
                    fetchPolicy = FetchPolicy.NetworkFirst,
                )
            } returns queryData

            every {
                queryData.editions
            } returns listOf(editionRow)
            every {
                editionRow.book_id
            } returns 10
            every {
                editionRow.id
            } returns 110
            every {
                editionRow.isbn_13
            } returns matchedIsbn
            every {
                editionRow.isbn_10
            } returns null

            // ----- Act -----
            val result = dataSource.fetchEditionMatchesForIsbns(isbns = listOf(matchedIsbn, unmatchedIsbn))

            // ----- Assert -----
            result shouldBe mapOf(
                matchedIsbn to IsbnEditionMatch(
                    bookId = 10,
                    editionId = 110,
                ),
            )
        }

        @Test
        fun `isbn13 row wins over a separate isbn10 row matching the same requested isbn`() = runTest {
            // ----- Arrange -----
            val isbn = "9780451524935"
            val queryData = mockk<GetEditionsByIsbnsQuery.Data>()
            val isbn13Row = mockk<GetEditionsByIsbnsQuery.Data.Edition>()
            val isbn10Row = mockk<GetEditionsByIsbnsQuery.Data.Edition>()

            coEvery {
                apolloClient.safeQuery(
                    query = any<GetEditionsByIsbnsQuery>(),
                    fetchPolicy = FetchPolicy.NetworkFirst,
                )
            } returns queryData

            every {
                queryData.editions
            } returns listOf(isbn10Row, isbn13Row)
            every {
                isbn13Row.book_id
            } returns 10
            every {
                isbn13Row.id
            } returns 110
            every {
                isbn13Row.isbn_13
            } returns isbn
            every {
                isbn13Row.isbn_10
            } returns null
            every {
                isbn10Row.book_id
            } returns 99
            every {
                isbn10Row.id
            } returns 999
            every {
                isbn10Row.isbn_13
            } returns null
            every {
                isbn10Row.isbn_10
            } returns isbn

            // ----- Act -----
            val result = dataSource.fetchEditionMatchesForIsbns(isbns = listOf(isbn))

            // ----- Assert -----
            result shouldBe mapOf(
                isbn to IsbnEditionMatch(
                    bookId = 10,
                    editionId = 110,
                ),
            )
        }

        @Test
        fun `single row matches on both isbn13 and isbn10 — both requested isbns map to the same edition`() = runTest {
            // ----- Arrange -----
            val isbn13 = "9780451524935"
            val isbn10 = "0141439513"
            val queryData = mockk<GetEditionsByIsbnsQuery.Data>()
            val editionRow = mockk<GetEditionsByIsbnsQuery.Data.Edition>()

            coEvery {
                apolloClient.safeQuery(
                    query = any<GetEditionsByIsbnsQuery>(),
                    fetchPolicy = FetchPolicy.NetworkFirst,
                )
            } returns queryData

            every {
                queryData.editions
            } returns listOf(editionRow)
            every {
                editionRow.book_id
            } returns 10
            every {
                editionRow.id
            } returns 110
            every {
                editionRow.isbn_13
            } returns isbn13
            every {
                editionRow.isbn_10
            } returns isbn10

            // ----- Act -----
            val result = dataSource.fetchEditionMatchesForIsbns(isbns = listOf(isbn13, isbn10))

            // ----- Assert -----
            result shouldBe mapOf(
                isbn13 to IsbnEditionMatch(
                    bookId = 10,
                    editionId = 110,
                ),
                isbn10 to IsbnEditionMatch(
                    bookId = 10,
                    editionId = 110,
                ),
            )
        }

        @Test
        fun `returns empty map immediately when isbns input is empty`() = runTest {
            // ----- Arrange -----
            // (no Apollo call expected — short-circuit path)

            // ----- Act -----
            val result = dataSource.fetchEditionMatchesForIsbns(isbns = emptyList())

            // ----- Assert -----
            result shouldBe emptyMap()
            coVerify(exactly = 0) {
                apolloClient.safeQuery(
                    query = any<GetEditionsByIsbnsQuery>(),
                    fetchPolicy = any(),
                )
            }
        }
    }

    @Nested
    inner class ReplayUpdateBookReview {
        @Test
        fun `issues UpdateUserBookReviewMutation with the correct userBookId`() = runTest {
            // ----- Arrange -----
            val userBookId = 42

            coEvery {
                apolloClient.safeMutation(mutation = any<UpdateUserBookReviewMutation>())
            } returns mockk(relaxed = true)

            // ----- Act -----
            dataSource.replayUpdateBookReview(
                userBookId = userBookId,
                review = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("Great read"))))),
                hasSpoilers = false,
                reviewedAt = "2026-05-30",
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                apolloClient.safeMutation(
                    mutation = match<UpdateUserBookReviewMutation> { it.id == userBookId },
                )
            }
        }
    }

    @Nested
    inner class AddBookByIsbn {
        @Test
        fun `returns CreatedBook with bookId and editionId when mutation succeeds`() = runTest {
            // ----- Arrange -----
            val isbn = "9780451524935"
            val mutationData = mockk<CreateBookMutation.Data>()
            val upsertBook = mockk<CreateBookMutation.Data.Upsert_book>()
            val bookEntry = mockk<CreateBookMutation.Data.Upsert_book.Book>()
            val editionEntry = mockk<CreateBookMutation.Data.Upsert_book.Edition>()

            coEvery {
                apolloClient.safeMutation(mutation = any<CreateBookMutation>())
            } returns mutationData

            every {
                mutationData.upsert_book
            } returns upsertBook

            every {
                upsertBook.errors
            } returns null

            every {
                upsertBook.book
            } returns bookEntry

            every {
                bookEntry.id
            } returns 42

            every {
                upsertBook.edition
            } returns editionEntry

            every {
                editionEntry.id
            } returns 110

            // ----- Act -----
            val result = dataSource.addBookByIsbn(isbn = isbn)

            // ----- Assert -----
            result shouldBe CreatedBook(
                bookId = 42,
                editionId = 110,
            )
        }

        @Test
        fun `returns CreatedBook with null editionId when edition is absent from response`() = runTest {
            // ----- Arrange -----
            val isbn = "9780451524935"
            val mutationData = mockk<CreateBookMutation.Data>()
            val upsertBook = mockk<CreateBookMutation.Data.Upsert_book>()
            val bookEntry = mockk<CreateBookMutation.Data.Upsert_book.Book>()

            coEvery {
                apolloClient.safeMutation(mutation = any<CreateBookMutation>())
            } returns mutationData

            every {
                mutationData.upsert_book
            } returns upsertBook

            every {
                upsertBook.errors
            } returns null

            every {
                upsertBook.book
            } returns bookEntry

            every {
                bookEntry.id
            } returns 42

            every {
                upsertBook.edition
            } returns null

            // ----- Act -----
            val result = dataSource.addBookByIsbn(isbn = isbn)

            // ----- Assert -----
            result shouldBe CreatedBook(
                bookId = 42,
                editionId = null,
            )
        }

        @Test
        fun `throws when errors list contains a non-blank string`() = runTest {
            // ----- Arrange -----
            val isbn = "9780451524935"
            val mutationData = mockk<CreateBookMutation.Data>()
            val upsertBook = mockk<CreateBookMutation.Data.Upsert_book>()

            coEvery {
                apolloClient.safeMutation(mutation = any<CreateBookMutation>())
            } returns mutationData

            every {
                mutationData.upsert_book
            } returns upsertBook

            every {
                upsertBook.errors
            } returns listOf("Book already exists")

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.addBookByIsbn(isbn = isbn)
            }
        }

        @Test
        fun `throws when book is absent from upsert_book response`() = runTest {
            // ----- Arrange -----
            val isbn = "9780451524935"
            val mutationData = mockk<CreateBookMutation.Data>()
            val upsertBook = mockk<CreateBookMutation.Data.Upsert_book>()

            coEvery {
                apolloClient.safeMutation(mutation = any<CreateBookMutation>())
            } returns mutationData

            every {
                mutationData.upsert_book
            } returns upsertBook

            every {
                upsertBook.errors
            } returns null

            every {
                upsertBook.book
            } returns null

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.addBookByIsbn(isbn = isbn)
            }
        }
    }
}
