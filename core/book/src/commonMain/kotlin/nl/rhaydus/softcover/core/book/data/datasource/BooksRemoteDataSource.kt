package nl.rhaydus.softcover.core.book.data.datasource

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.cache.normalized.FetchPolicy
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
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
import nl.rhaydus.softcover.GetTrendingBookIdsQuery
import nl.rhaydus.softcover.GetUserBooksQuery
import nl.rhaydus.softcover.GetUserBooksQuery.Data.Me.User_book.Companion.userBookFragment
import nl.rhaydus.softcover.MarkBookAsReadMutation
import nl.rhaydus.softcover.MarkBookAsReadMutation.Data.Insert_user_book.User_book.Companion.userBookFragment
import nl.rhaydus.softcover.MarkBookAsReadingMutation
import nl.rhaydus.softcover.MarkBookAsReadingMutation.Data.Update_user_book.User_book.Companion.userBookFragment
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
import nl.rhaydus.softcover.core.book.data.mapper.toBook
import nl.rhaydus.softcover.core.book.data.mapper.toBookEdition
import nl.rhaydus.softcover.core.book.domain.model.CreatedBook
import nl.rhaydus.softcover.core.book.domain.model.IsbnEditionMatch
import nl.rhaydus.softcover.core.database.mapper.reviewSlateFromDocument
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.network.helper.safeMutation
import nl.rhaydus.softcover.core.network.helper.safeQuery
import nl.rhaydus.softcover.type.DatesReadInput
import nl.rhaydus.softcover.type.UserBookCreateInput
import nl.rhaydus.softcover.type.UserBookUpdateInput

interface BooksRemoteDataSource {
    suspend fun fetchBookById(id: Int): Book

    suspend fun fetchBookIdForEdition(editionId: Int): Int?

    suspend fun fetchEditionMatchesForIsbns(isbns: List<String>): Map<String, IsbnEditionMatch>

    suspend fun addBookByIsbn(isbn: String): CreatedBook

    suspend fun fetchBooksByIds(
        ids: List<Int>,
        forceNetwork: Boolean = false,
    ): List<Book>

    suspend fun fetchTrendingBooks(
        from: String,
        to: String,
        limit: Int,
        offset: Int,
    ): List<Book>

    suspend fun markBookAsWantToRead(
        bookId: Int,
        editionId: Int? = null,
    ): Book

    suspend fun markBookAsReading(book: Book): Book

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
    ): List<Book>

    suspend fun getEditionsByBookId(bookId: Int): List<BookEdition>

    suspend fun fetchEditionsByIds(
        ids: List<Int>,
        forceNetwork: Boolean = false,
    ): List<BookEdition>

    suspend fun updateBookProgress(
        book: Book,
        newPage: Int? = null,
        newSeconds: Int? = null,
    ): Book

    suspend fun markBookAsRead(
        book: Book,
        editionId: Int? = null,
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
    )

    suspend fun replayMarkBookAsRead(
        bookId: Int,
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

/** Hardcover's ISBN external-book provider platform — `upsert_book` uses the ISBN as the external id. */
private const val HARDCOVER_ISBN_PLATFORM_ID: Int = 8

internal class BooksRemoteDataSourceImpl(
    private val apolloClient: ApolloClient,
    private val appDispatchers: AppDispatchers,
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

    override suspend fun addBookByIsbn(isbn: String): CreatedBook {
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

        val bookId = response.book?.id
            ?: throw Exception("upsert_book returned no book")

        return CreatedBook(
            bookId = bookId,
            editionId = response.edition?.id,
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

    override suspend fun fetchTrendingBooks(
        from: String,
        to: String,
        limit: Int,
        offset: Int,
    ): List<Book> {
        val trendingIds: List<Int> = apolloClient.safeQuery(
            query = GetTrendingBookIdsQuery(
                from = from,
                to = to,
                limit = limit,
                offset = offset,
            ),
        )
            .books_trending
            ?.ids
            ?.mapNotNull { it }
            ?: return emptyList()

        if (trendingIds.isEmpty()) return emptyList()

        val idOrdered = trendingIds.withIndex().associate { it.value to it.index }

        return apolloClient
            .safeQuery(
                query = GetBooksByIdsQuery(ids = trendingIds),
                fetchPolicy = FetchPolicy.CacheFirst,
            )
            .books
            .mapNotNull { it.booksByIdsBookDetailFragment()?.toBook() }
            // Restore the trending rank lost by the by-ids batch fetch; a book whose id isn't in the
            // trending set (e.g. an Apollo canonical-merge redirect) sorts last on its null rank.
            .sortedBy { book -> idOrdered[book.id] }
    }

    override suspend fun getEditionsByBookId(bookId: Int): List<BookEdition> {
        val result = apolloClient.safeQuery(
            query = GetEditionsByBookIdQuery(bookId = bookId),
            fetchPolicy = FetchPolicy.CacheFirst,
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

    override suspend fun markBookAsReading(book: Book): Book {
        val userBook = book.userBook
            ?: throw Exception("User did not have a user book")

        val currentDate = Clock.System
            .todayIn(TimeZone.currentSystemDefault())
            .toString()

        val input = UserBookUpdateInput(
            edition_id = Optional.Present(book.currentEdition?.id),
            review_has_spoilers = Optional.Present(userBook.reviewHasSpoilers),
            status_id = Optional.present(UserBookStatus.CURRENTLY_READING.code),
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
                mutation = MarkBookAsReadingMutation(
                    id = userBook.id,
                    `object` = input,
                ),
            )
            .update_user_book
            ?.user_book
            ?.userBookFragment()
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
    ): List<Book> = withContext(appDispatchers.io) {
        val result = apolloClient.safeQuery(
            query = GetUserBooksQuery(statusIds = Optional.presentIfNotNull(statusIds?.toList())),
        )

        val userBooks = result.me.firstOrNull()?.user_books
            ?: throw Exception("No books were found")

        val mapped = userBooks.mapNotNull { it.userBookFragment()?.toBook() }

        resolveCanonicalMerges(books = mapped)
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
    ): Book {
        val userBook = book.userBook
            ?: throw Exception("Book did not contain a user book")

        val userBookRead =
            book.userBookRead ?: throw Exception("Book did not contain a user book read")

        val isAudiobook = newSeconds != null

        val dataObject = DatesReadInput(
            progress_pages = if (isAudiobook) Optional.absent() else Optional.present(newPage),
            progress_seconds = if (isAudiobook) Optional.present(newSeconds) else Optional.absent(),
            started_at = Optional.present(userBookRead.startedAt),
            finished_at = Optional.present(userBookRead.finishedAt),
            edition_id = Optional.present(userBook.editionId),
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
    ): Book {
        val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()

        val dataObject = UserBookCreateInput(
            book_id = book.id,
            edition_id = Optional.presentIfNotNull(editionId),
            status_id = Optional.present(UserBookStatus.READ.code),
            user_date = Optional.present(currentDate),
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
                status_id = Optional.present(userBook.status.code),
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
    ) {
        val isAudiobook = progressSeconds != null

        val dataObject = DatesReadInput(
            progress_pages = if (isAudiobook) Optional.absent() else Optional.present(progressPages),
            progress_seconds = if (isAudiobook) Optional.present(progressSeconds) else Optional.absent(),
            started_at = Optional.present(startedAt),
            finished_at = Optional.present(finishedAt),
            edition_id = Optional.present(editionId),
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
        userDate: String,
    ) {
        val dataObject = UserBookCreateInput(
            book_id = bookId,
            status_id = Optional.present(UserBookStatus.READ.code),
            user_date = Optional.present(userDate),
            privacy_setting_id = Optional.present(PrivacySetting.PUBLIC.code),
        )

        apolloClient.safeMutation(mutation = MarkBookAsReadMutation(userBookCreateInput = dataObject))
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
