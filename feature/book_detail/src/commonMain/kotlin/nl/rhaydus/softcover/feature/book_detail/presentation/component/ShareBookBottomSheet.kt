package nl.rhaydus.softcover.feature.book_detail.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.control.SegmentedControl
import nl.rhaydus.softcover.core.component.control.SegmentedControlEvent
import nl.rhaydus.softcover.core.component.share.ShareCardSheetContent
import nl.rhaydus.softcover.core.component.share.ShareCardSheetEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShareSheetEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShareSheetUiModel

@Composable
internal fun ShareBookBottomSheet(
    model: ShareSheetUiModel,
    onEvent: (ShareSheetEvent) -> Unit,
) {
    ShareCardSheetContent(
        model = model.sheet,
        onEvent = { event ->
            when (event) {
                ShareCardSheetEvent.Dismissed -> onEvent(ShareSheetEvent.Dismissed)

                else -> onEvent(ShareSheetEvent.CaptureReported(captureEvent = event))
            }
        },
        aboveCard = {
            if (model.variantToggle != null) {
                SegmentedControl(
                    model = model.variantToggle,
                    onEvent = { event ->
                        when (event) {
                            is SegmentedControlEvent.Selected -> onEvent(
                                ShareSheetEvent.VariantSelected(variant = event.key),
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    )
}
