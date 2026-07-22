package nl.rhaydus.softcover.core.book.data.datasource

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.api.Query
import com.apollographql.cache.normalized.FetchPolicy
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.CreateBookMutation
import nl.rhaydus.softcover.GetBookByIdQuery
import nl.rhaydus.softcover.GetBookIdByEditionIdQuery
import nl.rhaydus.softcover.GetBooksByIdsQuery
import nl.rhaydus.softcover.GetEditionsByBookIdQuery
import nl.rhaydus.softcover.GetEditionsByIdsQuery
import nl.rhaydus.softcover.GetEditionsByIsbnsQuery
import nl.rhaydus.softcover.GetUserBooksQuery
import nl.rhaydus.softcover.MarkBookAsReadMutation
import nl.rhaydus.softcover.MarkBookAsReadingMutation
import nl.rhaydus.softcover.MarkBookAsReadingViaInsertMutation
import nl.rhaydus.softcover.MarkBookAsWantToReadMutation
import nl.rhaydus.softcover.RemoveUserBookMutation
import nl.rhaydus.softcover.UpdateBookEditionMutation
import nl.rhaydus.softcover.UpdateReadingProgressMutation
import nl.rhaydus.softcover.UpdateReadingProgressMutation.Data.Update_user_book_read.User_book_read.Companion.userBookReadFragment
import nl.rhaydus.softcover.UpdateUserBookRatingMutation
import nl.rhaydus.softcover.UpdateUserBookReviewMutation
import nl.rhaydus.softcover.core.book.data.mapper.toBook
import nl.rhaydus.softcover.core.book.data.mapper.toBookEdition
import nl.rhaydus.softcover.core.book.domain.model.CreatedBook
import nl.rhaydus.softcover.core.book.domain.model.IsbnEditionMatch
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.ReadingJournal
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.ReviewParagraph
import nl.rhaydus.softcover.core.domain.model.ReviewRun
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.network.helper.safeMutation
import nl.rhaydus.softcover.core.network.helper.safeQuery

class BooksRemoteDataSourceImplTest {
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

        mockkStatic("nl.rhaydus.softcover.core.network.helper.ApolloExtensionsKt")
        mockkStatic("nl.rhaydus.softcover.core.book.data.mapper.BookMapperKt")
        mockkObject(UpdateReadingProgressMutation.Data.Update_user_book_read.User_book_read.Companion)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
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

    private fun stubCanonicalBook(
        id: Int = 99,
        title: String = "Canonical Title",
        rating: Double = 4.5,
        description: String = "Canonical description",
        releaseYear: Int = 2020,
        releaseDate: LocalDate? = null,
        coverUrl: String = "https://covers.example.com/canonical.jpg",
        usersCount: Int = 500,
        ratingsCount: Int = 200,
    ): Book = mockk {
        every {
            this@mockk.id
        } returns id

        every {
            this@mockk.canonicalId
        } returns null

        every {
            this@mockk.title
        } returns title

        every {
            this@mockk.rating
        } returns rating

        every {
            this@mockk.description
        } returns description

        every {
            this@mockk.releaseYear
        } returns releaseYear

        every {
            this@mockk.releaseDate
        } returns releaseDate

        every {
            this@mockk.coverUrl
        } returns coverUrl

        every {
            this@mockk.authors
        } returns emptyList()

        every {
            this@mockk.usersCount
        } returns usersCount

        every {
            this@mockk.ratingsCount
        } returns ratingsCount

        every {
            this@mockk.bookSeries
        } returns null

        every {
            this@mockk.positionsInSeries
        } returns emptyList()

        every {
            this@mockk.isCompilation
        } returns false
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

            mockkObject(GetBooksByIdsQuery.Data.Book.Companion)

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

            mockkObject(GetBooksByIdsQuery.Data.Book.Companion)

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
        fun `filters out entries whose toBook maps to null`() = runTest {
            // ----- Arrange -----
            val ids = listOf(1, 2)
            val expectedBook = stubBook(id = 1)
            val queryData = mockk<GetBooksByIdsQuery.Data>()
            val bookEntry1 = mockk<GetBooksByIdsQuery.Data.Book>()
            val bookEntry2 = mockk<GetBooksByIdsQuery.Data.Book>()

            mockkObject(GetBooksByIdsQuery.Data.Book.Companion)

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
                } returns expectedBook
            }

            every {
                with(GetBooksByIdsQuery.Data.Book.Companion) { bookEntry2.bookDetailFragment() }
            } returns mockk {
                every {
                    toBook()
                } returns null
            }

            // ----- Act -----
            val result = dataSource.fetchBooksByIds(ids = ids)

            // ----- Assert -----
            result shouldBe listOf(expectedBook)
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

            mockkObject(GetEditionsByBookIdQuery.Data.Edition.Companion)

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
        fun `returns empty list when editions list is empty`() = runTest {
            // ----- Arrange -----
            val bookId = 5
            val queryData = mockk<GetEditionsByBookIdQuery.Data>()

            coEvery {
                apolloClient.safeQuery(
                    query = any<GetEditionsByBookIdQuery>(),
                    fetchPolicy = any(),
                )
            } returns queryData

            every {
                queryData.editions
            } returns emptyList()

            // ----- Act -----
            val result = dataSource.getEditionsByBookId(bookId = bookId)

            // ----- Assert -----
            result shouldBe emptyList()
        }

        @Test
        fun `filters out editions whose editionDetailFragment companion extension returns null`() = runTest {
            // ----- Arrange -----
            val bookId = 5
            val queryData = mockk<GetEditionsByBookIdQuery.Data>()
            val editionEntry = mockk<GetEditionsByBookIdQuery.Data.Edition>()

            mockkObject(GetEditionsByBookIdQuery.Data.Edition.Companion)

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

            mockkObject(GetEditionsByIdsQuery.Data.Edition.Companion)

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
        fun `returns empty list when query returns no editions`() = runTest {
            // ----- Arrange -----
            val ids = listOf(10)
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
            val result = dataSource.fetchEditionsByIds(ids = ids)

            // ----- Assert -----
            result shouldBe emptyList()
        }

        @Test
        fun `filters out entries whose editionDetailFragment companion extension returns null`() = runTest {
            // ----- Arrange -----
            val ids = listOf(10)
            val queryData = mockk<GetEditionsByIdsQuery.Data>()
            val editionEntry = mockk<GetEditionsByIdsQuery.Data.Edition>()

            mockkObject(GetEditionsByIdsQuery.Data.Edition.Companion)

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
        fun `throws when toBook maps the fragment to null`() = runTest {
            // ----- Arrange -----
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
            } returns null

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.markBookAsWantToRead(bookId = 5)
            }
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

    @Nested
    inner class MarkBookAsReading {
        @Test
        fun `returns mapped book when mutation succeeds`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook(id = 7)
            val book = stubBook(userBook = userBook)
            val expectedBook = stubBook()
            val mutationData = mockk<MarkBookAsReadingMutation.Data>()
            val updateUserBook = mockk<MarkBookAsReadingMutation.Data.Update_user_book>()
            val userBookEntry = mockk<MarkBookAsReadingMutation.Data.Update_user_book.User_book>()

            every {
                book.currentEdition
            } returns stubBookEdition()

            coEvery {
                apolloClient.safeMutation(mutation = any<MarkBookAsReadingMutation>())
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
            val mutationData = mockk<MarkBookAsReadingViaInsertMutation.Data>()
            val insertUserBook = mockk<MarkBookAsReadingViaInsertMutation.Data.Insert_user_book>()
            val userBookEntry = mockk<MarkBookAsReadingViaInsertMutation.Data.Insert_user_book.User_book>()

            coEvery {
                apolloClient.safeMutation(mutation = any<MarkBookAsReadingViaInsertMutation>())
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
            val mutationData = mockk<MarkBookAsReadingViaInsertMutation.Data>()

            coEvery {
                apolloClient.safeMutation(mutation = any<MarkBookAsReadingViaInsertMutation>())
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
        fun `throws when insert mutation returns null insert_user_book user_book`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBook = null)
            val mutationData = mockk<MarkBookAsReadingViaInsertMutation.Data>()
            val insertUserBook = mockk<MarkBookAsReadingViaInsertMutation.Data.Insert_user_book>()

            coEvery {
                apolloClient.safeMutation(mutation = any<MarkBookAsReadingViaInsertMutation>())
            } returns mutationData

            every {
                mutationData.insert_user_book
            } returns insertUserBook

            every {
                insertUserBook.user_book
            } returns null

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.markBookAsReading(book = book)
            }
        }

        @Test
        fun `throws when insert mutation toBook maps the fragment to null`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBook = null)
            val mutationData = mockk<MarkBookAsReadingViaInsertMutation.Data>()
            val insertUserBook = mockk<MarkBookAsReadingViaInsertMutation.Data.Insert_user_book>()
            val userBookEntry = mockk<MarkBookAsReadingViaInsertMutation.Data.Insert_user_book.User_book>()

            coEvery {
                apolloClient.safeMutation(mutation = any<MarkBookAsReadingViaInsertMutation>())
            } returns mutationData

            every {
                mutationData.insert_user_book
            } returns insertUserBook

            every {
                insertUserBook.user_book
            } returns userBookEntry

            every {
                userBookEntry.toBook()
            } returns null

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.markBookAsReading(book = book)
            }
        }

        @Test
        fun `sends status_id CURRENTLY_READING when book has no userBook`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBook = null)
            val expectedBook = stubBook()
            val mutationData = mockk<MarkBookAsReadingViaInsertMutation.Data>()
            val insertUserBook = mockk<MarkBookAsReadingViaInsertMutation.Data.Insert_user_book>()
            val userBookEntry = mockk<MarkBookAsReadingViaInsertMutation.Data.Insert_user_book.User_book>()

            coEvery {
                apolloClient.safeMutation(mutation = any<MarkBookAsReadingViaInsertMutation>())
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
                    mutation = match<MarkBookAsReadingViaInsertMutation> {
                        it.userBookCreateInput.status_id == Optional.present(UserBookStatus.CURRENTLY_READING.code)
                    },
                )
            }
        }

        @Test
        fun `sends a present user_date when book has no userBook`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBook = null)
            val expectedBook = stubBook()
            val mutationData = mockk<MarkBookAsReadingViaInsertMutation.Data>()
            val insertUserBook = mockk<MarkBookAsReadingViaInsertMutation.Data.Insert_user_book>()
            val userBookEntry = mockk<MarkBookAsReadingViaInsertMutation.Data.Insert_user_book.User_book>()

            coEvery {
                apolloClient.safeMutation(mutation = any<MarkBookAsReadingViaInsertMutation>())
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
                    mutation = match<MarkBookAsReadingViaInsertMutation> {
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
                val mutationData = mockk<MarkBookAsReadingViaInsertMutation.Data>()
                val insertUserBook = mockk<MarkBookAsReadingViaInsertMutation.Data.Insert_user_book>()
                val userBookEntry =
                    mockk<MarkBookAsReadingViaInsertMutation.Data.Insert_user_book.User_book>()

                coEvery {
                    apolloClient.safeMutation(mutation = any<MarkBookAsReadingViaInsertMutation>())
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
                        mutation = match<MarkBookAsReadingViaInsertMutation> {
                            it.userBookCreateInput.edition_id == Optional.presentIfNotNull(editionId)
                        },
                    )
                }
            }

        @Test
        fun `sets edition_id to Optional Absent when editionId is null and book has no userBook`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBook = null)
            val expectedBook = stubBook()
            val mutationData = mockk<MarkBookAsReadingViaInsertMutation.Data>()
            val insertUserBook = mockk<MarkBookAsReadingViaInsertMutation.Data.Insert_user_book>()
            val userBookEntry = mockk<MarkBookAsReadingViaInsertMutation.Data.Insert_user_book.User_book>()

            coEvery {
                apolloClient.safeMutation(mutation = any<MarkBookAsReadingViaInsertMutation>())
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
                editionId = null,
            )

            // ----- Assert -----
            coVerify {
                apolloClient.safeMutation(
                    mutation = match<MarkBookAsReadingViaInsertMutation> {
                        it.userBookCreateInput.edition_id == Optional.Absent
                    },
                )
            }
        }

        @Test
        fun `throws when mutation returns null update_user_book wrapper`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook(id = 7)
            val book = stubBook(userBook = userBook)
            val mutationData = mockk<MarkBookAsReadingMutation.Data>()

            every {
                book.currentEdition
            } returns stubBookEdition()

            coEvery {
                apolloClient.safeMutation(mutation = any<MarkBookAsReadingMutation>())
            } returns mutationData

            every {
                mutationData.update_user_book
            } returns null

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.markBookAsReading(book = book)
            }
        }

        @Test
        fun `throws when toBook maps the fragment to null`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook(id = 7)
            val book = stubBook(userBook = userBook)
            val mutationData = mockk<MarkBookAsReadingMutation.Data>()
            val updateUserBook = mockk<MarkBookAsReadingMutation.Data.Update_user_book>()
            val userBookEntry = mockk<MarkBookAsReadingMutation.Data.Update_user_book.User_book>()

            every {
                book.currentEdition
            } returns stubBookEdition()

            coEvery {
                apolloClient.safeMutation(mutation = any<MarkBookAsReadingMutation>())
            } returns mutationData

            every {
                mutationData.update_user_book
            } returns updateUserBook

            every {
                updateUserBook.user_book
            } returns userBookEntry

            every {
                userBookEntry.toBook()
            } returns null

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.markBookAsReading(book = book)
            }
        }
    }

    @Nested
    inner class UpdateBookRating {
        @Test
        fun `returns mapped book when mutation succeeds`() = runTest {
            // ----- Arrange -----
            val userBookId = 7
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
        }

        @Test
        fun `issues UpdateUserBookRatingMutation with the correct user book id`() = runTest {
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
            dataSource.updateBookRating(
                userBook = userBook,
                rating = 4.5,
            )

            // ----- Assert -----
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

        @Test
        fun `throws when toBook maps the fragment to null`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook(id = 7)
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
        fun `merges single book against its canonical when canonicalId differs from id`() = runTest {
            // ----- Arrange -----
            val userId = 1
            val canonicalId = 20
            val originalBook = stubBook(
                id = 10,
                canonicalId = canonicalId,
            )
            val canonicalBook = stubCanonicalBook(id = canonicalId)
            val mergedBook = stubBook(
                id = canonicalId,
                canonicalId = null,
            )
            val userBooksQueryData = mockk<GetUserBooksQuery.Data>()
            val meEntry = mockk<GetUserBooksQuery.Data.Me>()
            val userBookEntry = mockk<GetUserBooksQuery.Data.Me.User_book>()
            val booksQueryData = mockk<GetBooksByIdsQuery.Data>()
            val canonicalBookEntry = mockk<GetBooksByIdsQuery.Data.Book>()

            mockkObject(GetBooksByIdsQuery.Data.Book.Companion)

            coEvery {
                apolloClient.safeQuery(
                    query = match<Query<GetUserBooksQuery.Data>> { it is GetUserBooksQuery },
                    fetchPolicy = any(),
                )
            } returns userBooksQueryData

            coEvery {
                apolloClient.safeQuery(
                    query = match<Query<GetBooksByIdsQuery.Data>> { it is GetBooksByIdsQuery },
                    fetchPolicy = any(),
                )
            } returns booksQueryData

            every {
                userBooksQueryData.me
            } returns listOf(meEntry)

            every {
                meEntry.user_books
            } returns listOf(userBookEntry)

            every {
                userBookEntry.toBook()
            } returns originalBook

            every {
                booksQueryData.books
            } returns listOf(canonicalBookEntry)

            every {
                with(GetBooksByIdsQuery.Data.Book.Companion) { canonicalBookEntry.bookDetailFragment() }
            } returns mockk {
                every {
                    toBook()
                } returns canonicalBook
            }

            val canonicalBookId = canonicalBook.id
            val canonicalBookTitle = canonicalBook.title
            val canonicalBookRating = canonicalBook.rating
            val canonicalBookDescription = canonicalBook.description
            val canonicalBookReleaseYear = canonicalBook.releaseYear
            val canonicalBookReleaseDate = canonicalBook.releaseDate
            val canonicalBookCoverUrl = canonicalBook.coverUrl
            val canonicalBookAuthors = canonicalBook.authors
            val canonicalBookUsersCount = canonicalBook.usersCount
            val canonicalBookReviewsCount = canonicalBook.ratingsCount
            val canonicalBookSeries = canonicalBook.bookSeries
            val canonicalBookPositionsInSeries = canonicalBook.positionsInSeries
            val canonicalBookIsCompilation = canonicalBook.isCompilation

            every {
                originalBook.copy(
                    id = canonicalBookId,
                    canonicalId = null,
                    title = canonicalBookTitle,
                    rating = canonicalBookRating,
                    description = canonicalBookDescription,
                    releaseYear = canonicalBookReleaseYear,
                    releaseDate = canonicalBookReleaseDate,
                    coverUrl = canonicalBookCoverUrl,
                    authors = canonicalBookAuthors,
                    usersCount = canonicalBookUsersCount,
                    ratingsCount = canonicalBookReviewsCount,
                    bookSeries = canonicalBookSeries,
                    positionsInSeries = canonicalBookPositionsInSeries,
                    isCompilation = canonicalBookIsCompilation,
                )
            } returns mergedBook

            // ----- Act -----
            val result = dataSource.initializeBooks(userId = userId)

            // ----- Assert -----
            result shouldBe listOf(mergedBook)
            coVerify(exactly = 1) {
                apolloClient.safeQuery(
                    query = match<Query<GetBooksByIdsQuery.Data>> { it is GetBooksByIdsQuery },
                    fetchPolicy = any(),
                )
            }
        }

        @Test
        fun `issues single batch GetBooksByIdsQuery call for multiple books with different canonical ids`() = runTest {
            // ----- Arrange -----
            val userId = 1
            val canonicalId1 = 20
            val canonicalId2 = 30
            val originalBook1 = stubBook(
                id = 10,
                canonicalId = canonicalId1,
            )
            val originalBook2 = stubBook(
                id = 11,
                canonicalId = canonicalId2,
            )
            val canonicalBook1 = stubCanonicalBook(id = canonicalId1)
            val canonicalBook2 = stubCanonicalBook(
                id = canonicalId2,
                title = "Other Canonical",
            )
            val mergedBook1 = stubBook(
                id = canonicalId1,
                canonicalId = null,
            )
            val mergedBook2 = stubBook(
                id = canonicalId2,
                canonicalId = null,
            )
            val userBooksQueryData = mockk<GetUserBooksQuery.Data>()
            val meEntry = mockk<GetUserBooksQuery.Data.Me>()
            val userBookEntry1 = mockk<GetUserBooksQuery.Data.Me.User_book>()
            val userBookEntry2 = mockk<GetUserBooksQuery.Data.Me.User_book>()
            val booksQueryData = mockk<GetBooksByIdsQuery.Data>()
            val canonicalBookEntry1 = mockk<GetBooksByIdsQuery.Data.Book>()
            val canonicalBookEntry2 = mockk<GetBooksByIdsQuery.Data.Book>()

            mockkObject(GetBooksByIdsQuery.Data.Book.Companion)

            coEvery {
                apolloClient.safeQuery(
                    query = match<Query<GetUserBooksQuery.Data>> { it is GetUserBooksQuery },
                    fetchPolicy = any(),
                )
            } returns userBooksQueryData

            coEvery {
                apolloClient.safeQuery(
                    query = match<Query<GetBooksByIdsQuery.Data>> { it is GetBooksByIdsQuery },
                    fetchPolicy = any(),
                )
            } returns booksQueryData

            every {
                userBooksQueryData.me
            } returns listOf(meEntry)

            every {
                meEntry.user_books
            } returns listOf(userBookEntry1, userBookEntry2)

            every {
                userBookEntry1.toBook()
            } returns originalBook1

            every {
                userBookEntry2.toBook()
            } returns originalBook2

            every {
                booksQueryData.books
            } returns listOf(canonicalBookEntry1, canonicalBookEntry2)

            every {
                with(GetBooksByIdsQuery.Data.Book.Companion) { canonicalBookEntry1.bookDetailFragment() }
            } returns mockk {
                every {
                    toBook()
                } returns canonicalBook1
            }

            every {
                with(GetBooksByIdsQuery.Data.Book.Companion) { canonicalBookEntry2.bookDetailFragment() }
            } returns mockk {
                every {
                    toBook()
                } returns canonicalBook2
            }

            val canonical1Id = canonicalBook1.id
            val canonical1Title = canonicalBook1.title
            val canonical1Rating = canonicalBook1.rating
            val canonical1Description = canonicalBook1.description
            val canonical1ReleaseYear = canonicalBook1.releaseYear
            val canonical1ReleaseDate = canonicalBook1.releaseDate
            val canonical1CoverUrl = canonicalBook1.coverUrl
            val canonical1Authors = canonicalBook1.authors
            val canonical1UsersCount = canonicalBook1.usersCount
            val canonical1ReviewsCount = canonicalBook1.ratingsCount
            val canonical1BookSeries = canonicalBook1.bookSeries
            val canonical1PositionsInSeries = canonicalBook1.positionsInSeries
            val canonical1IsCompilation = canonicalBook1.isCompilation

            val canonical2Id = canonicalBook2.id
            val canonical2Title = canonicalBook2.title
            val canonical2Rating = canonicalBook2.rating
            val canonical2Description = canonicalBook2.description
            val canonical2ReleaseYear = canonicalBook2.releaseYear
            val canonical2ReleaseDate = canonicalBook2.releaseDate
            val canonical2CoverUrl = canonicalBook2.coverUrl
            val canonical2Authors = canonicalBook2.authors
            val canonical2UsersCount = canonicalBook2.usersCount
            val canonical2ReviewsCount = canonicalBook2.ratingsCount
            val canonical2BookSeries = canonicalBook2.bookSeries
            val canonical2PositionsInSeries = canonicalBook2.positionsInSeries
            val canonical2IsCompilation = canonicalBook2.isCompilation

            every {
                originalBook1.copy(
                    id = canonical1Id,
                    canonicalId = null,
                    title = canonical1Title,
                    rating = canonical1Rating,
                    description = canonical1Description,
                    releaseYear = canonical1ReleaseYear,
                    releaseDate = canonical1ReleaseDate,
                    coverUrl = canonical1CoverUrl,
                    authors = canonical1Authors,
                    usersCount = canonical1UsersCount,
                    ratingsCount = canonical1ReviewsCount,
                    bookSeries = canonical1BookSeries,
                    positionsInSeries = canonical1PositionsInSeries,
                    isCompilation = canonical1IsCompilation,
                )
            } returns mergedBook1

            every {
                originalBook2.copy(
                    id = canonical2Id,
                    canonicalId = null,
                    title = canonical2Title,
                    rating = canonical2Rating,
                    description = canonical2Description,
                    releaseYear = canonical2ReleaseYear,
                    releaseDate = canonical2ReleaseDate,
                    coverUrl = canonical2CoverUrl,
                    authors = canonical2Authors,
                    usersCount = canonical2UsersCount,
                    ratingsCount = canonical2ReviewsCount,
                    bookSeries = canonical2BookSeries,
                    positionsInSeries = canonical2PositionsInSeries,
                    isCompilation = canonical2IsCompilation,
                )
            } returns mergedBook2

            // ----- Act -----
            val result = dataSource.initializeBooks(userId = userId)

            // ----- Assert -----
            result shouldBe listOf(mergedBook1, mergedBook2)
            coVerify(exactly = 1) {
                apolloClient.safeQuery(
                    query = match<Query<GetBooksByIdsQuery.Data>> { it is GetBooksByIdsQuery },
                    fetchPolicy = any(),
                )
            }
        }

        @Test
        fun `de-duplicates identical canonical ids into a single batch call`() = runTest {
            // ----- Arrange -----
            val userId = 1
            val sharedCanonicalId = 20
            val originalBook1 = stubBook(
                id = 10,
                canonicalId = sharedCanonicalId,
            )
            val originalBook2 = stubBook(
                id = 11,
                canonicalId = sharedCanonicalId,
            )
            val canonicalBook = stubCanonicalBook(id = sharedCanonicalId)
            val mergedBook1 = stubBook(
                id = sharedCanonicalId,
                canonicalId = null,
            )
            val mergedBook2 = stubBook(
                id = sharedCanonicalId,
                canonicalId = null,
            )
            val userBooksQueryData = mockk<GetUserBooksQuery.Data>()
            val meEntry = mockk<GetUserBooksQuery.Data.Me>()
            val userBookEntry1 = mockk<GetUserBooksQuery.Data.Me.User_book>()
            val userBookEntry2 = mockk<GetUserBooksQuery.Data.Me.User_book>()
            val booksQueryData = mockk<GetBooksByIdsQuery.Data>()
            val canonicalBookEntry = mockk<GetBooksByIdsQuery.Data.Book>()

            mockkObject(GetBooksByIdsQuery.Data.Book.Companion)

            coEvery {
                apolloClient.safeQuery(
                    query = match<Query<GetUserBooksQuery.Data>> { it is GetUserBooksQuery },
                    fetchPolicy = any(),
                )
            } returns userBooksQueryData

            coEvery {
                apolloClient.safeQuery(
                    query = match<Query<GetBooksByIdsQuery.Data>> { it is GetBooksByIdsQuery },
                    fetchPolicy = any(),
                )
            } returns booksQueryData

            every {
                userBooksQueryData.me
            } returns listOf(meEntry)

            every {
                meEntry.user_books
            } returns listOf(userBookEntry1, userBookEntry2)

            every {
                userBookEntry1.toBook()
            } returns originalBook1

            every {
                userBookEntry2.toBook()
            } returns originalBook2

            every {
                booksQueryData.books
            } returns listOf(canonicalBookEntry)

            every {
                with(GetBooksByIdsQuery.Data.Book.Companion) { canonicalBookEntry.bookDetailFragment() }
            } returns mockk {
                every {
                    toBook()
                } returns canonicalBook
            }

            val sharedId = canonicalBook.id
            val sharedTitle = canonicalBook.title
            val sharedRating = canonicalBook.rating
            val sharedDescription = canonicalBook.description
            val sharedReleaseYear = canonicalBook.releaseYear
            val sharedReleaseDate = canonicalBook.releaseDate
            val sharedCoverUrl = canonicalBook.coverUrl
            val sharedAuthors = canonicalBook.authors
            val sharedUsersCount = canonicalBook.usersCount
            val sharedReviewsCount = canonicalBook.ratingsCount
            val sharedBookSeries = canonicalBook.bookSeries
            val sharedPositionsInSeries = canonicalBook.positionsInSeries
            val sharedIsCompilation = canonicalBook.isCompilation

            every {
                originalBook1.copy(
                    id = sharedId,
                    canonicalId = null,
                    title = sharedTitle,
                    rating = sharedRating,
                    description = sharedDescription,
                    releaseYear = sharedReleaseYear,
                    releaseDate = sharedReleaseDate,
                    coverUrl = sharedCoverUrl,
                    authors = sharedAuthors,
                    usersCount = sharedUsersCount,
                    ratingsCount = sharedReviewsCount,
                    bookSeries = sharedBookSeries,
                    positionsInSeries = sharedPositionsInSeries,
                    isCompilation = sharedIsCompilation,
                )
            } returns mergedBook1

            every {
                originalBook2.copy(
                    id = sharedId,
                    canonicalId = null,
                    title = sharedTitle,
                    rating = sharedRating,
                    description = sharedDescription,
                    releaseYear = sharedReleaseYear,
                    releaseDate = sharedReleaseDate,
                    coverUrl = sharedCoverUrl,
                    authors = sharedAuthors,
                    usersCount = sharedUsersCount,
                    ratingsCount = sharedReviewsCount,
                    bookSeries = sharedBookSeries,
                    positionsInSeries = sharedPositionsInSeries,
                    isCompilation = sharedIsCompilation,
                )
            } returns mergedBook2

            // ----- Act -----
            dataSource.initializeBooks(userId = userId)

            // ----- Assert -----
            coVerify(exactly = 1) {
                apolloClient.safeQuery(
                    query = match<Query<GetBooksByIdsQuery.Data>> { it is GetBooksByIdsQuery },
                    fetchPolicy = any(),
                )
            }
        }

        @Test
        fun `only merges canonical books while passing through non-canonical books unchanged`() = runTest {
            // ----- Arrange -----
            val userId = 1
            val canonicalId = 20
            val originalCanonicalBook = stubBook(
                id = 10,
                canonicalId = canonicalId,
            )
            val nonCanonicalBook = stubBook(
                id = 11,
                canonicalId = null,
            )
            val canonicalBook = stubCanonicalBook(id = canonicalId)
            val mergedBook = stubBook(
                id = canonicalId,
                canonicalId = null,
            )
            val userBooksQueryData = mockk<GetUserBooksQuery.Data>()
            val meEntry = mockk<GetUserBooksQuery.Data.Me>()
            val canonicalUserBookEntry = mockk<GetUserBooksQuery.Data.Me.User_book>()
            val nonCanonicalUserBookEntry = mockk<GetUserBooksQuery.Data.Me.User_book>()
            val booksQueryData = mockk<GetBooksByIdsQuery.Data>()
            val canonicalBookEntry = mockk<GetBooksByIdsQuery.Data.Book>()

            mockkObject(GetBooksByIdsQuery.Data.Book.Companion)

            coEvery {
                apolloClient.safeQuery(
                    query = match<Query<GetUserBooksQuery.Data>> { it is GetUserBooksQuery },
                    fetchPolicy = any(),
                )
            } returns userBooksQueryData

            coEvery {
                apolloClient.safeQuery(
                    query = match<Query<GetBooksByIdsQuery.Data>> { it is GetBooksByIdsQuery },
                    fetchPolicy = any(),
                )
            } returns booksQueryData

            every {
                userBooksQueryData.me
            } returns listOf(meEntry)

            every {
                meEntry.user_books
            } returns listOf(canonicalUserBookEntry, nonCanonicalUserBookEntry)

            every {
                canonicalUserBookEntry.toBook()
            } returns originalCanonicalBook

            every {
                nonCanonicalUserBookEntry.toBook()
            } returns nonCanonicalBook

            every {
                booksQueryData.books
            } returns listOf(canonicalBookEntry)

            every {
                with(GetBooksByIdsQuery.Data.Book.Companion) { canonicalBookEntry.bookDetailFragment() }
            } returns mockk {
                every {
                    toBook()
                } returns canonicalBook
            }

            val mergeTargetId = canonicalBook.id
            val mergeTargetTitle = canonicalBook.title
            val mergeTargetRating = canonicalBook.rating
            val mergeTargetDescription = canonicalBook.description
            val mergeTargetReleaseYear = canonicalBook.releaseYear
            val mergeTargetReleaseDate = canonicalBook.releaseDate
            val mergeTargetCoverUrl = canonicalBook.coverUrl
            val mergeTargetAuthors = canonicalBook.authors
            val mergeTargetUsersCount = canonicalBook.usersCount
            val mergeTargetReviewsCount = canonicalBook.ratingsCount
            val mergeTargetBookSeries = canonicalBook.bookSeries
            val mergeTargetPositionsInSeries = canonicalBook.positionsInSeries
            val mergeTargetIsCompilation = canonicalBook.isCompilation

            every {
                originalCanonicalBook.copy(
                    id = mergeTargetId,
                    canonicalId = null,
                    title = mergeTargetTitle,
                    rating = mergeTargetRating,
                    description = mergeTargetDescription,
                    releaseYear = mergeTargetReleaseYear,
                    releaseDate = mergeTargetReleaseDate,
                    coverUrl = mergeTargetCoverUrl,
                    authors = mergeTargetAuthors,
                    usersCount = mergeTargetUsersCount,
                    ratingsCount = mergeTargetReviewsCount,
                    bookSeries = mergeTargetBookSeries,
                    positionsInSeries = mergeTargetPositionsInSeries,
                    isCompilation = mergeTargetIsCompilation,
                )
            } returns mergedBook

            // ----- Act -----
            val result = dataSource.initializeBooks(userId = userId)

            // ----- Assert -----
            result shouldBe listOf(mergedBook, nonCanonicalBook)
        }

        @Test
        fun `preserves pre-merge metadata when canonical id is not present in fetchBooksByIds response`() = runTest {
            // ----- Arrange -----
            val userId = 1
            val bookWithMissingCanonical = stubBook(
                id = 10,
                canonicalId = 20,
            )
            val userBooksQueryData = mockk<GetUserBooksQuery.Data>()
            val meEntry = mockk<GetUserBooksQuery.Data.Me>()
            val userBookEntry = mockk<GetUserBooksQuery.Data.Me.User_book>()
            val booksQueryData = mockk<GetBooksByIdsQuery.Data>()

            mockkObject(GetBooksByIdsQuery.Data.Book.Companion)

            coEvery {
                apolloClient.safeQuery(
                    query = match<Query<GetUserBooksQuery.Data>> { it is GetUserBooksQuery },
                    fetchPolicy = any(),
                )
            } returns userBooksQueryData

            coEvery {
                apolloClient.safeQuery(
                    query = match<Query<GetBooksByIdsQuery.Data>> { it is GetBooksByIdsQuery },
                    fetchPolicy = any(),
                )
            } returns booksQueryData

            every {
                userBooksQueryData.me
            } returns listOf(meEntry)

            every {
                meEntry.user_books
            } returns listOf(userBookEntry)

            every {
                userBookEntry.toBook()
            } returns bookWithMissingCanonical

            every {
                booksQueryData.books
            } returns emptyList()

            // ----- Act -----
            val result = dataSource.initializeBooks(userId = userId)

            // ----- Assert -----
            result shouldBe listOf(bookWithMissingCanonical)
        }

        @Test
        fun `withCanonicalMetadata copies ratingsCount from canonical book`() = runTest {
            // ----- Arrange -----
            val userId = 1
            val canonicalId = 20
            val originalBook = stubBook(
                id = 10,
                canonicalId = canonicalId,
            )
            val canonicalBook = stubCanonicalBook(
                id = canonicalId,
                ratingsCount = 999,
            )
            val mergedBook = stubBook(
                id = canonicalId,
                canonicalId = null,
            )
            val userBooksQueryData = mockk<GetUserBooksQuery.Data>()
            val meEntry = mockk<GetUserBooksQuery.Data.Me>()
            val userBookEntry = mockk<GetUserBooksQuery.Data.Me.User_book>()
            val booksQueryData = mockk<GetBooksByIdsQuery.Data>()
            val canonicalBookEntry = mockk<GetBooksByIdsQuery.Data.Book>()

            mockkObject(GetBooksByIdsQuery.Data.Book.Companion)

            coEvery {
                apolloClient.safeQuery(
                    query = match<Query<GetUserBooksQuery.Data>> { it is GetUserBooksQuery },
                    fetchPolicy = any(),
                )
            } returns userBooksQueryData

            coEvery {
                apolloClient.safeQuery(
                    query = match<Query<GetBooksByIdsQuery.Data>> { it is GetBooksByIdsQuery },
                    fetchPolicy = any(),
                )
            } returns booksQueryData

            every {
                userBooksQueryData.me
            } returns listOf(meEntry)

            every {
                meEntry.user_books
            } returns listOf(userBookEntry)

            every {
                userBookEntry.toBook()
            } returns originalBook

            every {
                booksQueryData.books
            } returns listOf(canonicalBookEntry)

            every {
                with(GetBooksByIdsQuery.Data.Book.Companion) { canonicalBookEntry.bookDetailFragment() }
            } returns mockk {
                every {
                    toBook()
                } returns canonicalBook
            }

            val canonicalBookId = canonicalBook.id
            val canonicalBookTitle = canonicalBook.title
            val canonicalBookRating = canonicalBook.rating
            val canonicalBookDescription = canonicalBook.description
            val canonicalBookReleaseYear = canonicalBook.releaseYear
            val canonicalBookReleaseDate = canonicalBook.releaseDate
            val canonicalBookCoverUrl = canonicalBook.coverUrl
            val canonicalBookAuthors = canonicalBook.authors
            val canonicalBookUsersCount = canonicalBook.usersCount
            val canonicalBookReviewsCount = canonicalBook.ratingsCount
            val canonicalBookSeries = canonicalBook.bookSeries
            val canonicalBookPositionsInSeries = canonicalBook.positionsInSeries
            val canonicalBookIsCompilation = canonicalBook.isCompilation

            every {
                originalBook.copy(
                    id = canonicalBookId,
                    canonicalId = null,
                    title = canonicalBookTitle,
                    rating = canonicalBookRating,
                    description = canonicalBookDescription,
                    releaseYear = canonicalBookReleaseYear,
                    releaseDate = canonicalBookReleaseDate,
                    coverUrl = canonicalBookCoverUrl,
                    authors = canonicalBookAuthors,
                    usersCount = canonicalBookUsersCount,
                    ratingsCount = canonicalBookReviewsCount,
                    bookSeries = canonicalBookSeries,
                    positionsInSeries = canonicalBookPositionsInSeries,
                    isCompilation = canonicalBookIsCompilation,
                )
            } returns mergedBook

            // ----- Act -----
            val result = dataSource.initializeBooks(userId = userId)

            // ----- Assert -----
            result shouldBe listOf(mergedBook)
            // The copy call above (which includes ratingsCount = 999) must be invoked:
            // if withCanonicalMetadata omitted ratingsCount the mock would not match and
            // copy(...) would return the wrong value, causing the assertion to fail.
            verify(exactly = 1) {
                originalBook.copy(
                    id = canonicalBookId,
                    canonicalId = null,
                    title = canonicalBookTitle,
                    rating = canonicalBookRating,
                    description = canonicalBookDescription,
                    releaseYear = canonicalBookReleaseYear,
                    releaseDate = canonicalBookReleaseDate,
                    coverUrl = canonicalBookCoverUrl,
                    authors = canonicalBookAuthors,
                    usersCount = canonicalBookUsersCount,
                    ratingsCount = canonicalBookReviewsCount,
                    bookSeries = canonicalBookSeries,
                    positionsInSeries = canonicalBookPositionsInSeries,
                    isCompilation = canonicalBookIsCompilation,
                )
            }
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
            val existingJournals = listOf(ReadingJournal(
                updatedAt = "2024-01-01T00:00:00.000000",
                event = "status_updated",
            ),)
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
        fun `returns updated book with currentSeconds set when newSeconds is provided`() = runTest {
            // ----- Arrange -----
            val newSeconds = 3600
            val existingJournals = listOf(ReadingJournal(
                updatedAt = "2024-01-01T00:00:00.000000",
                event = "status_updated",
            ),)
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
            val updatedUserBookReadMock = stubUserBookRead(
                id = 5,
                currentSeconds = newSeconds,
            )
            val expectedBook = stubBook(userBookRead = updatedUserBookReadMock)

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
                newSeconds = newSeconds,
            )

            // ----- Assert -----
            result.userBookRead?.currentSeconds shouldBe newSeconds
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
        fun `throws when mutation returns null insert_user_book user_book`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            val mutationData = mockk<MarkBookAsReadMutation.Data>()
            val insertUserBook = mockk<MarkBookAsReadMutation.Data.Insert_user_book>()

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
            } returns null

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.markBookAsRead(book = book)
            }
        }

        @Test
        fun `throws when toBook maps the user book fragment to null`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
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
            } returns null

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.markBookAsRead(book = book)
            }
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
        fun `sets edition_id to Optional Absent when editionId is null`() = runTest {
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
            dataSource.markBookAsRead(
                book = book,
                editionId = null,
            )

            // ----- Assert -----
            coVerify {
                apolloClient.safeMutation(
                    mutation = match<MarkBookAsReadMutation> {
                        it.userBookCreateInput.edition_id == Optional.Absent
                    },
                )
            }
        }
    }

    @Nested
    inner class UpdateBookEdition {
        @Test
        fun `returns mapped book when mutation succeeds`() = runTest {
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
            } returns mockk {
                every {
                    code
                } returns 1
            }

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
            } returns mockk {
                every {
                    code
                } returns 1
            }

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
        fun `throws when toBook maps the fragment to null`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook(id = 3)
            val newEditionId = 88
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
            } returns mockk {
                every {
                    code
                } returns 1
            }

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
            } returns null

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.updateBookEdition(
                    userBook = userBook,
                    newEditionId = newEditionId,
                )
            }
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
    inner class UpdateBookReview {
        @Test
        fun `returns mapped book when mutation succeeds`() = runTest {
            // ----- Arrange -----
            val userBookId = 7
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
        }

        @Test
        fun `issues UpdateUserBookReviewMutation with the correct user book id`() = runTest {
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
            dataSource.updateBookReview(
                userBook = userBook,
                review = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("Great read"))))),
                hasSpoilers = false,
                reviewedAt = "2026-05-30",
            )

            // ----- Assert -----
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

        @Test
        fun `throws when toBook maps the fragment to null`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook(id = 7)
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
        fun `isbn13 match — returns IsbnEditionMatch keyed by the requested isbn`() = runTest {
            // ----- Arrange -----
            val isbn = "9780451524935"
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
            } returns isbn
            every {
                editionRow.isbn_10
            } returns null

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
        fun `isbn10 match — returns IsbnEditionMatch keyed by the requested isbn`() = runTest {
            // ----- Arrange -----
            val isbn = "0451524934"
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
            } returns 20
            every {
                editionRow.id
            } returns 220
            every {
                editionRow.isbn_13
            } returns null
            every {
                editionRow.isbn_10
            } returns isbn

            // ----- Act -----
            val result = dataSource.fetchEditionMatchesForIsbns(isbns = listOf(isbn))

            // ----- Assert -----
            result shouldBe mapOf(
                isbn to IsbnEditionMatch(
                    bookId = 20,
                    editionId = 220,
                ),
            )
        }

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
