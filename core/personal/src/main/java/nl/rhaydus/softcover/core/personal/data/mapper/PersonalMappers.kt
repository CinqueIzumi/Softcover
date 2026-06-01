package nl.rhaydus.softcover.core.personal.data.mapper

import nl.rhaydus.softcover.core.data.database.model.HighlightEntity
import nl.rhaydus.softcover.core.data.database.model.ReadingLogEntryEntity
import nl.rhaydus.softcover.core.data.database.model.ReadingSessionEntity
import nl.rhaydus.softcover.core.domain.model.ReadingSession
import nl.rhaydus.softcover.core.personal.domain.model.Highlight
import nl.rhaydus.softcover.core.personal.domain.model.ReadingLogEntry
import java.time.Instant
import java.time.LocalDate

private fun String.toInstantOrEpoch(): Instant =
    runCatching { Instant.parse(this) }.getOrDefault(Instant.EPOCH)

private fun String?.toInstantOrNull(): Instant? =
    this?.let { runCatching { Instant.parse(it) }.getOrNull() }

private fun String?.toLocalDateOrNull(): LocalDate? =
    this?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

fun HighlightEntity.toDomain(): Highlight = Highlight(
    id = id,
    bookId = bookId,
    quote = quote,
    page = page,
    note = note,
    createdAt = createdAt.toInstantOrEpoch(),
)

fun Highlight.toEntity(): HighlightEntity = HighlightEntity(
    id = id,
    bookId = bookId,
    quote = quote,
    page = page,
    note = note,
    createdAt = createdAt.toString(),
)

fun ReadingSessionEntity.toDomain(): ReadingSession = ReadingSession(
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

fun ReadingSession.toEntity(): ReadingSessionEntity = ReadingSessionEntity(
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

fun ReadingLogEntryEntity.toDomain(): ReadingLogEntry = ReadingLogEntry(
    id = id,
    bookId = bookId,
    startedAt = startedAt.toLocalDateOrNull(),
    finishedAt = finishedAt.toLocalDateOrNull(),
    rating = rating,
    note = note,
    createdAt = createdAt.toInstantOrEpoch(),
)

fun ReadingLogEntry.toEntity(): ReadingLogEntryEntity = ReadingLogEntryEntity(
    id = id,
    bookId = bookId,
    startedAt = startedAt?.toString(),
    finishedAt = finishedAt?.toString(),
    rating = rating,
    note = note,
    createdAt = createdAt.toString(),
)
