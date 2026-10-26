package nl.rhaydus.softcover.core.book.data.datasource

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.cache.normalized.FetchPolicy
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.CreateBookMutation
import nl.rhaydus.softcover.GetBookByIdQuery
import nl.rhaydus.softcover.GetBookByIdQuery.Data.Book.Companion.bookDetailFragment
import nl.rhaydus.softcover.GetBookIdByEditionIdQuery
import nl.rhaydus.softcover.GetBooksByIdsQuery
import nl.rhaydus.softcover.GetBooksByIdsQuery.Data.Book.Companion.bookDetailFragment as booksByIdsBookDetailFragment
import nl.rhaydus.softcover.GetEditionsByBookIdQuery
import nl.rhaydus.softcover.GetEditionsByBookIdQuery.Data.Edition.Companion.editionDetailFragment
import nl.rhaydus.softcover.GetEditionsByIdsQuery
import nl.rhaydus.softcover.GetEditionsByIdsQuery.Data.Edition.Companion.editionDetailFragment as editionsByIdsDetailFragment
import nl.rhaydus.softcover.GetEditionsByIsbnsQuery
import nl.rhaydus.softcover.GetLatestUnfinishedReadQuery
import nl.rhaydus.softcover.GetRecentReleaseIdsQuery
import nl.rhaydus.softcover.GetSimilarBooksQuery
import nl.rhaydus.softcover.GetSystemVibeBookIdsQuery
import nl.rhaydus.softcover.GetTrendingBookIdsQuery
import nl.rhaydus.softcover.GetUpcomingReleaseIdsQuery
import nl.rhaydus.softcover.GetUserBookIdsByIdsQuery
import nl.rhaydus.softcover.GetUserBooksQuery
import nl.rhaydus.softcover.GetUserBooksQuery.Data.Me.User_book.Companion.userBookFragment
import nl.rhaydus.softcover.InsertUserBookWithStatusMutation
import nl.rhaydus.softcover.InsertUserBookWithStatusMutation.Data.Insert_user_book.User_book.Companion.userBookFragment as insertUserBookWithStatusFragment
import nl.rhaydus.softcover.MarkBookAsReadMutation
import nl.rhaydus.softcover.MarkBookAsReadMutation.Data.Insert_user_book.User_book.Companion.userBookFragment
import nl.rhaydus.softcover.MarkBookAsWantToReadMutation
import nl.rhaydus.softcover.MarkBookAsWantToReadMutation.Data.Insert_user_book.User_book.Companion.userBookFragment
import nl.rhaydus.softcover.RemoveUserBookMutation
import nl.rhaydus.softcover.UpdateBookEditionMutation
import nl.rhaydus.softcover.UpdateBookEditionMutation.Data.Update_user_book.User_book.Companion.userBookFragment
import nl.rhaydus.softcover.UpdateReadingProgressMutation
import nl.rhaydus.softcover.UpdateReadingProgressMutation.Data.Update_user_book_read.User_book_read.User_book.Companion.userBookFragment
import nl.rhaydus.softcover.UpdateUserBookRatingMutation
import nl.rhaydus.softcover.UpdateUserBookRatingMutation.Data.Update_user_book.User_book.Companion.userBookFragment as updateUserBookRatingUserBookFragment
import nl.rhaydus.softcover.UpdateUserBookReviewMutation
import nl.rhaydus.softcover.UpdateUserBookReviewMutation.Data.Update_user_book.User_book.Companion.userBookFragment as updateUserBookReviewUserBookFragment
import nl.rhaydus.softcover.UpdateUserBookStatusMutation
import nl.rhaydus.softcover.UpdateUserBookStatusMutation.Data.Update_user_book.User_book.Companion.userBookFragment
import nl.rhaydus.softcover.core.book.data.mapper.parseCachedBookIds
import nl.rhaydus.softcover.core.book.data.mapper.toBook
import nl.rhaydus.softcover.core.book.data.mapper.toBookEdition
import nl.rhaydus.softcover.core.book.data.mapper.toDatePeriod
import nl.rhaydus.softcover.core.book.data.mapper.toMonths
import nl.rhaydus.softcover.core.book.data.mapper.toTrendingDuration
import nl.rhaydus.softcover.core.book.data.mapper.toUserTag
import nl.rhaydus.softcover.core.book.data.mapper.toVibeSlug
import nl.rhaydus.softcover.core.book.data.mapper.toVibeType
import nl.rhaydus.softcover.core.book.domain.model.BookImport
import nl.rhaydus.softcover.core.book.domain.model.IsbnEditionMatch
import nl.rhaydus.softcover.core.book.domain.model.SimilarBookCandidate
import nl.rhaydus.softcover.core.book.domain.model.SimilarBookSeed
import nl.rhaydus.softcover.core.book.domain.model.UnfinishedRead
import nl.rhaydus.softcover.core.book.domain.model.UserBooksSnapshot
import nl.rhaydus.softcover.core.database.mapper.reviewSlateFromDocument
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.JournalEventType
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.core.domain.model.RecentWindow
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.SystemVibe
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.core.domain.model.UpcomingWindow
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.core.domain.util.localDateOf
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.core.network.helper.CountedPage
import nl.rhaydus.softcover.core.network.helper.fetchAllPagesCounted
import nl.rhaydus.softcover.core.network.helper.safeMutation
import nl.rhaydus.softcover.core.network.helper.safeQuery
import nl.rhaydus.softcover.fragment.BookListFragment.Book_series.Companion.bookSeriesFragment
import nl.rhaydus.softcover.type.DatesReadInput
import nl.rhaydus.softcover.type.UserBookCreateInput
import nl.rhaydus.softcover.type.UserBookUpdateInput
import kotlin.time.Clock

interface BooksRemoteDataSource {
    suspend fun fetchBookById(id: Int): Book

    suspend fun fetchBookIdForEdition(editionId: Int): Int?

    suspend fun fetchLatestUnfinishedRead(userBookId: Int): UnfinishedRead?

    suspend fun fetchEditionMatchesForIsbns(isbns: List<String>): Map<String, IsbnEditionMatch>

    suspend fun addBookByIsbn(isbn: String): BookImport

    suspend fun fetchBooksByIds(
        ids: List<Int>,
        forceNetwork: Boolean = false,
    ): List<Book>

    suspend fun fetchSimilarBookSeed(bookId: Int): SimilarBookSeed

    suspend fun fetchSimilarCandidates(ids: List<Int>): List<SimilarBookCandidate>

    suspend fun fetchTrendingBookIds(period: TrendingPeriod): List<Int>

    /** Ids of the [limit] most-shelved books whose release date falls within [window], most-wanted first. */
    suspend fun fetchUpcomingBookIds(
        window: UpcomingWindow,
        limit: Int,
    ): List<Int>

    /** Ids of the [limit] most-shelved books whose release date falls within the past [window], most-shelved first. */
    suspend fun fetchRecentBookIds(
        window: RecentWindow,
        limit: Int,
    ): List<Int>

    /**
     * Ids of up to [limit] books from the reader's [vibe], best match first, or `null` when
     * Hardcover has not generated one yet. Throws when the vibe's id list is malformed.
     */
    suspend fun fetchSystemVibeBookIds(
        vibe: SystemVibe,
        limit: Int,
    ): List<Int>?

    suspend fun markBookAsWantToRead(
        bookId: Int,
        editionId: Int? = null,
    ): Book

    suspend fun markBookAsReading(
        book: Book,
        editionId: Int? = null,
    ): Book

    suspend fun markBookAsPaused(
        book: Book,
        editionId: Int? = null,
    ): Book

    suspend fun updateBookRating(
        userBook: UserBook,
        rating: Double,
    ): Book

    suspend fun updateBookReview(
        userBook: UserBook,
        review: ReviewDocument,
        hasSpoilers: Boolean,
        reviewedAt: String,
    ): Book

    suspend fun removeBookFromLibrary(book: Book)

    suspend fun initializeBooks(
        userId: Int,
        statusIds: Set<Int>? = null,
        userBookIds: Set<Int> = emptySet(),
    ): UserBooksSnapshot

    /** Returns the subset of [ids] the signed-in user still has as user books; throws if the lookup fails. */
    suspend fun fetchExistingUserBookIds(ids: List<Int>): Set<Int>

    suspend fun getEditionsByBookId(
        bookId: Int,
        forceNetwork: Boolean = false,
    ): List<BookEdition>

    suspend fun fetchEditionsByIds(
        ids: List<Int>,
        forceNetwork: Boolean = false,
    ): List<BookEdition>

    suspend fun updateBookProgress(
        book: Book,
        newPage: Int? = null,
        newSeconds: Int? = null,
        actionAt: String? = null,
    ): Book

    suspend fun markBookAsRead(
        book: Book,
        editionId: Int? = null,
        actionAt: String? = null,
    ): Book

    suspend fun updateBookEdition(
        userBook: UserBook,
        newEditionId: Int,
    ): Book

    suspend fun replayUpdateBookProgress(
        userBookReadId: Int,
        editionId: Int?,
        progressPages: Int?,
        progressSeconds: Int?,
        startedAt: String?,
        finishedAt: String?,
        actionAt: String? = null,
    )

    suspend fun replayMarkBookAsRead(
        bookId: Int,
        editionId: Int?,
        userDate: String,
        actionAt: String? = null,
    )

    suspend fun replayUpdateUserBookStatus(
        userBookId: Int,
        statusCode: Int,
        userDate: String,
    )

    suspend fun replayUpdateBookRating(
        userBookId: Int,
        rating: Double,
    )

    suspend fun replayUpdateBookReview(
        userBookId: Int,
        review: ReviewDocument,
        hasSpoilers: Boolean,
        reviewedAt: String,
    )
}

private const val BATCH_ID_LIMIT: Int = 200

// Request-multiplier budget for the library fetch: the page count is the server's row count divided by
// this. The page is small because each row carries a deep selection and the server's row cap for it is
// unverified.
private const val USER_BOOKS_PAGE_SIZE: Int = 50

/** Hardcover's ISBN external-book provider platform — `upsert_book` uses the ISBN as the external id. */
private const val HARDCOVER_ISBN_PLATFORM_ID: Int = 8

/** `upsert_book` status while Hardcover imports the book asynchronously from its ISBN source. */
private const val UPSERT_BOOK_STATUS_FETCHING: String = "fetching"

internal class BooksRemoteDataSourceImpl(
    private val apolloClient: ApolloClient,
    private val appDispatchers: AppDispatchers,
    private val getUserIdUseCase: GetUserIdUseCase,
) : BooksRemoteDataSource {
    override suspend fun fetchBookById(id: Int): Book {
        val book = fetchBookByIdRaw(id)

        val canonicalId = book.canonicalId

        if (canonicalId != null && canonicalId != book.id) {
            AppLog.i("Book $id has canonical $canonicalId; refetching canonical.")
            return fetchBookByIdRaw(canonicalId).copy(canonicalId = null)
        }

        return book
    }

    private suspend fun fetchBookByIdRaw(id: Int): Book {
        val result = apolloClient.safeQuery(
            query = GetBookByIdQuery(id = id),
            fetchPolicy = FetchPolicy.CacheFirst,
        )

        val raw = result.books.firstOrNull() ?: throw BookNotFoundException(bookId = id)

        return raw.bookDetailFragment()?.toBook()
            ?: throw Exception("Book $id could not be mapped")
    }

    override suspend fun fetchBookIdForEdition(editionId: Int): Int? {
        val result = apolloClient.safeQuery(
            query = GetBookIdByEditionIdQuery(editionId = editionId),
            fetchPolicy = FetchPolicy.NetworkFirst,
        )

        return result.editions.firstOrNull()?.book_id
    }

    override suspend fun fetchLatestUnfinishedRead(userBookId: Int): UnfinishedRead? {
        val result = apolloClient.safeQuery(
            query = GetLatestUnfinishedReadQuery(userBookId = userBookId),
            fetchPolicy = FetchPolicy.NetworkFirst,
        )

        return result.user_book_reads.firstOrNull()?.let { read ->
            UnfinishedRead(
                id = read.id,
                startedAt = read.started_at,
            )
        }
    }

    override suspend fun fetchEditionMatchesForIsbns(isbns: List<String>): Map<String, IsbnEditionMatch> {
        if (isbns.isEmpty()) return emptyMap()

        val result = apolloClient.safeQuery(
            query = GetEditionsByIsbnsQuery(isbns = isbns),
            fetchPolicy = FetchPolicy.NetworkFirst,
        )

        val requested = isbns.toSet()
        val matches = mutableMapOf<String, IsbnEditionMatch>()

        // isbn_13 matches take precedence, so they are folded in before isbn_10 matches fill any gaps.
        result.editions.forEach { edition ->
            val isbn13 = edition.isbn_13
            if (isbn13 != null && isbn13 in requested && isbn13 !in matches) {
                matches[isbn13] = IsbnEditionMatch(
                    bookId = edition.book_id,
                    editionId = edition.id,
                )
            }
        }

        result.editions.forEach { edition ->
            val isbn10 = edition.isbn_10
            if (isbn10 != null && isbn10 in requested && isbn10 !in matches) {
                matches[isbn10] = IsbnEditionMatch(
                    bookId = edition.book_id,
                    editionId = edition.id,
                )
            }
        }

        return matches
    }

    override suspend fun addBookByIsbn(isbn: String): BookImport {
        val response = apolloClient
            .safeMutation(
                mutation = CreateBookMutation(
                    externalId = isbn,
                    platformId = HARDCOVER_ISBN_PLATFORM_ID,
                    bookId = Optional.absent(),
                ),
            )
            .upsert_book
            ?: throw Exception("Did not receive an upsert-book response")

        val errors: List<String> = response.errors
            .orEmpty()
            .filterNotNull()
            .filter { it.isNotBlank() }

        if (errors.isNotEmpty()) throw Exception("CreateBook rejected by server: ${errors.joinToString()}")

        // upsert_book's top-level id and edition_id are the book's and the edition's ids.
        val bookId = response.book?.id ?: response.id

        if (bookId == null) {
            if (response.status == UPSERT_BOOK_STATUS_FETCHING) return BookImport.Fetching

            throw Exception(
                "upsert_book returned no book (status=${response.status}, id=${response.id}, " +
                    "edition_id=${response.edition_id})",
            )
        }

        return BookImport.Created(
            bookId = bookId,
            editionId = response.edition?.id ?: response.edition_id,
        )
    }

    override suspend fun fetchBooksByIds(
        ids: List<Int>,
        forceNetwork: Boolean,
    ): List<Book> {
        if (ids.isEmpty()) return emptyList()

        val policy = if (forceNetwork) FetchPolicy.NetworkFirst else FetchPolicy.CacheFirst

        return ids.chunked(BATCH_ID_LIMIT).flatMap { chunk ->
            val result = apolloClient.safeQuery(
                query = GetBooksByIdsQuery(ids = chunk),
                fetchPolicy = policy,
            )

            result.books.mapNotNull { it.booksByIdsBookDetailFragment()?.toBook() }
        }
    }

    override suspend fun fetchSimilarBookSeed(bookId: Int): SimilarBookSeed {
        val result = apolloClient.safeQuery(
            query = GetSimilarBooksQuery(bookId = bookId),
            fetchPolicy = FetchPolicy.CacheFirst,
        )

        val raw = result.books.firstOrNull() ?: throw BookNotFoundException(bookId = bookId)

        val sourceSeries = raw.book_series.mapNotNull { it.series }

        return SimilarBookSeed(
            sourceBookId = bookId,
            sourceCanonicalId = raw.canonical?.id,
            rankedIds = raw.cached_similar_book_ids.toBookIds(),
            excludedSeriesBookIds = sourceSeries.flatMap { series -> series.book_series.map { it.book_id } }.toSet(),
            sourceSeriesIds = sourceSeries.map { it.id }.toSet(),
        )
    }

    // The series ids come from the raw rows because Book.bookSeries keeps only the first series.
    override suspend fun fetchSimilarCandidates(ids: List<Int>): List<SimilarBookCandidate> {
        if (ids.isEmpty()) return emptyList()

        return ids.chunked(BATCH_ID_LIMIT).flatMap { chunk ->
            val result = apolloClient.safeQuery(
                query = GetBooksByIdsQuery(ids = chunk),
                fetchPolicy = FetchPolicy.CacheFirst,
            )

            result.books.mapNotNull { raw ->
                val detail = raw.booksByIdsBookDetailFragment() ?: return@mapNotNull null
                val book = detail.toBook() ?: return@mapNotNull null

                SimilarBookCandidate(
                    book = book,
                    seriesIds = detail.book_series
                        .mapNotNull { it.bookSeriesFragment()?.series?.id }
                        .toSet(),
                )
            }
        }
    }

    override suspend fun fetchTrendingBookIds(period: TrendingPeriod): List<Int> = apolloClient
        .safeQuery(query = GetTrendingBookIdsQuery(duration = period.toTrendingDuration()))
        .books_trending
        ?.ids
        ?.mapNotNull { it }
        .orEmpty()

    override suspend fun fetchUpcomingBookIds(
        window: UpcomingWindow,
        limit: Int,
    ): List<Int> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        return apolloClient
            .safeQuery(
                query = GetUpcomingReleaseIdsQuery(
                    today = today.toString(),
                    maxDate = today.plus(DatePeriod(months = window.toMonths())).toString(),
                    limit = limit,
                ),
            )
            .books
            .map { it.id }
    }

    override suspend fun fetchRecentBookIds(
        window: RecentWindow,
        limit: Int,
    ): List<Int> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        return apolloClient
            .safeQuery(
                query = GetRecentReleaseIdsQuery(
                    minDate = today.minus(window.toDatePeriod()).toString(),
                    today = today.toString(),
                    limit = limit,
                ),
            )
            .books
            .map { it.id }
    }

    override suspend fun fetchSystemVibeBookIds(
        vibe: SystemVibe,
        limit: Int,
    ): List<Int>? {
        val userId = getUserIdUseCase().getOrThrow()

        val row = apolloClient
            .safeQuery(
                query = GetSystemVibeBookIdsQuery(
                    userId = userId,
                    vibeType = vibe.toVibeType(),
                    slug = vibe.toVibeSlug(),
                ),
            )
            .vibes
            .firstOrNull()
            ?: return null

        return parseCachedBookIds(
            cachedBookIds = row.cached_book_ids,
            limit = limit,
        )
    }

    override suspend fun getEditionsByBookId(
        bookId: Int,
        forceNetwork: Boolean,
    ): List<BookEdition> {
        val policy = if (forceNetwork) FetchPolicy.NetworkFirst else FetchPolicy.CacheFirst

        val result = apolloClient.safeQuery(
            query = GetEditionsByBookIdQuery(bookId = bookId),
            fetchPolicy = policy,
        )

        return result.editions.mapNotNull { it.editionDetailFragment()?.toBookEdition() }
    }

    override suspend fun fetchEditionsByIds(
        ids: List<Int>,
        forceNetwork: Boolean,
    ): List<BookEdition> {
        if (ids.isEmpty()) return emptyList()

        val policy = if (forceNetwork) FetchPolicy.NetworkFirst else FetchPolicy.CacheFirst

        return ids.chunked(BATCH_ID_LIMIT).flatMap { chunk ->
            val result = apolloClient.safeQuery(
                query = GetEditionsByIdsQuery(ids = chunk),
                fetchPolicy = policy,
            )

            result.editions.mapNotNull { it.editionsByIdsDetailFragment()?.toBookEdition() }
        }
    }

    override suspend fun markBookAsWantToRead(
        bookId: Int,
        editionId: Int?,
    ): Book {
        val userBookCreateInput = UserBookCreateInput(
            book_id = bookId,
            edition_id = Optional.presentIfNotNull(editionId),
            status_id = Optional.Present(UserBookStatus.WANT_TO_READ.code),
            privacy_setting_id = Optional.Present(PrivacySetting.PUBLIC.code),
        )

        return apolloClient
            .safeMutation(mutation = MarkBookAsWantToReadMutation(`object` = userBookCreateInput))
            .insert_user_book
            ?.user_book
            ?.userBookFragment()
            ?.toBook() ?: throw Exception("Book could not be mapped")
    }

    override suspend fun markBookAsReading(
        book: Book,
        editionId: Int?,
    ): Book = setStatusOrCreate(
        book = book,
        status = UserBookStatus.CURRENTLY_READING,
        editionId = editionId,
    )

    override suspend fun markBookAsPaused(
        book: Book,
        editionId: Int?,
    ): Book = setStatusOrCreate(
        book = book,
        status = UserBookStatus.PAUSED,
        editionId = editionId,
    )

    private suspend fun setStatusOrCreate(
        book: Book,
        status: UserBookStatus,
        editionId: Int?,
    ): Book {
        if (book.userBook == null) {
            return createUserBookWithStatus(
                book = book,
                status = status,
                editionId = editionId,
            )
        }

        return updateUserBookStatus(
            book = book,
            status = status,
            editionId = editionId,
        )
    }

    private suspend fun updateUserBookStatus(
        book: Book,
        status: UserBookStatus,
        editionId: Int?,
    ): Book {
        val userBook = book.userBook ?: throw Exception("User did not have a user book")

        val currentDate = Clock.System
            .todayIn(TimeZone.currentSystemDefault())
            .toString()

        val input = UserBookUpdateInput(
            edition_id = Optional.presentIfNotNull(editionId),
            review_has_spoilers = Optional.Present(userBook.reviewHasSpoilers),
            status_id = Optional.present(status.code),
            last_read_date = Optional.Present(userBook.lastReadDate),
            rating = Optional.Present(userBook.rating),
            privacy_setting_id = Optional.Present(PrivacySetting.PUBLIC.code),
            referrer_user_id = Optional.Present(userBook.referrerUserId),
            reviewed_at = Optional.Present(userBook.reviewedAt),
            date_added = Optional.Present(userBook.dateAdded),
            user_date = Optional.present(currentDate),
        )

        return apolloClient
            .safeMutation(
                mutation = UpdateUserBookStatusMutation(
                    id = userBook.id,
                    `object` = input,
                ),
            )
            .update_user_book
            ?.user_book
            ?.userBookFragment()
            ?.toBook() ?: throw Exception("Book could not be mapped")
    }

    private suspend fun createUserBookWithStatus(
        book: Book,
        status: UserBookStatus,
        editionId: Int?,
    ): Book {
        val currentDate = Clock.System
            .todayIn(TimeZone.currentSystemDefault())
            .toString()

        val dataObject = UserBookCreateInput(
            book_id = book.id,
            edition_id = Optional.presentIfNotNull(editionId),
            status_id = Optional.present(status.code),
            user_date = Optional.present(currentDate),
            privacy_setting_id = Optional.present(PrivacySetting.PUBLIC.code),
        )

        return apolloClient
            .safeMutation(
                mutation = InsertUserBookWithStatusMutation(userBookCreateInput = dataObject),
            )
            .insert_user_book
            ?.user_book
            ?.insertUserBookWithStatusFragment()
            ?.toBook() ?: throw Exception("Book could not be mapped")
    }

    override suspend fun updateBookRating(
        userBook: UserBook,
        rating: Double,
    ): Book {
        val input = UserBookUpdateInput(
            rating = Optional.Present(rating),
        )

        return apolloClient
            .safeMutation(
                mutation = UpdateUserBookRatingMutation(
                    id = userBook.id,
                    `object` = input,
                ),
            )
            .update_user_book
            ?.user_book
            ?.updateUserBookRatingUserBookFragment()
            ?.toBook() ?: throw Exception("Book could not be mapped")
    }

    override suspend fun updateBookReview(
        userBook: UserBook,
        review: ReviewDocument,
        hasSpoilers: Boolean,
        reviewedAt: String,
    ): Book {
        val input = UserBookUpdateInput(
            review_slate = Optional.Present(reviewSlateFromDocument(document = review)),
            review_has_spoilers = Optional.Present(hasSpoilers),
            reviewed_at = Optional.Present(reviewedAt),
        )

        return apolloClient
            .safeMutation(
                mutation = UpdateUserBookReviewMutation(
                    id = userBook.id,
                    `object` = input,
                ),
            )
            .update_user_book
            ?.user_book
            ?.updateUserBookReviewUserBookFragment()
            ?.toBook() ?: throw Exception("Book could not be mapped")
    }

    override suspend fun removeBookFromLibrary(book: Book) {
        val userBookId = book.userBook?.id
            ?: throw Exception("User did not have a user book")

        apolloClient.safeMutation(mutation = RemoveUserBookMutation(id = userBookId))
    }

    override suspend fun initializeBooks(
        userId: Int,
        statusIds: Set<Int>?,
        userBookIds: Set<Int>,
    ): UserBooksSnapshot = withContext(appDispatchers.io) {
        val userBooks = fetchAllPagesCounted(
            pageSize = USER_BOOKS_PAGE_SIZE,
            key = { it.id },
        ) { limit, offset ->
            val result = apolloClient.safeQuery(
                query = GetUserBooksQuery(
                    userId = userId,
                    statusIds = Optional.presentIfNotNull(statusIds?.toList()),
                    userBookIds = Optional.present(userBookIds.toList()),
                    limit = limit,
                    offset = offset,
                ),
            )

            // A missing `me` must fail the fetch: an empty page would end the loop and read as complete.
            val me = result.me.firstOrNull() ?: throw Exception("No books were found")

            CountedPage(
                rows = me.user_books,
                total = me.total.aggregate?.count ?: 0,
            )
        }

        val mapped: List<Pair<Book, GetUserBooksQuery.Data.Me.User_book>> = userBooks.mapNotNull { userBook ->
            userBook.userBookFragment()?.toBook()?.let { it to userBook }
        }

        val merged: List<Book> = resolveCanonicalMerges(books = mapped.map { it.first })

        UserBooksSnapshot(
            books = merged,
            userTagsByBookId = merged
                .zip(mapped)
                .associate { (mergedBook, original) ->
                    val (originalBook, userBook) = original

                    mergedBook.id to userBook.userTagsFor(wasMerged = mergedBook.id != originalBook.id)
                },
        )
    }

    // A merged book shows the canonical book's tags, which is where the user's tagging lives after a merge.
    private fun GetUserBooksQuery.Data.Me.User_book.userTagsFor(wasMerged: Boolean): List<UserTag> {
        val canonical = book.canonical

        return if (wasMerged && canonical != null) {
            canonical.taggings.map { it.toUserTag(tag = it.tag) }
        } else {
            book.taggings.map { it.toUserTag(tag = it.tag) }
        }
    }

    override suspend fun fetchExistingUserBookIds(ids: List<Int>): Set<Int> =
        withContext(appDispatchers.io) {
            ids.chunked(BATCH_ID_LIMIT).flatMapTo(mutableSetOf()) { chunk ->
                val result = apolloClient.safeQuery(query = GetUserBookIdsByIdsQuery(ids = chunk))

                val userBooks = result.me.firstOrNull()?.user_books
                    ?: throw Exception("No user was found")

                userBooks.map { it.id }
            }
        }

    private suspend fun resolveCanonicalMerges(books: List<Book>): List<Book> {
        val canonicalIds = books.mapNotNull { it.canonicalId }.distinct()

        if (canonicalIds.isEmpty()) return books

        AppLog.i("Resolving ${canonicalIds.size} canonical merge(s): $canonicalIds")

        val canonicalBooks = fetchBooksByIds(ids = canonicalIds).associateBy { it.id }

        return books.map { book ->
            val canonicalId = book.canonicalId ?: return@map book

            val canonical = canonicalBooks[canonicalId]

            if (canonical == null) {
                AppLog.w("Canonical $canonicalId for book ${book.id} not returned; keeping pre-merge metadata.")
                return@map book
            }
            book.withCanonicalMetadata(canonical = canonical)
        }
    }

    private fun Book.withCanonicalMetadata(canonical: Book): Book = copy(
        id = canonical.id,
        canonicalId = null,
        title = canonical.title,
        rating = canonical.rating,
        description = canonical.description,
        releaseYear = canonical.releaseYear,
        releaseDate = canonical.releaseDate,
        coverUrl = canonical.coverUrl,
        authors = canonical.authors,
        nonPrimaryAuthorIds = canonical.nonPrimaryAuthorIds,
        usersCount = canonical.usersCount,
        ratingsCount = canonical.ratingsCount,
        bookSeries = canonical.bookSeries,
        positionsInSeries = canonical.positionsInSeries,
        isCompilation = canonical.isCompilation,
    )

    override suspend fun updateBookProgress(
        book: Book,
        newPage: Int?,
        newSeconds: Int?,
        actionAt: String?,
    ): Book {
        val userBook = book.userBook
            ?: throw Exception("Book did not contain a user book")

        val userBookRead =
            book.currentRead ?: throw Exception("Book did not contain a user book read")

        val isAudiobook = newSeconds != null

        val dataObject = DatesReadInput(
            progress_pages = if (isAudiobook) Optional.absent() else Optional.present(newPage),
            progress_seconds = if (isAudiobook) Optional.present(newSeconds) else Optional.absent(),
            started_at = Optional.present(userBookRead.startedAt),
            finished_at = Optional.present(userBookRead.finishedAt),
            edition_id = Optional.present(userBook.editionId),
            action_at = Optional.presentIfNotNull(actionAt),
            action = if (actionAt != null) {
                Optional.present(JournalEventType.ProgressUpdated.eventName)
            } else {
                Optional.absent()
            },
        )

        val mutation = UpdateReadingProgressMutation(
            id = userBookRead.id,
            datesReadInput = dataObject,
        )

        val userBookFragment = apolloClient
            .safeMutation(mutation = mutation)
            .update_user_book_read
            ?.user_book_read
            ?.user_book
            ?.userBookFragment()
            ?: throw Exception("Did not receive a new user book fragment")

        return userBookFragment.toBook() ?: throw Exception("Book could not be mapped")
    }

    override suspend fun markBookAsRead(
        book: Book,
        editionId: Int?,
        actionAt: String?,
    ): Book {
        val userDate = actionAt
            ?.let { localDateOf(actionAt = it) }
            ?: Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()

        val dataObject = UserBookCreateInput(
            book_id = book.id,
            edition_id = Optional.presentIfNotNull(editionId),
            status_id = Optional.present(UserBookStatus.READ.code),
            user_date = Optional.present(userDate),
            action_at = Optional.presentIfNotNull(actionAt),
            privacy_setting_id = Optional.present(PrivacySetting.PUBLIC.code),
        )

        val mutation = MarkBookAsReadMutation(userBookCreateInput = dataObject)

        val userBookFragment = apolloClient
            .safeMutation(mutation = mutation)
            .insert_user_book
            ?.user_book
            ?.userBookFragment()
            ?: throw Exception("Did not receive a new user book fragment")

        return userBookFragment.toBook() ?: throw Exception("Book was not mapped successfully")
    }

    override suspend fun updateBookEdition(
        userBook: UserBook,
        newEditionId: Int,
    ): Book {
        val mutation = UpdateBookEditionMutation(
            id = userBook.id,
            `object` = UserBookUpdateInput(
                date_added = Optional.Present(userBook.dateAdded),
                edition_id = Optional.present(newEditionId),
                last_read_date = Optional.Present(userBook.lastReadDate),
                privacy_setting_id = Optional.Present(PrivacySetting.PUBLIC.code),
                rating = Optional.Present(userBook.rating),
                referrer_user_id = Optional.Present(userBook.referrerUserId),
                review_has_spoilers = Optional.Present(userBook.reviewHasSpoilers),
                status_id = when (userBook.status) {
                    BookStatus.None -> Optional.Absent
                    else -> Optional.present(userBook.status.code)
                },
                reviewed_at = Optional.Present(userBook.reviewedAt),
            ),
        )

        val userBookFragment = apolloClient
            .safeMutation(mutation = mutation)
            .update_user_book
            ?.user_book
            ?.userBookFragment()
            ?: throw Exception("Did not receive a new user book fragment")

        return userBookFragment.toBook() ?: throw Exception("Book was not mapped successfully")
    }

    override suspend fun replayUpdateBookProgress(
        userBookReadId: Int,
        editionId: Int?,
        progressPages: Int?,
        progressSeconds: Int?,
        startedAt: String?,
        finishedAt: String?,
        actionAt: String?,
    ) {
        val isAudiobook = progressSeconds != null

        val dataObject = DatesReadInput(
            progress_pages = if (isAudiobook) Optional.absent() else Optional.present(progressPages),
            progress_seconds = if (isAudiobook) Optional.present(progressSeconds) else Optional.absent(),
            started_at = Optional.present(startedAt),
            finished_at = Optional.present(finishedAt),
            edition_id = Optional.present(editionId),
            action_at = Optional.presentIfNotNull(actionAt),
            action = if (actionAt != null) {
                Optional.present(JournalEventType.ProgressUpdated.eventName)
            } else {
                Optional.absent()
            },
        )

        apolloClient.safeMutation(
            mutation = UpdateReadingProgressMutation(
                id = userBookReadId,
                datesReadInput = dataObject,
            ),
        )
    }

    override suspend fun replayMarkBookAsRead(
        bookId: Int,
        editionId: Int?,
        userDate: String,
        actionAt: String?,
    ) {
        val resolvedUserDate = actionAt?.let { localDateOf(actionAt = it) } ?: userDate

        val dataObject = UserBookCreateInput(
            book_id = bookId,
            edition_id = Optional.presentIfNotNull(editionId),
            status_id = Optional.present(UserBookStatus.READ.code),
            user_date = Optional.present(resolvedUserDate),
            action_at = Optional.presentIfNotNull(actionAt),
            privacy_setting_id = Optional.present(PrivacySetting.PUBLIC.code),
        )

        apolloClient.safeMutation(mutation = MarkBookAsReadMutation(userBookCreateInput = dataObject))
    }

    override suspend fun replayUpdateUserBookStatus(
        userBookId: Int,
        statusCode: Int,
        userDate: String,
    ) {
        val input = UserBookUpdateInput(
            status_id = Optional.present(statusCode),
            user_date = Optional.present(userDate),
        )

        apolloClient.safeMutation(
            mutation = UpdateUserBookStatusMutation(
                id = userBookId,
                `object` = input,
            ),
        )
    }

    override suspend fun replayUpdateBookRating(
        userBookId: Int,
        rating: Double,
    ) {
        val input = UserBookUpdateInput(
            rating = Optional.Present(rating),
        )

        apolloClient.safeMutation(
            mutation = UpdateUserBookRatingMutation(
                id = userBookId,
                `object` = input,
            ),
        )
    }

    override suspend fun replayUpdateBookReview(
        userBookId: Int,
        review: ReviewDocument,
        hasSpoilers: Boolean,
        reviewedAt: String,
    ) {
        val input = UserBookUpdateInput(
            review_slate = Optional.Present(reviewSlateFromDocument(document = review)),
            review_has_spoilers = Optional.Present(hasSpoilers),
            reviewed_at = Optional.Present(reviewedAt),
        )

        apolloClient.safeMutation(
            mutation = UpdateUserBookReviewMutation(
                id = userBookId,
                `object` = input,
            ),
        )
    }
}

// jsonb is unmapped, so the ids arrive as whatever the JSON decoder produced.
private fun Any?.toBookIds(): List<Int> = (this as? List<*>)
    .orEmpty()
    .mapNotNull { (it as? Number)?.toInt() ?: (it as? String)?.toIntOrNull() }
