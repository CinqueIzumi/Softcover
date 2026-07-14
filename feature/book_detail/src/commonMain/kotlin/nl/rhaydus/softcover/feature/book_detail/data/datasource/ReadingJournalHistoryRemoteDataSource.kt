package nl.rhaydus.softcover.feature.book_detail.data.datasource

import com.apollographql.apollo.ApolloClient
import nl.rhaydus.softcover.GetReadingJournalHistoryQuery
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.core.network.helper.safeQuery
import nl.rhaydus.softcover.feature.book_detail.data.mapper.toReadingJournalEntry
import nl.rhaydus.softcover.feature.book_detail.domain.model.ReadingJournalEntry

interface ReadingJournalHistoryRemoteDataSource {
    suspend fun getReadingJournalHistory(bookId: Int): List<ReadingJournalEntry>
}

internal class ReadingJournalHistoryRemoteDataSourceImpl(
    private val apolloClient: ApolloClient,
    private val getUserIdUseCase: GetUserIdUseCase,
) : ReadingJournalHistoryRemoteDataSource {
    override suspend fun getReadingJournalHistory(bookId: Int): List<ReadingJournalEntry> {
        val userId = getUserIdUseCase().getOrThrow()

        val result = apolloClient.safeQuery(
            query = GetReadingJournalHistoryQuery(
                bookId = bookId,
                userId = userId,
                limit = JOURNAL_HISTORY_LIMIT,
            ),
        )

        return result.reading_journals.mapNotNull {
            toReadingJournalEntry(
                updatedAt = it.updated_at,
                metadata = it.metadata,
            )
        }
    }

    private companion object {
        const val JOURNAL_HISTORY_LIMIT = 200
    }
}
