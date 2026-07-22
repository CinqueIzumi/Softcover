package nl.rhaydus.softcover.feature.book_detail.data.datasource

import com.apollographql.apollo.ApolloClient
import nl.rhaydus.softcover.FindTagsByUserAndTaggableQuery
import nl.rhaydus.softcover.FindTagsByUserQuery
import nl.rhaydus.softcover.SaveTagsMutation
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.core.network.helper.safeMutation
import nl.rhaydus.softcover.core.network.helper.safeQuery
import nl.rhaydus.softcover.feature.book_detail.data.mapper.toBasicTag
import nl.rhaydus.softcover.feature.book_detail.data.mapper.toUserTag

private const val TAGGABLE_TYPE_BOOK = "Book"

interface UserTagsRemoteDataSource {
    suspend fun getUserTags(
        userId: Int,
        bookId: Int,
    ): List<UserTag>

    suspend fun saveTags(
        bookId: Int,
        tags: List<UserTag>,
    ): List<UserTag>

    suspend fun fetchUserVocabulary(userId: Int): List<UserTag>
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

        return result.taggings.map { it.toUserTag() }
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

    // The user's personal usage frequency per (category, name) — distinct from the tag's global
    // popularity count carried by TagFragment.count — computed client-side from the raw taggings
    // list since the API has no per-user tag-frequency aggregate.
    override suspend fun fetchUserVocabulary(userId: Int): List<UserTag> {
        val result = apolloClient.safeQuery(
            query = FindTagsByUserQuery(userId = userId),
        )

        return result.taggings
            .map { it.toUserTag() }
            .groupBy { it.category to it.name }
            .map { (categoryAndName, occurrences) ->
                val (category, name) = categoryAndName

                UserTag(
                    name = name,
                    category = category,
                    count = occurrences.size,
                    spoiler = false,
                )
            }
    }
}
