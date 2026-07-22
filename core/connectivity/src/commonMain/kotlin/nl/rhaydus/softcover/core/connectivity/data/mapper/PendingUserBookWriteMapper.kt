package nl.rhaydus.softcover.core.connectivity.data.mapper

import nl.rhaydus.softcover.core.database.model.PendingUserBookWriteEntity
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWrite
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWriteKind

internal fun PendingUserBookWrite.toEntity(): PendingUserBookWriteEntity = PendingUserBookWriteEntity(
    kind = kind.name,
    userBookId = userBookId,
    userBookReadId = userBookReadId,
    bookId = bookId,
    editionId = editionId,
    progressPages = progressPages,
    progressSeconds = progressSeconds,
    startedAt = startedAt,
    finishedAt = finishedAt,
    rating = rating,
    reviewSlateJson = reviewSlateJson,
    reviewHasSpoilers = reviewHasSpoilers,
    enqueuedAt = enqueuedAt,
    actionAt = actionAt,
)

/** Null when the persisted `kind` names no known write — a row written by a build that knew more. */
internal fun PendingUserBookWriteEntity.toPendingUserBookWrite(): PendingUserBookWrite? {
    val parsedKind: PendingUserBookWriteKind = PendingUserBookWriteKind
        .entries
        .firstOrNull { it.name == kind }
        ?: return null

    return PendingUserBookWrite(
        kind = parsedKind,
        userBookId = userBookId,
        userBookReadId = userBookReadId,
        bookId = bookId,
        editionId = editionId,
        progressPages = progressPages,
        progressSeconds = progressSeconds,
        startedAt = startedAt,
        finishedAt = finishedAt,
        rating = rating,
        reviewSlateJson = reviewSlateJson,
        reviewHasSpoilers = reviewHasSpoilers,
        enqueuedAt = enqueuedAt,
        actionAt = actionAt,
    )
}
