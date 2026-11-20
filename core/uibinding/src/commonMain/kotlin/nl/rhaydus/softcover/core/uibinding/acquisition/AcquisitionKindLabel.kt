package nl.rhaydus.softcover.core.uibinding.acquisition

import nl.rhaydus.softcover.core.domain.model.AcquisitionKind

fun AcquisitionKind.toLabel(): String = when (this) {
    AcquisitionKind.LIBRARY -> "Library"
    AcquisitionKind.BOUGHT_IN_SHOP -> "Bought in a bookshop"
    AcquisitionKind.BOUGHT_ONLINE -> "Bought online"
    AcquisitionKind.BOUGHT_SECOND_HAND -> "Bought second-hand"
    AcquisitionKind.BOUGHT_DIGITAL -> "Bought digital"
    AcquisitionKind.SUBSCRIPTION -> "Subscription or audio credit"
    AcquisitionKind.BORROWED -> "Borrowed"
    AcquisitionKind.GIFT -> "A gift"
}
