package nl.rhaydus.softcover.feature.settings.data.datasource

import com.apollographql.apollo.ApolloClient
import nl.rhaydus.softcover.UserIdQuery
import nl.rhaydus.softcover.feature.book.data.mapper.toBook
import nl.rhaydus.softcover.feature.settings.domain.model.UserInformation

class SettingsRemoteDataSourceImpl(
    private val apolloClient: ApolloClient,
) : SettingsRemoteDataSource {
    override suspend fun getUserInformation(): UserInformation {
        val result = apolloClient
            .query(query = UserIdQuery())
            .execute()
            .dataOrThrow()

        val me = result.me.firstOrNull()
            ?: throw Exception("User could not be initialized")

        return UserInformation(
            id = me.id,
            books = me.user_books.map { it.userBookFragment.toBook() },
        )
    }
}