package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import nl.rhaydus.designsystem.component.DesktopTooltip
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnShareBookClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

@Composable
internal fun BookShareButton(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
    iconColors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
) {
    val isKnown = state.book.takeUnless { state.loadingBookDetails } != null

    DesktopTooltip(text = "Share") {
        IconButton(
            onClick = { runAction(OnShareBookClickAction()) },
            enabled = isKnown,
            colors = iconColors,
        ) {
            val shareIcon = drawableIconResource(
                icon = SoftcoverIcon.Share,
                contentDescription = "Share",
            )

            Icon(
                painter = shareIcon.getIconPainter(),
                contentDescription = shareIcon.contentDescription,
            )
        }
    }
}
