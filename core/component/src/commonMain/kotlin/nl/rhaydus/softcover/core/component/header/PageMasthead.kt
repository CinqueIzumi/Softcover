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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * Names the page, once per page: an optional no-bar eyebrow, the title, then an optional subtitle.
 * [PageMastheadUiModel.size] picks the type scale for the surface — [PageMastheadSize.Regular] for a
 * page's own masthead, [PageMastheadSize.Compact] where it shares space with other chrome. The
 * masthead text itself is inert — it is read, never tapped; [actions] is the only interactive part,
 * trailing the title column (e.g. [MastheadRefreshAction]). While [isLoading] only the subtitle masks.
 */
@Composable
fun PageMasthead(
    model: PageMastheadUiModel,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val dimensions = PageMastheadDimensions.forSize(
        size = model.size,
        typography = MaterialTheme.editorialTypography,
    )

    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(modifier = Modifier.weight(1f)) {
            if (model.eyebrow != null) {
                SectionHeaderEyebrowText(
                    eyebrow = model.eyebrow,
                    style = MaterialTheme.editorialTypography.eyebrow,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(4.dp))
            }

            SoftcoverText(
                text = model.title,
                style = dimensions.titleStyle,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.semantics { heading() },
            )

            if (model.subtitle != null) {
                Spacer(modifier = Modifier.height(dimensions.titleToSubtitleGap))

                SoftcoverText(
                    text = model.subtitle,
                    isLoading = isLoading,
                    standIn = model.subtitle,
                    style = dimensions.subtitleStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.widthIn(max = SUBTITLE_MAX_WIDTH),
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = spacedBy(4.dp)) {
            actions()
        }
    }
}
