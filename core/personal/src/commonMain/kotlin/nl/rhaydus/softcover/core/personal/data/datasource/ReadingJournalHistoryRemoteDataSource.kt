package nl.rhaydus.softcover.core.personal.data.datasource

import com.apollographql.apollo.ApolloClient
import nl.rhaydus.softcover.GetReadingJournalHistoryQuery
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.core.network.helper.fetchAllPages
import nl.rhaydus.softcover.core.network.helper.safeQuery
import nl.rhaydus.softcover.core.personal.data.mapper.toReadingJournalEntry
import nl.rhaydus.softcover.core.personal.domain.model.ReadingJournalEntry

interface ReadingJournalHistoryRemoteDataSource {
    suspend fun getReadingJournalHistory(bookId: Int): List<ReadingJournalEntry>
}

internal class ReadingJournalHistoryRemoteDataSourceImpl(
    private val apolloClient: ApolloClient,
    private val getUserIdUseCase: GetUserIdUseCase,
) : ReadingJournalHistoryRemoteDataSource {
    override suspend fun getReadingJournalHistory(bookId: Int): List<ReadingJournalEntry> {
        val userId = getUserIdUseCase().getOrThrow()

        return fetchAllJournalRows(
            bookId = bookId,
            userId = userId,
        ).mapNotNull {
            toReadingJournalEntry(
                updatedAt = it.updated_at,
                metadata = it.metadata,
            )
        }
    }

    // A single fetch used to cap at a client-side limit of 200, so a book with more than 200
    // progress updates kept only the OLDEST 200 (that limit, ordered updated_at ascending) and
    // dropped the most recent ones - exactly the data the pace forecast needs. Paging removes that
    // ceiling. (The server's own per-request cap is far higher, in the high hundreds, so it was the
    // client-side 200 that bit, not the server.)
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
