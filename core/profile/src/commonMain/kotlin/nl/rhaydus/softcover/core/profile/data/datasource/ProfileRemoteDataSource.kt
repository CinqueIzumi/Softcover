package nl.rhaydus.softcover.core.profile.data.datasource

import com.apollographql.apollo.ApolloClient
import nl.rhaydus.softcover.GetUserProfileDataQuery
import nl.rhaydus.softcover.core.network.helper.safeQuery
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileSnapshot

interface ProfileRemoteDataSource {
    suspend fun getUserProfileSnapshot(): UserProfileSnapshot
}

internal class ProfileRemoteDataSourceImpl(
    private val apolloClient: ApolloClient,
) : ProfileRemoteDataSource {
    override suspend fun getUserProfileSnapshot(): UserProfileSnapshot {
        val data = apolloClient.safeQuery(query = GetUserProfileDataQuery())
        val me = data
            .me
            .firstOrNull()
            ?: throw Exception("User could not be initialized")

        return UserProfileSnapshot(
            profileImageUrl = me.image?.url.orEmpty(),
            name = me.name.orEmpty(),
            username = me.username?.toString().orEmpty(),
            bio = me.bio.orEmpty(),
        )
    }
}
