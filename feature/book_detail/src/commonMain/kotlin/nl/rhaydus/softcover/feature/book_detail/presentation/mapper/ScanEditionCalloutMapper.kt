package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import nl.rhaydus.softcover.core.component.callout.CalloutActionUiModel
import nl.rhaydus.softcover.core.component.callout.CalloutEvent
import nl.rhaydus.softcover.core.component.callout.CalloutUiModel
import nl.rhaydus.softcover.core.component.callout.CalloutVariant
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissScanEditionBannerClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnUpdateToScannedEditionClickAction

private const val SCAN_EDITION_EYEBROW = "Already on your shelves"
private const val SCAN_EDITION_BODY = "You've added a different edition of this book. Update it to the one you scanned?"
private const val UPDATE_EDITION_LABEL = "Update edition"
private const val UPDATING_EDITION_LABEL = "Updating edition…"

internal fun scanEditionCallout(
    scannedEditionId: Int?,
    shelvedEditionId: Int?,
    dismissed: Boolean,
    isUpdating: Boolean,
): CalloutUiModel? {
    if (scannedEditionId == null || shelvedEditionId == null) return null

    if (dismissed || shelvedEditionId == scannedEditionId) return null

    return CalloutUiModel(
        variant = CalloutVariant.Neutral(),
        eyebrow = SCAN_EDITION_EYEBROW,
        body = SCAN_EDITION_BODY,
        callToAction = if (isUpdating) {
            CalloutActionUiModel.Busy(label = UPDATING_EDITION_LABEL)
        } else {
            CalloutActionUiModel.Button(label = UPDATE_EDITION_LABEL)
        },
        dismissible = true,
    )
}

internal fun bookDetailActionFor(event: CalloutEvent): BookDetailAction = when (event) {
    CalloutEvent.ActionClicked -> OnUpdateToScannedEditionClickAction()
    CalloutEvent.DismissClicked -> OnDismissScanEditionBannerClickAction()
}
