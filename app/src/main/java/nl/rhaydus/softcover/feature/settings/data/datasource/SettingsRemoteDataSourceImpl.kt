package nl.rhaydus.softcover.feature.settings.data.datasource

import com.apollographql.apollo.ApolloClient
import nl.rhaydus.softcover.UserIdQuery

class SettingsRemoteDataSourceImpl(
    private val apolloClient: ApolloClient,
) : SettingsRemoteDataSource {
    override suspend fun getUserId(): Int {
        val result = apolloClient
            .query(query = UserIdQuery())
            .execute()

        val id = result
            .data
            ?.me
            ?.firstOrNull()
            ?.id ?: throw Exception("User could not be initialized.")

        return id
    }
}