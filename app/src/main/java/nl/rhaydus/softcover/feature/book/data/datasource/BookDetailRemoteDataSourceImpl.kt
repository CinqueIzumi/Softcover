package nl.rhaydus.softcover.feature.book.data.datasource

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import nl.rhaydus.softcover.GetBookByIdQuery
import nl.rhaydus.softcover.UpdateBookStatusMutation
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import nl.rhaydus.softcover.feature.book.data.mapper.toBookWithOptionals
import nl.rhaydus.softcover.type.UserBookUpdateInput

class BookDetailRemoteDataSourceImpl(
    private val apolloClient: ApolloClient,
) : BookDetailRemoteDataSource {
    override suspend fun fetchBookById(
        id: Int,
        userId: Int,
    ): Book {
        val result = apolloClient
            .query(
                GetBookByIdQuery(
                    id = id,
                    userId = userId,
                )
            )
            .execute()
            .dataOrThrow()

        val book = result
            .books
            .firstOrNull()
            ?.bookFragment
            ?.toBookWithOptionals() ?: throw Exception("Book could not be mapped")

        return book
    }

    override suspend fun updateBookStatus(
        book: Book,
        newStatus: BookStatus,
        userId: Int,
    ) {
        val userBookId = book.userBookId ?: throw Exception("user did not have an user book id")

        val mutation = UpdateBookStatusMutation(
            id = userBookId,
            userId = userId,
            `object` = UserBookUpdateInput(
                status_id = Optional.present(newStatus.code)
            )
        )

        apolloClient
            .mutation(mutation = mutation)
            .execute()
            .dataOrThrow()
    }
}