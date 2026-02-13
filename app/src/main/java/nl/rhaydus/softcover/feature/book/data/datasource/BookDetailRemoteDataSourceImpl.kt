package nl.rhaydus.softcover.feature.book.data.datasource

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import nl.rhaydus.softcover.GetBookByIdQuery
import nl.rhaydus.softcover.MarkBookAsReadingMutation
import nl.rhaydus.softcover.MarkBookAsWantToReadMutation
import nl.rhaydus.softcover.RemoveUserBookMutation
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.book.data.mapper.toBook
import nl.rhaydus.softcover.type.UserBookCreateInput
import nl.rhaydus.softcover.type.UserBookUpdateInput
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BookDetailRemoteDataSourceImpl(
    private val apolloClient: ApolloClient,
) : BookDetailRemoteDataSource {
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
}