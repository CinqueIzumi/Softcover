package nl.rhaydus.softcover.core.connectivity.data.mapper

import nl.rhaydus.softcover.core.database.model.PendingQuoteWriteEntity
import nl.rhaydus.softcover.core.domain.connectivity.PendingQuoteWrite
import nl.rhaydus.softcover.core.domain.connectivity.PendingQuoteWriteKind

internal fun PendingQuoteWrite.toEntity(): PendingQuoteWriteEntity = PendingQuoteWriteEntity(
    kind = kind.name,
    quoteLocalId = quoteLocalId,
    enqueuedAt = enqueuedAt,
)

internal fun PendingQuoteWriteEntity.toPendingQuoteWrite(): PendingQuoteWrite? {
    val parsedKind: PendingQuoteWriteKind = PendingQuoteWriteKind
        .entries
        .firstOrNull { it.name == kind }
        ?: return null

    return PendingQuoteWrite(
        kind = parsedKind,
        quoteLocalId = quoteLocalId,
        enqueuedAt = enqueuedAt,
        attempts = attempts,
    )
}
