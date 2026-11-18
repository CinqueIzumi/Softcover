package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import nl.rhaydus.softcover.core.domain.model.AcquisitionKind

internal const val WHERE_YOU_GOT_IT_PLACEHOLDER = "Somewhere else…"

internal fun AcquisitionKind.toKey(): String = when (this) {
    AcquisitionKind.LIBRARY -> "acquired-library"
    AcquisitionKind.BOUGHT_IN_SHOP -> "acquired-bookshop"
    AcquisitionKind.BOUGHT_ONLINE -> "acquired-online"
    AcquisitionKind.BOUGHT_SECOND_HAND -> "acquired-second-hand"
    AcquisitionKind.BOUGHT_DIGITAL -> "acquired-digital"
    AcquisitionKind.SUBSCRIPTION -> "acquired-subscription"
    AcquisitionKind.BORROWED -> "acquired-borrowed"
    AcquisitionKind.GIFT -> "acquired-gift"
}

internal fun AcquisitionKind.toLabel(): String = when (this) {
    AcquisitionKind.LIBRARY -> "Library"
    AcquisitionKind.BOUGHT_IN_SHOP -> "Bought in a bookshop"
    AcquisitionKind.BOUGHT_ONLINE -> "Bought online"
    AcquisitionKind.BOUGHT_SECOND_HAND -> "Bought second-hand"
    AcquisitionKind.BOUGHT_DIGITAL -> "Bought digital"
    AcquisitionKind.SUBSCRIPTION -> "Subscription or audio credit"
    AcquisitionKind.BORROWED -> "Borrowed"
    AcquisitionKind.GIFT -> "A gift"
}

internal fun AcquisitionKind.toGloss(): String? = when (this) {
    AcquisitionKind.BOUGHT_IN_SHOP -> "A physical copy, in person."
    AcquisitionKind.BOUGHT_ONLINE -> "A physical copy, delivered."
    AcquisitionKind.BOUGHT_DIGITAL -> "Ebook or audiobook."
    AcquisitionKind.LIBRARY,
    AcquisitionKind.BOUGHT_SECOND_HAND,
    AcquisitionKind.SUBSCRIPTION,
    AcquisitionKind.BORROWED,
    AcquisitionKind.GIFT -> null
}

internal fun String.toAcquisitionKind(): AcquisitionKind? = AcquisitionKind.entries.firstOrNull { it.toKey() == this }
