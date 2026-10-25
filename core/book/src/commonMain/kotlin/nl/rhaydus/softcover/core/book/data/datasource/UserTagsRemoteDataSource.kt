package nl.rhaydus.softcover.core.book.data.datasource

import com.apollographql.apollo.ApolloClient
import nl.rhaydus.softcover.FindTagsByUserAndTaggableQuery
import nl.rhaydus.softcover.SaveTagsMutation
import nl.rhaydus.softcover.core.book.data.mapper.toBasicTag
import nl.rhaydus.softcover.core.book.data.mapper.toUserTag
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.core.network.helper.safeMutation
import nl.rhaydus.softcover.core.network.helper.safeQuery

private const val TAGGABLE_TYPE_BOOK = "Book"

internal interface UserTagsRemoteDataSource {
    suspend fun getUserTags(
        userId: Int,
        bookId: Int,
    ): List<UserTag>

    suspend fun saveTags(
        bookId: Int,
        tags: List<UserTag>,
    ): List<UserTag>
}

internal class UserTagsRemoteDataSourceImpl(
    private val apolloClient: ApolloClient,
) : UserTagsRemoteDataSource {
    override suspend fun getUserTags(
        userId: Int,
        bookId: Int,
    ): List<UserTag> {
        val result = apolloClient.safeQuery(
            query = FindTagsByUserAndTaggableQuery(
                userId = userId,
                type = TAGGABLE_TYPE_BOOK,
                id = bookId.toLong(),
            ),
        )

        return result.taggings.map { it.toUserTag(tag = it.tag) }
    }

    override suspend fun saveTags(
        bookId: Int,
        tags: List<UserTag>,
    ): List<UserTag> {
        val result = apolloClient.safeMutation(
            mutation = SaveTagsMutation(
                id = bookId.toLong(),
                tags = tags.map { it.toBasicTag() },
                type = TAGGABLE_TYPE_BOOK,
            ),
        )

        return result.upsertTags?.tags?.filterNotNull()?.map { it.toUserTag() }.orEmpty()
    }
}
