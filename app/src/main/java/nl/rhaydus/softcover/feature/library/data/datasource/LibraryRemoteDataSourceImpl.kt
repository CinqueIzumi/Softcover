package nl.rhaydus.softcover.feature.library.data.datasource

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.apollographql.apollo.cache.normalized.watch
import com.apollographql.apollo.exception.ApolloHttpException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import nl.rhaydus.softcover.GetUserBooksQuery
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.reading.data.mapper.toBookWithOptionals
import timber.log.Timber

class LibraryRemoteDataSourceImpl(
    private val apolloClient: ApolloClient,
) : LibraryRemoteDataSource {
    override suspend fun getUserBooks(userId: Int): Flow<List<Book>> {
        return apolloClient
            .query(query = GetUserBooksQuery(userId = userId))
            .fetchPolicy(FetchPolicy.CacheAndNetwork)
            .watch()
            .mapNotNull { result ->
                if (userId == -1) return@mapNotNull emptyList()

                result.exception?.let { exception ->
                    Timber.e("-=- Something went wrong fetching currently reading! ${exception.message}")

                    if (exception is ApolloHttpException && exception.statusCode == 401) {
                        return@mapNotNull emptyList()
                    }
                }

                result.data?.user_books?.map { it.book.bookFragment.toBookWithOptionals() }
            }
    }

    override suspend fun refreshUserBooks(userId: Int) {
        apolloClient
            .query(query = GetUserBooksQuery(userId = userId))
            .fetchPolicy(FetchPolicy.NetworkOnly)
            .execute()
            .dataOrThrow()
    }
}