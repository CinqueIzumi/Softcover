package nl.rhaydus.softcover.core.component.share

/** The platform-specific wording of the share sheet's actions and their outcomes. */
internal data class ShareCaptureCopy(
    val saveLabel: String,
    val shareLabel: String,
    val sharingLabel: String,
    val savedMessage: String,
    val saveFailedMessage: String,
)

internal expect val shareCaptureCopy: ShareCaptureCopy
