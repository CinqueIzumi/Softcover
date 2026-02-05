package nl.rhaydus.softcover.feature.settings.data.datasource

import com.apollographql.apollo.ApolloClient
import nl.rhaydus.softcover.GetUserIdQuery
import nl.rhaydus.softcover.GetUserProfileDataQuery
import nl.rhaydus.softcover.feature.settings.domain.model.UserProfileData

class SettingsRemoteDataSourceImpl(
    private val apolloClient: ApolloClient,
) : SettingsRemoteDataSource {
    override suspend fun getUserIdFromBackend(): Int {
        val result = apolloClient
            .query(query = GetUserIdQuery())
            .execute()
            .dataOrThrow()

        val me = result.me.firstOrNull()
            ?: throw Exception("User could not be initialized")

        return me.id
    }

    override suspend fun getUserProfileData(): UserProfileData {
        val result = apolloClient
            .query(query = GetUserProfileDataQuery())
            .execute()
            .dataOrThrow()

        val me = result.me.firstOrNull()
            ?: throw Exception("User could not be initialized")

        return UserProfileData(
            profileImageUrl = me.image?.url ?: "",
            name = me.name ?: "",
            bio = me.bio ?: "",
            booksRead = me.user_books.firstOrNull()?.read_count ?: -1,
        )
    }
}