package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissScanEditionBannerClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnUpdateToScannedEditionClickAction

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ScanEditionUpdateBanner(
    isUpdating: Boolean,
    runAction: (BookDetailAction) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(
                start = 20.dp,
                top = 16.dp,
                end = 8.dp,
                bottom = 16.dp,
            ),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ALREADY ON YOUR SHELVES",
                    style = MaterialTheme.editorialTypography.eyebrowSmall,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "You've added a different edition of this book. Update it to the one you scanned?",
                    style = MaterialTheme.editorialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isUpdating) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularWavyProgressIndicator(modifier = Modifier.size(20.dp))

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "Updating edition…",
                            style = MaterialTheme.editorialTypography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    RhaydusButton(
                        label = "Update edition",
                        style = ButtonStyle.TONAL,
                        size = ButtonSize.S,
                        onClick = { runAction(OnUpdateToScannedEditionClickAction()) },
                    )
                }
            }

            IconButton(
                onClick = { runAction(OnDismissScanEditionBannerClickAction()) },
                modifier = Modifier.size(32.dp),
            ) {
                val dismissIcon = drawableIconResource(
                    icon = SoftcoverIcon.Close,
                    contentDescription = "Dismiss",
                )

                Icon(
                    painter = dismissIcon.getIconPainter(),
                    contentDescription = dismissIcon.contentDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
