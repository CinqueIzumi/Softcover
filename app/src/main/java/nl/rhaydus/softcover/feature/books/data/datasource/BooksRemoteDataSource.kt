package nl.rhaydus.softcover.feature.books.data.datasource

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import nl.rhaydus.softcover.GetBookByIdQuery
import nl.rhaydus.softcover.GetUserBooksQuery
import nl.rhaydus.softcover.MarkBookAsReadMutation
import nl.rhaydus.softcover.MarkBookAsReadingMutation
import nl.rhaydus.softcover.MarkBookAsWantToReadMutation
import nl.rhaydus.softcover.RemoveUserBookMutation
import nl.rhaydus.softcover.UpdateBookEditionMutation
import nl.rhaydus.softcover.UpdateReadingProgressMutation
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.books.data.mapper.toBook
import nl.rhaydus.softcover.type.DatesReadInput
import nl.rhaydus.softcover.type.UserBookCreateInput
import nl.rhaydus.softcover.type.UserBookUpdateInput
import java.time.LocalDate
import java.time.format.DateTimeFormatter

interface BooksRemoteDataSource {
    suspend fun fetchBookById(id: Int): Book

    suspend fun markBookAsWantToRead(bookId: Int): Book

    suspend fun markBookAsReading(book: Book): Book

    suspend fun removeBookFromLibrary(book: Book)

    suspend fun initializeBooks(userId: Int): List<Book>

    suspend fun updateBookProgress(
        book: Book,
        newPage: Int,
    ): Book

    suspend fun markBookAsRead(book: Book): Book

    suspend fun updateBookEdition(
        userBookId: Int,
        newEditionId: Int,
    ): Book
}

class BooksRemoteDataSourceImpl(
    private val apolloClient: ApolloClient,
) : BooksRemoteDataSource {
    override suspend fun fetchBookById(id: Int): Book {
        val result = apolloClient
            .query(GetBookByIdQuery(id = id))
            .execute()
            .dataOrThrow()

        val book = result
            .books
            .firstOrNull()
            ?.bookFragment
            ?.toBook() ?: throw Exception("Book could not be mapped")

        return book
    }

    override suspend fun markBookAsWantToRead(bookId: Int): Book {
        val userBookCreateInput = UserBookCreateInput(
            book_id = bookId,
            status_id = Optional.Present(1),
            privacy_setting_id = Optional.Present(1),
        )

        val result = apolloClient
            .mutation(mutation = MarkBookAsWantToReadMutation(`object` = userBookCreateInput))
            .execute()
            .dataOrThrow()

        val book = result
            .insert_user_book
            ?.user_book
            ?.userBookFragment
            ?.toBook() ?: throw Exception("Book could not be mapped")

        return book
    }

    override suspend fun markBookAsReading(book: Book): Book {
        val userBook = book.userBook
            ?: throw Exception("User did not have a user book")

        val currentDate = LocalDate
            .now()
            .format(DateTimeFormatter.ISO_LOCAL_DATE)

        val input = UserBookUpdateInput(
            edition_id = Optional.Present(book.currentEdition.id),
            review_has_spoilers = Optional.Present(userBook.reviewHasSpoilers),
            status_id = Optional.present(2),
            last_read_date = Optional.Present(userBook.lastReadDate),
            rating = Optional.Present(userBook.rating),
            privacy_setting_id = Optional.Present(1),
            referrer_user_id = Optional.Present(userBook.referrerUserId),
            reviewed_at = Optional.Present(userBook.reviewedAt),
            date_added = Optional.Present(userBook.dateAdded),
            user_date = Optional.present(currentDate)
        )

        val book = apolloClient
            .mutation(
                mutation = MarkBookAsReadingMutation(
                    id = userBook.id,
                    `object` = input
                )
            )
            .execute()
            .dataOrThrow()
            .update_user_book
            ?.user_book
            ?.userBookFragment
            ?.toBook() ?: throw Exception("Book could not be mapped")

        return book
    }

    override suspend fun removeBookFromLibrary(book: Book) {
        val userBookId = book.userBook?.id
            ?: throw Exception("User did not have a user book")

        apolloClient
            .mutation(mutation = RemoveUserBookMutation(id = userBookId))
            .execute()
            .dataOrThrow()
    }

    override suspend fun initializeBooks(userId: Int): List<Book> {
        val result = apolloClient
            .query(query = GetUserBooksQuery())
            .execute()
            .dataOrThrow()

        val userBooks = result.me.firstOrNull()?.user_books
            ?: throw Exception("No books were found")

        return userBooks.map { it.userBookFragment.toBook() }
    }

    override suspend fun updateBookProgress(
        book: Book,
        newPage: Int,
    ): Book {
        val userBook = book.userBook
            ?: throw Exception("Book did not contain a user book")

        val userBookRead =
            book.userBookRead ?: throw Exception("Book did not contain a user book read")

        val dataObject = DatesReadInput(
            progress_pages = Optional.present(newPage),
            started_at = Optional.present(userBookRead.startedAt),
            finished_at = Optional.present(userBookRead.finishedAt),
            edition_id = Optional.present(userBook.editionId),
        )

        val mutation = UpdateReadingProgressMutation(
            id = userBookRead.id,
            datesReadInput = dataObject
        )

        val result = apolloClient
            .mutation(mutation = mutation)
            .execute()
            .dataOrThrow()

        val userBookReadFragment =
            result.update_user_book_read?.user_book_read?.userBookReadFragment
                ?: throw Exception("Did not receive a new user book read fragment")

        val updatedUserBookRead = userBookRead.copy(
            currentPage = userBookReadFragment.progress_pages,
            progress = userBookReadFragment.progress?.toFloat()
        )

        val updatedBook = book.copy(userBookRead = updatedUserBookRead)

        return updatedBook
    }

    override suspend fun markBookAsRead(book: Book): Book {
        val currentDate = LocalDate.now().toString()

        val dataObject = UserBookCreateInput(
            book_id = book.id,
            status_id = Optional.present(UserBookStatus.READ.code),
            user_date = Optional.present(currentDate)
        )

        val mutation = MarkBookAsReadMutation(userBookCreateInput = dataObject)

        val result = apolloClient
            .mutation(mutation = mutation)
            .execute()
            .dataOrThrow()

        val userBookFragment = result.insert_user_book?.user_book?.userBookFragment
            ?: throw Exception("Did not receive a new user book fragment")

        return userBookFragment.toBook()
    }

    override suspend fun updateBookEdition(
        userBookId: Int,
        newEditionId: Int,
    ): Book {
        val mutation = UpdateBookEditionMutation(
            id = userBookId,
            `object` = UserBookUpdateInput(
                edition_id = Optional.present(newEditionId)
            )
        )

        val result = apolloClient
            .mutation(mutation = mutation)
            .execute()
            .dataOrThrow()

        val userBookFragment = result.update_user_book?.user_book?.userBookFragment
            ?: throw Exception("Did not receive a new user book fragment")

        return userBookFragment.toBook()
    }
}