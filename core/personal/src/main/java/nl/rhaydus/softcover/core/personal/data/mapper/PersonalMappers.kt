package nl.rhaydus.softcover.core.personal.data.mapper

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.database.model.HighlightEntity
import nl.rhaydus.softcover.core.database.model.ReadingLogEntryEntity
import nl.rhaydus.softcover.core.database.model.ReadingSessionEntity
import nl.rhaydus.softcover.core.domain.model.ReadingSession
import nl.rhaydus.softcover.core.personal.domain.model.Highlight
import nl.rhaydus.softcover.core.personal.domain.model.ReadingLogEntry

private fun String.toInstantOrEpoch(): Instant =
    runCatching { Instant.parse(this) }.getOrDefault(Instant.fromEpochMilliseconds(0))

private fun String?.toInstantOrNull(): Instant? =
    this?.let { runCatching { Instant.parse(it) }.getOrNull() }

private fun String?.toLocalDateOrNull(): LocalDate? =
    this?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

internal fun HighlightEntity.toDomain(): Highlight = Highlight(
    id = id,
    bookId = bookId,
    quote = quote,
    page = page,
    note = note,
    createdAt = createdAt.toInstantOrEpoch(),
)

internal fun Highlight.toEntity(): HighlightEntity = HighlightEntity(
    id = id,
    bookId = bookId,
    quote = quote,
    page = page,
    note = note,
    createdAt = createdAt.toString(),
)

internal fun ReadingSessionEntity.toDomain(): ReadingSession = ReadingSession(
    id = id,
    bookId = bookId,
    startedAt = startedAt.toInstantOrEpoch(),
    endedAt = endedAt?.toInstantOrEpoch(),
    startPage = startPage,
    endPage = endPage,
    startSeconds = startSeconds,
    endSeconds = endSeconds,
    pausedSeconds = pausedSeconds,
    lastPausedAt = lastPausedAt.toInstantOrNull(),
)

internal fun ReadingSession.toEntity(): ReadingSessionEntity = ReadingSessionEntity(
    id = id,
    bookId = bookId,
    startedAt = startedAt.toString(),
    endedAt = endedAt?.toString(),
    startPage = startPage,
    endPage = endPage,
    startSeconds = startSeconds,
    endSeconds = endSeconds,
    pausedSeconds = pausedSeconds,
    lastPausedAt = lastPausedAt?.toString(),
)

internal fun ReadingLogEntryEntity.toDomain(): ReadingLogEntry = ReadingLogEntry(
    id = id,
    bookId = bookId,
    startedAt = startedAt.toLocalDateOrNull(),
    finishedAt = finishedAt.toLocalDateOrNull(),
    rating = rating,
    note = note,
    createdAt = createdAt.toInstantOrEpoch(),
)

internal fun ReadingLogEntry.toEntity(): ReadingLogEntryEntity = ReadingLogEntryEntity(
    id = id,
    bookId = bookId,
    startedAt = startedAt?.toString(),
    finishedAt = finishedAt?.toString(),
    rating = rating,
    note = note,
    createdAt = createdAt.toString(),
)
