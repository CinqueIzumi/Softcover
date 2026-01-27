package nl.rhaydus.softcover.feature.book.data.datasource

import com.apollographql.apollo.ApolloClient
import nl.rhaydus.softcover.GetBookByIdQuery
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.reading.data.mapper.toBook
import javax.inject.Inject

class BookDetailRemoteDataSourceImpl @Inject constructor(
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

        val book: Book = result
            .books
            .firstOrNull()
            ?.user_books
            ?.firstOrNull()
            ?.user_book_reads
            ?.firstOrNull()
            ?.userBookReadFragment
            ?.toBook() ?: throw Exception("Book could not be mapped")

        return book
    }
}