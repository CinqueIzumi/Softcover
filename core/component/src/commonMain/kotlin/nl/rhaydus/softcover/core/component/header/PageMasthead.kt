package nl.rhaydus.softcover.core.component.header

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * Names the page, once per page: an optional no-bar eyebrow, the title, then an optional subtitle.
 * [PageMastheadUiModel.size] picks the type scale for the surface — [PageMastheadSize.Regular] for a
 * page's own masthead, [PageMastheadSize.Compact] where it shares space with other chrome. The
 * masthead text itself is inert — it is read, never tapped; [actions] is the only interactive part,
 * trailing the title column (e.g. [MastheadRefreshAction]).
 */
@Composable
fun PageMasthead(
    model: PageMastheadUiModel,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val dimensions = PageMastheadDimensions.forSize(
        size = model.size,
        typography = MaterialTheme.editorialTypography,
    )

    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(modifier = Modifier.weight(1f)) {
            if (model.eyebrow != null) {
                Text(
                    text = model.eyebrow.uppercase(),
                    style = MaterialTheme.editorialTypography.eyebrow,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = model.title,
                style = dimensions.titleStyle,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (model.subtitle != null) {
                Spacer(modifier = Modifier.height(dimensions.titleToSubtitleGap))

                Text(
                    text = model.subtitle,
                    style = dimensions.subtitleStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.widthIn(max = dimensions.subtitleMaxWidth),
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = spacedBy(4.dp)) {
            actions()
        }
    }
}
