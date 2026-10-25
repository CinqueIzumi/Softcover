package nl.rhaydus.softcover.core.personal.data.datasource

import com.apollographql.apollo.ApolloClient
import nl.rhaydus.softcover.GetAllReadingJournalHistoryQuery
import nl.rhaydus.softcover.GetReadingJournalHistoryQuery
import nl.rhaydus.softcover.core.database.model.ProgressHistoryEntity
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.core.network.helper.fetchAllPagesOrThrow
import nl.rhaydus.softcover.core.network.helper.safeQuery
import nl.rhaydus.softcover.core.personal.data.mapper.toProgressHistoryEntity

interface ReadingJournalHistoryRemoteDataSource {
    suspend fun getReadingJournalHistory(bookId: Int): List<ProgressHistoryEntity>

    /** Every `progress_updated` journal row for the user across all books, oldest first. */
    suspend fun getAllReadingJournalHistory(): List<ProgressHistoryEntity>
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

    override suspend fun getAllReadingJournalHistory(): List<ProgressHistoryEntity> {
        val userId = getUserIdUseCase().getOrThrow()

        return fetchAllPagesOrThrow(
            pageSize = ALL_BOOKS_PAGE_SIZE,
            maxPages = ALL_BOOKS_MAX_PAGES,
        ) { limit, offset ->
            apolloClient.safeQuery(
                query = GetAllReadingJournalHistoryQuery(
                    userId = userId,
                    limit = limit,
                    offset = offset,
                ),
            ).reading_journals
        }.mapNotNull { row ->
            row.book_id?.let { bookId ->
                toProgressHistoryEntity(
                    bookId = bookId,
                    actionAt = row.action_at,
                    metadata = row.metadata,
                )
            }
        }
    }

    private suspend fun fetchAllJournalRows(
        bookId: Int,
        userId: Int,
    ): List<GetReadingJournalHistoryQuery.Data.Reading_journal> =
        fetchAllPagesOrThrow(
            pageSize = PAGE_SIZE,
            maxPages = MAX_PAGES,
        ) { limit, offset ->
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

// Request-multiplier budget: ceiling of PAGE_SIZE * MAX_PAGES rows before the fetch fails.
private const val PAGE_SIZE: Int = 100

private const val MAX_PAGES: Int = 100

// A whole account's updates span many books: larger pages keep the request count down, for a ceiling of 50,000 rows.
private const val ALL_BOOKS_PAGE_SIZE: Int = 500

private const val ALL_BOOKS_MAX_PAGES: Int = 100
