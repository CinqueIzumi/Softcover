package nl.rhaydus.softcover.feature.profile.presentation.collector

import nl.rhaydus.softcover.core.component.share.ReadingLifeShareCardUiModel
import nl.rhaydus.softcover.core.component.share.ShareCaptureProgress

internal data class ShareSheetSnapshot(
    val card: ReadingLifeShareCardUiModel?,
    val progress: ShareCaptureProgress,
)
