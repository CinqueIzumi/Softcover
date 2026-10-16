package nl.rhaydus.softcover.core.personal.data.mapper

import nl.rhaydus.softcover.GetBookQuotesQuery
import nl.rhaydus.softcover.core.database.model.QuoteEntity
import nl.rhaydus.softcover.core.database.model.QuoteSyncState
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.core.domain.model.Quote
import nl.rhaydus.softcover.core.domain.model.QuotePosition
import kotlin.time.Instant

private const val POSITION_PAGES = "pages"
private const val POSITION_PERCENT = "percent"
private const val POSITION_SECONDS = "seconds"
private const val PERCENT_POSSIBLE = 100.0

internal fun QuoteEntity.toDomain(): Quote = Quote(
    localId = localId,
    remoteId = remoteId,
    bookId = bookId,
    editionId = editionId,
    text = text,
    privacy = PrivacySetting.entries.firstOrNull { it.code == privacySettingId } ?: PrivacySetting.PUBLIC,
    actionAt = Instant.fromEpochMilliseconds(actionAtEpochMillis),
    position = toPosition(),
    isPending = syncState == QuoteSyncState.PENDING_INSERT,
)

internal fun QuoteEntity.toPosition(): QuotePosition? {
    val value: Double = positionValue ?: return null

    return when (positionType) {
        POSITION_PAGES -> QuotePosition.Pages(
            page = value.toInt(),
            totalPages = positionPossible?.toInt(),
            percent = positionPercent,
        )

        POSITION_SECONDS -> QuotePosition.Seconds(
            seconds = value.toInt(),
            totalSeconds = positionPossible?.toInt(),
            percent = positionPercent,
        )

        POSITION_PERCENT -> QuotePosition.Percent(percent = positionPercent ?: value)

        else -> null
    }
}

internal fun newPendingQuoteEntity(
    bookId: Int,
    editionId: Int?,
    text: String,
    privacy: PrivacySetting,
    actionAt: Instant,
    position: QuotePosition?,
): QuoteEntity = positionColumns(position = position).let { columns ->
    QuoteEntity(
        remoteId = null,
        bookId = bookId,
        editionId = editionId,
        text = text,
        privacySettingId = privacy.code,
        actionAtEpochMillis = actionAt.toEpochMilliseconds(),
        positionType = columns.type,
        positionValue = columns.value,
        positionPossible = columns.possible,
        positionPercent = columns.percent,
        syncState = QuoteSyncState.PENDING_INSERT,
    )
}

/** Rows with no entry text or an unparsable date are dropped: a quote without either cannot be shown. */
internal fun GetBookQuotesQuery.Data.Reading_journal.toQuoteEntity(bookId: Int): QuoteEntity? {
    val text: String = entry?.takeIf { it.isNotBlank() } ?: return null
    val instant: Instant = runCatching { Instant.parse(action_at) }.getOrNull() ?: return null
    val columns: PositionColumns = positionColumns(position = quotePositionFromMetadata(metadata = metadata))

    return QuoteEntity(
        remoteId = id,
        bookId = bookId,
        editionId = edition_id,
        text = text,
        privacySettingId = privacy_setting_id,
        actionAtEpochMillis = instant.toEpochMilliseconds(),
        positionType = columns.type,
        positionValue = columns.value,
        positionPossible = columns.possible,
        positionPercent = columns.percent,
        syncState = QuoteSyncState.SYNCED,
    )
}

private data class PositionColumns(
    val type: String?,
    val value: Double?,
    val possible: Double?,
    val percent: Double?,
)

private fun positionColumns(position: QuotePosition?): PositionColumns = when (position) {
    null -> PositionColumns(
        type = null,
        value = null,
        possible = null,
        percent = null,
    )

    is QuotePosition.Pages -> PositionColumns(
        type = POSITION_PAGES,
        value = position.page.toDouble(),
        possible = position.totalPages?.toDouble(),
        percent = position.percent,
    )

    is QuotePosition.Seconds -> PositionColumns(
        type = POSITION_SECONDS,
        value = position.seconds.toDouble(),
        possible = position.totalSeconds?.toDouble(),
        percent = position.percent,
    )

    is QuotePosition.Percent -> PositionColumns(
        type = POSITION_PERCENT,
        value = position.percent,
        possible = PERCENT_POSSIBLE,
        percent = position.percent,
    )
}
