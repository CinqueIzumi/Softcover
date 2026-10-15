package nl.rhaydus.softcover.core.personal.data.mapper

import nl.rhaydus.softcover.core.database.model.HighlightEntity
import nl.rhaydus.softcover.core.database.model.ReadingSessionEntity
import nl.rhaydus.softcover.core.domain.model.ReadingSession
import nl.rhaydus.softcover.core.personal.domain.model.Highlight
import kotlin.time.Instant

private fun String.toInstantOrEpoch(): Instant =
    runCatching { Instant.parse(this) }.getOrDefault(Instant.fromEpochMilliseconds(0))

private fun String?.toInstantOrNull(): Instant? =
    this?.let { runCatching { Instant.parse(it) }.getOrNull() }

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
