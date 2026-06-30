package nl.rhaydus.softcover.core.preferences.data.datasource

import com.apollographql.apollo.ApolloClient
import nl.rhaydus.softcover.GetUserIdQuery
import nl.rhaydus.softcover.core.network.helper.safeQuery

interface SettingsRemoteDataSource {
    suspend fun getUserIdFromBackend(): Int
}

internal class SettingsRemoteDataSourceImpl(
    private val apolloClient: ApolloClient,
) : SettingsRemoteDataSource {
    override suspend fun getUserIdFromBackend(): Int {
        val me = apolloClient
            .safeQuery(query = GetUserIdQuery())
            .me
            .firstOrNull()
            ?: throw RuntimeException("User could not be initialized")

        return me.id
    }
}
