package nl.rhaydus.softcover.feature.book_detail.presentation.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.core.component.chip.ChipFlow
import nl.rhaydus.softcover.core.component.control.SegmentedControl
import nl.rhaydus.softcover.core.component.control.SegmentedControlEvent
import nl.rhaydus.softcover.core.component.share.ShareCardSheetContent
import nl.rhaydus.softcover.core.component.share.ShareCardSheetEvent
import nl.rhaydus.softcover.core.component.state.RevealedWhenPresent
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
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

                Spacer(modifier = Modifier.height(Spacing.xl))
            }
        },
        belowCard = {
            if (model.options.isEmpty.not()) {
                Spacer(modifier = Modifier.height(Spacing.xl))

                ChipFlow(
                    set = model.options,
                    onEvent = { _, option ->
                        onEvent(ShareSheetEvent.OptionClicked(option = option))
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }

            val revealRequester = remember { BringIntoViewRequester() }
            val coroutineScope = rememberCoroutineScope()
            var revealedHeight by remember { mutableIntStateOf(0) }

            // Each frame of growth asks the sheet's scroll to follow, so overflow scrolls in step with the reveal.
            RevealedWhenPresent(
                value = model.tagPicker?.takeIf { it.expanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(revealRequester)
                    .onSizeChanged { size ->
                        val grew = size.height > revealedHeight

                        revealedHeight = size.height

                        if (grew) {
                            coroutineScope.launch { revealRequester.bringIntoView() }
                        }
                    },
            ) { picker ->
                ShareTagPicker(
                    model = picker,
                    onEvent = onEvent,
                )
            }
        },
    )
}
