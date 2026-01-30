package nl.rhaydus.softcover.feature.book.data.datasource

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import nl.rhaydus.softcover.GetBookByIdQuery
import nl.rhaydus.softcover.MarkBookAsWantToReadMutation
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.book.data.mapper.toBook
import nl.rhaydus.softcover.type.UserBookCreateInput

class BookDetailRemoteDataSourceImpl(
    private val apolloClient: ApolloClient,
) : BookDetailRemoteDataSource {
    override suspend fun fetchBookById(id: Int): Book {
        val result = apolloClient
            .query(
                GetBookByIdQuery(id = id)
            )
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
}