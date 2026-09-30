package nl.rhaydus.softcover.feature.book_detail.presentation.state

internal data class ShareSheetBodyUiModel(
    val variant: ShareCardVariant,
    val shareLabel: String,
    val saveToGalleryLabel: String,
    val isBusy: Boolean,
    val hasVariantToggle: Boolean,
)
