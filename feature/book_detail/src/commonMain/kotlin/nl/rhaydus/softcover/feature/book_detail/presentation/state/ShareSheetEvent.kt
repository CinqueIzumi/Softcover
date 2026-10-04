package nl.rhaydus.softcover.feature.book_detail.presentation.state

internal sealed interface ShareSheetEvent {
    data class VariantSelected(val variant: ShareCardVariant) : ShareSheetEvent

    data object Dismissed : ShareSheetEvent
}
