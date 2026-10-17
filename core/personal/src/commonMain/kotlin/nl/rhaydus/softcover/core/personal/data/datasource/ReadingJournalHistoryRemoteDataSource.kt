package nl.rhaydus.softcover.core.personal.data.datasource

import com.apollographql.apollo.ApolloClient
import nl.rhaydus.softcover.GetReadingJournalHistoryQuery
import nl.rhaydus.softcover.core.database.model.ProgressHistoryEntity
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.core.network.helper.fetchAllPages
import nl.rhaydus.softcover.core.network.helper.safeQuery
import nl.rhaydus.softcover.core.personal.data.mapper.toProgressHistoryEntity

interface ReadingJournalHistoryRemoteDataSource {
    suspend fun getReadingJournalHistory(bookId: Int): List<ProgressHistoryEntity>
}

internal class ReadingJournalHistoryRemoteDataSourceImpl(
    private val apolloClient: ApolloClient,
    private val getUserIdUseCase: GetUserIdUseCase,
) : ReadingJournalHistoryRemoteDataSource {
    override suspend fun getReadingJournalHistory(bookId: Int): List<ProgressHistoryEntity> {
        val userId = getUserIdUseCase().getOrThrow()

        return fetchAllJournalRows(
            bookId = bookId,
            userId = userId,
        ).mapNotNull {
            toProgressHistoryEntity(
                bookId = bookId,
                actionAt = it.action_at,
                metadata = it.metadata,
            )
        }
    }

    private suspend fun fetchAllJournalRows(
        bookId: Int,
        userId: Int,
    ): List<GetReadingJournalHistoryQuery.Data.Reading_journal> =
        fetchAllPages { limit, offset ->
            apolloClient.safeQuery(
                query = GetReadingJournalHistoryQuery(
                    bookId = bookId,
                    userId = userId,
                    limit = limit,
                    offset = offset,
                ),
            ).reading_journals
        }
}
