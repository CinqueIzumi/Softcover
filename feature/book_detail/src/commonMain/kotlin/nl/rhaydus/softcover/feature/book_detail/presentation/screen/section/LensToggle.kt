package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.control.SegmentedControl
import nl.rhaydus.softcover.core.component.control.SegmentedControlEvent
import nl.rhaydus.softcover.core.component.control.SegmentedControlUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLens

/**
 * The sticky lens toggle: a full-width segmented pill that switches the sections below between "Yours"
 * and "The Book". The caller composes this as a `stickyHeader` under the top bar.
 */
@Composable
internal fun LensToggle(
    model: SegmentedControlUiModel<BookDetailLens>,
    onLensSelected: (BookDetailLens) -> Unit,
    modifier: Modifier = Modifier,
) {
    // The caller composes this as a `stickyHeader`, so the root paints an opaque, edge-to-edge
    // page-background layer first — otherwise content scrolling underneath would show through the
    // padded pill's own margin while the header is pinned.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
    ) {
        SegmentedControl(
            model = model,
            onEvent = { event ->
                when (event) {
                    is SegmentedControlEvent.Selected -> onLensSelected(event.key)
                }
            },
            modifier = Modifier.padding(
                horizontal = Spacing.md,
                vertical = Spacing.xs,
            ),
        )
    }
}
