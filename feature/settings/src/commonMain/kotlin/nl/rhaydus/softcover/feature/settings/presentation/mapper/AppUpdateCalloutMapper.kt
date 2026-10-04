package nl.rhaydus.softcover.feature.settings.presentation.mapper

import nl.rhaydus.softcover.core.component.callout.CalloutActionUiModel
import nl.rhaydus.softcover.core.component.callout.CalloutUiModel
import nl.rhaydus.softcover.core.component.callout.CalloutVariant
import nl.rhaydus.softcover.core.domain.model.AppUpdateState

private const val DOWNLOADING_EYEBROW = "Downloading"
private const val DOWNLOADING_TITLE = "Bringing the update down"
private const val DOWNLOADING_BODY = "You can keep reading — it'll finish in the background."

private const val DOWNLOADED_EYEBROW = "Ready to install"
private const val DOWNLOADED_TITLE = "The update is ready"
private const val DOWNLOADED_BODY = "Downloaded and waiting. Softcover will restart once."
private const val DOWNLOADED_ACTION = "Install & restart"

private const val FAILED_EYEBROW = "Update failed"
private const val FAILED_TITLE = "That didn't go through"
private const val FAILED_BODY = "Tap to try again."
private const val FAILED_ACTION = "Try again"

private const val AVAILABLE_EYEBROW = "Update available"
private const val AVAILABLE_TITLE = "A new version is ready"
private const val AVAILABLE_BODY = "A newer Softcover is ready whenever you are."
private const val AVAILABLE_ACTION = "Download update"

/**
 * The Settings app-update callout, or `null` while no update is in play. The copy names no version
 * and the download shows no percentage: the client has no reliable version string or progress to
 * report.
 */
internal fun appUpdateCalloutFor(state: AppUpdateState): CalloutUiModel? = when (state) {
    AppUpdateState.Idle -> null

    AppUpdateState.Downloading -> appUpdateCallout(
        eyebrow = DOWNLOADING_EYEBROW,
        title = DOWNLOADING_TITLE,
        body = DOWNLOADING_BODY,
        callToAction = CalloutActionUiModel.Progress,
    )

    AppUpdateState.Downloaded -> appUpdateCallout(
        eyebrow = DOWNLOADED_EYEBROW,
        title = DOWNLOADED_TITLE,
        body = DOWNLOADED_BODY,
        callToAction = CalloutActionUiModel.Button(label = DOWNLOADED_ACTION),
    )

    AppUpdateState.Failed -> appUpdateCallout(
        eyebrow = FAILED_EYEBROW,
        title = FAILED_TITLE,
        body = FAILED_BODY,
        callToAction = CalloutActionUiModel.Button(label = FAILED_ACTION),
    )

    AppUpdateState.Available -> appUpdateCallout(
        eyebrow = AVAILABLE_EYEBROW,
        title = AVAILABLE_TITLE,
        body = AVAILABLE_BODY,
        callToAction = CalloutActionUiModel.Button(label = AVAILABLE_ACTION),
    )
}

private fun appUpdateCallout(
    eyebrow: String,
    title: String,
    body: String,
    callToAction: CalloutActionUiModel,
) = CalloutUiModel(
    variant = CalloutVariant.Highlight(title = title),
    eyebrow = eyebrow,
    body = body,
    callToAction = callToAction,
    dismissible = false,
)
