package nl.rhaydus.softcover.feature.book_detail.presentation.state

import nl.rhaydus.softcover.core.component.control.SegmentedControlUiModel

internal data class ShareSheetBodyUiModel(
    val displayName: String,
    val shareLabel: String,
    val saveToGalleryLabel: String,
    val isBusy: Boolean,
    val variantToggle: SegmentedControlUiModel<ShareCardVariant>?,
)
