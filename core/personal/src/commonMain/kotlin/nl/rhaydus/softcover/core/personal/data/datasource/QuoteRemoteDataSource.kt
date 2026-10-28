package nl.rhaydus.softcover.core.personal.data.datasource

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import nl.rhaydus.softcover.DeleteQuoteMutation
import nl.rhaydus.softcover.GetAllQuotesQuery
import nl.rhaydus.softcover.GetAllQuotesQuery.Data.Reading_journal.Companion.quoteFieldsFragment as allQuoteFieldsFragment
import nl.rhaydus.softcover.GetBookQuotesQuery
import nl.rhaydus.softcover.GetBookQuotesQuery.Data.Reading_journal.Companion.quoteFieldsFragment as bookQuoteFieldsFragment
import nl.rhaydus.softcover.InsertQuoteMutation
import nl.rhaydus.softcover.core.database.model.QuoteEntity
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.core.network.helper.fetchAllPagesOrThrow
import nl.rhaydus.softcover.core.network.helper.safeMutation
import nl.rhaydus.softcover.core.network.helper.safeQuery
import nl.rhaydus.softcover.core.personal.data.mapper.toPosition
import nl.rhaydus.softcover.core.personal.data.mapper.toQuoteEntity
import nl.rhaydus.softcover.core.personal.data.mapper.toQuoteMetadata
import nl.rhaydus.softcover.type.ReadingJournalCreateType
import kotlin.time.Instant

private const val QUOTE_EVENT = "quote"

interface QuoteRemoteDataSource {
    suspend fun getQuotes(bookId: Int): List<QuoteEntity>

    suspend fun getAllQuotes(): List<QuoteEntity>

    /** Returns the `reading_journals.id` Hardcover assigned. */
    suspend fun insertQuote(entity: QuoteEntity): Long

    suspend fun deleteQuote(remoteId: Long)
}

internal class QuoteRemoteDataSourceImpl(
    private val apolloClient: ApolloClient,
    private val getUserIdUseCase: GetUserIdUseCase,
) : QuoteRemoteDataSource {
    override suspend fun getQuotes(bookId: Int): List<QuoteEntity> {
        val userId = getUserIdUseCase().getOrThrow()

        return fetchAllPagesOrThrow(
            pageSize = PAGE_SIZE,
            maxPages = MAX_PAGES,
        ) { limit, offset ->
            apolloClient.safeQuery(
                query = GetBookQuotesQuery(
                    bookId = bookId,
                    userId = userId,
                    limit = limit,
                    offset = offset,
                ),
            ).reading_journals
        }.mapNotNull { it.bookQuoteFieldsFragment()?.toQuoteEntity() }
    }

    override suspend fun getAllQuotes(): List<QuoteEntity> {
        val userId = getUserIdUseCase().getOrThrow()

        return fetchAllPagesOrThrow(
            pageSize = PAGE_SIZE,
            maxPages = MAX_PAGES,
        ) { limit, offset ->
            apolloClient.safeQuery(
                query = GetAllQuotesQuery(
                    userId = userId,
                    limit = limit,
                    offset = offset,
                ),
            ).reading_journals
        }.mapNotNull { it.allQuoteFieldsFragment()?.toQuoteEntity() }
    }

    override suspend fun insertQuote(entity: QuoteEntity): Long {
        val input = ReadingJournalCreateType(
            action_at = Optional.Present(Instant.fromEpochMilliseconds(entity.actionAtEpochMillis).toString()),
            book_id = entity.bookId,
            edition_id = Optional.presentIfNotNull(entity.editionId),
            entry = Optional.Present(entity.text),
            event = QUOTE_EVENT,
            metadata = Optional.Present(entity.toPosition().toQuoteMetadata()),
            privacy_setting_id = entity.privacySettingId,
            tags = emptyList(),
        )

        val response = apolloClient
            .safeMutation(mutation = InsertQuoteMutation(`object` = input))
            .insert_reading_journal
            ?: error("Did not receive an insert-quote response")

        val errors: List<String> = response.errors.orEmpty().filterNotNull().filter { it.isNotBlank() }

        check(errors.isEmpty()) { "InsertQuote rejected by server: ${errors.joinToString()}" }

        return response.id?.toLong() ?: error("InsertQuote returned no id")
    }

    override suspend fun deleteQuote(remoteId: Long) {
        apolloClient.safeMutation(mutation = DeleteQuoteMutation(id = remoteId.toInt()))
    }
}

// Request-multiplier budget: ceiling of PAGE_SIZE * MAX_PAGES rows before the fetch fails.
private const val PAGE_SIZE: Int = 100

private const val MAX_PAGES: Int = 100
