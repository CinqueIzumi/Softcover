package nl.rhaydus.softcover.feature.book_detail.data.datasource

import com.apollographql.apollo.ApolloClient
import nl.rhaydus.softcover.FindUserTagVocabularyQuery
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.core.network.helper.fetchAllPagesOrThrow
import nl.rhaydus.softcover.core.network.helper.safeQuery
import nl.rhaydus.softcover.feature.book_detail.data.mapper.toUserTag

interface UserTagVocabularyRemoteDataSource {
    suspend fun fetchUserVocabulary(userId: Int): List<UserTag>
}

internal class UserTagVocabularyRemoteDataSourceImpl(
    private val apolloClient: ApolloClient,
) : UserTagVocabularyRemoteDataSource {
    // Two distinct tag rows can share a category and name; their counts are summed into one suggestion.
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
        fetchAllPagesOrThrow(
            pageSize = PAGE_SIZE,
            maxPages = MAX_PAGES,
        ) { limit, offset ->
            apolloClient.safeQuery(
                query = FindUserTagVocabularyQuery(
                    userId = userId,
                    limit = limit,
                    offset = offset,
                ),
            ).tags
        }
}

// Request-multiplier budget: ceiling of PAGE_SIZE * MAX_PAGES rows before the fetch fails.
private const val PAGE_SIZE: Int = 100

private const val MAX_PAGES: Int = 100
