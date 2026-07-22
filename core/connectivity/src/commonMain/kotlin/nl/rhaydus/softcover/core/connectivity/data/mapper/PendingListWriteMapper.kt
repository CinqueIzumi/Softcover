package nl.rhaydus.softcover.core.connectivity.data.mapper

import nl.rhaydus.softcover.core.database.model.PendingListWriteEntity
import nl.rhaydus.softcover.core.domain.connectivity.PendingListWrite
import nl.rhaydus.softcover.core.domain.connectivity.PendingListWriteKind
import nl.rhaydus.softcover.core.domain.model.PrivacySetting

private const val ORDERED_IDS_SEPARATOR: String = ","

internal fun PendingListWrite.toEntity(): PendingListWriteEntity = PendingListWriteEntity(
    kind = kind.name,
    listId = listId,
    listName = listName,
    bookId = bookId,
    editionId = editionId,
    listBookId = listBookId,
    startPosition = startPosition,
    orderedListBookIdsCsv = orderedListBookIds?.joinToString(separator = ORDERED_IDS_SEPARATOR),
    enqueuedAt = enqueuedAt,
    privacySettingId = privacy?.code,
)

internal fun PendingListWriteEntity.toPendingListWrite(): PendingListWrite? {
    val parsedKind: PendingListWriteKind = PendingListWriteKind
        .entries
        .firstOrNull { it.name == kind }
        ?: return null

    return PendingListWrite(
        kind = parsedKind,
        listId = listId,
        listName = listName,
        bookId = bookId,
        editionId = editionId,
        listBookId = listBookId,
        startPosition = startPosition,
        orderedListBookIds = orderedListBookIdsCsv
            ?.takeIf { it.isNotBlank() }
            ?.split(ORDERED_IDS_SEPARATOR)
            ?.mapNotNull { it.trim().toIntOrNull() },
        enqueuedAt = enqueuedAt,
        privacy = PrivacySetting.entries.firstOrNull { it.code == privacySettingId },
    )
}
