package nl.rhaydus.softcover.core.corpus.data.mapper

import nl.rhaydus.softcover.core.database.model.BookCopyEntity
import nl.rhaydus.softcover.core.domain.model.AcquisitionKind
import nl.rhaydus.softcover.core.domain.model.AcquisitionSource
import nl.rhaydus.softcover.core.domain.model.BookCopy
import nl.rhaydus.softcover.core.domain.model.PricePaid

// Stored names are written out, never derived from the enum name, so renaming a constant cannot
// change what is on disk. A stored name nothing maps to reads as no source.

internal fun BookCopyEntity.toDomain(): BookCopy = BookCopy(
    bookId = bookId,
    source = customSource?.takeIf { it.isNotBlank() }?.let { AcquisitionSource.Other(text = it) }
        ?: source.toAcquisitionKind()?.let { AcquisitionSource.Preset(kind = it) },
    price = priceThousandths?.let { amount ->
        priceCurrency?.let { currency ->
            PricePaid(
                amountThousandths = amount,
                currencyCode = currency,
            )
        }
    },
)

internal fun BookCopy.toEntity(updatedAt: Long): BookCopyEntity = BookCopyEntity(
    bookId = bookId,
    source = (source as? AcquisitionSource.Preset)?.kind?.toStorageName(),
    customSource = (source as? AcquisitionSource.Other)?.text?.trim()?.takeIf { it.isNotEmpty() },
    updatedAt = updatedAt,
    priceThousandths = price?.amountThousandths,
    priceCurrency = price?.currencyCode,
)

private fun AcquisitionKind.toStorageName(): String = when (this) {
    AcquisitionKind.LIBRARY -> "library"
    AcquisitionKind.BOUGHT_IN_SHOP -> "bought_in_shop"
    AcquisitionKind.BOUGHT_ONLINE -> "bought_online"
    AcquisitionKind.BOUGHT_SECOND_HAND -> "bought_second_hand"
    AcquisitionKind.BOUGHT_DIGITAL -> "bought_digital"
    AcquisitionKind.SUBSCRIPTION -> "subscription"
    AcquisitionKind.BORROWED -> "borrowed"
    AcquisitionKind.GIFT -> "gift"
}

private fun String?.toAcquisitionKind(): AcquisitionKind? =
    AcquisitionKind.entries.firstOrNull { it.toStorageName() == this }
