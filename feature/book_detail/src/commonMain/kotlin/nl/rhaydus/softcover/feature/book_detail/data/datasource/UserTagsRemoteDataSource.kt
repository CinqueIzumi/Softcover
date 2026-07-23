package nl.rhaydus.softcover.feature.book_detail.data.datasource

import com.apollographql.apollo.ApolloClient
import nl.rhaydus.softcover.FindTagsByUserAndTaggableQuery
import nl.rhaydus.softcover.FindUserTagVocabularyQuery
import nl.rhaydus.softcover.SaveTagsMutation
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.core.network.helper.fetchAllPages
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

    // Queries distinct tags the user has ever applied, with the user's personal usage count
    // computed server-side by a nested taggings_aggregate — rather than paging the user's raw
    // taggings and counting occurrences client-side. A heavy user has far fewer distinct tags
    // than taggings (~150 vs. thousands), so this is a fraction of the requests.
    //
    // The fetch is still paginated rather than a single unbounded query, and still ordered by the
    // row's own id: an unstable order_by would make offset windows non-deterministic, letting rows
    // be skipped or repeated across pages once the result set exceeds one page. Paging through the
    // full result set and grouping once at the end guards against that regardless of how many
    // distinct tags the user has.
    //
    // Grouping by (category, name) still runs after paging: two distinct tag rows can share a
    // category + name (the pair a suggestion is keyed on), and their counts must be summed into
    // one suggestion rather than shown as duplicates.
    override suspend fun fetchUserVocabulary(userId: Int): List<UserTag> =
        fetchAllTagRows(userId)
            .map { it.toUserTag() }
            .groupBy { it.category to it.name }
            .map { (categoryAndName, rows) ->
                val (category, name) = categoryAndName

                UserTag(
                    name = name,
                    category = category,
                    count = rows.sumOf { it.count },
                    spoiler = false,
                )
            }

    private suspend fun fetchAllTagRows(userId: Int): List<FindUserTagVocabularyQuery.Data.Tag> =
        fetchAllPages { limit, offset ->
            apolloClient.safeQuery(
                query = FindUserTagVocabularyQuery(
                    userId = userId,
                    limit = limit,
                    offset = offset,
                ),
            ).tags
        }
}
