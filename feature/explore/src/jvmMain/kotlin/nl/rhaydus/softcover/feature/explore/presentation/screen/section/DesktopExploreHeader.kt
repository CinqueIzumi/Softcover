package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.DesktopTooltip
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.component.header.MastheadRefreshAction
import nl.rhaydus.softcover.core.component.header.MastheadRefreshEvent
import nl.rhaydus.softcover.core.component.header.MastheadRefreshUiModel
import nl.rhaydus.softcover.core.component.header.PageMasthead
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRefreshAction

@Composable
internal fun DesktopExploreHeader(
    masthead: PageMastheadUiModel,
    refresh: MastheadRefreshUiModel,
    runAction: (ExploreAction) -> Unit,
    onScanClick: () -> Unit,
) {
    PageMasthead(
        model = masthead,
        modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
    ) {
        MastheadRefreshAction(
            model = refresh,
            onEvent = {
                when (it) {
                    MastheadRefreshEvent.Refresh -> runAction(OnRefreshAction)
                }
            },
        )

        DesktopTooltip(text = "Scan a barcode") {
            IconButton(
                onClick = onScanClick,
                modifier = Modifier.pointerHandCursor(),
            ) {
                val scanIcon = drawableIconResource(
                    icon = SoftcoverIcon.BarcodeScanner,
                    contentDescription = "Scan a book's barcode",
                )

                Icon(
                    painter = scanIcon.getIconPainter(),
                    contentDescription = scanIcon.contentDescription,
                )
            }
        }
    }
}
