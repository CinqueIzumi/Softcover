package nl.rhaydus.softcover.feature.book_detail.presentation.state

import nl.rhaydus.softcover.core.component.share.ShareCardSheetEvent

internal sealed interface ShareSheetEvent {
    data class VariantSelected(val variant: ShareCardVariant) : ShareSheetEvent

    data class OptionClicked(val option: ShareSheetOption) : ShareSheetEvent

    data class TagToggled(
        val variant: ShareCardVariant,
        val tag: String,
    ) : ShareSheetEvent

    data class CaptureReported(val captureEvent: ShareCardSheetEvent) : ShareSheetEvent

    data object Dismissed : ShareSheetEvent
}
