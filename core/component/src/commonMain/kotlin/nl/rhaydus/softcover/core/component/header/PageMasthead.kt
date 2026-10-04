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
 * trailing the title column (e.g. [MastheadRefreshAction]).
 */
@Composable
fun PageMasthead(
    model: PageMastheadUiModel,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
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
                style = when (model.size) {
                    PageMastheadSize.Regular -> MaterialTheme.editorialTypography.pageTitle
                    PageMastheadSize.Compact -> MaterialTheme.editorialTypography.headlineMedium
                },
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.semantics { heading() },
            )

            if (model.subtitle != null) {
                Spacer(
                    modifier = Modifier.height(
                        when (model.size) {
                            PageMastheadSize.Regular -> 6.dp
                            PageMastheadSize.Compact -> 4.dp
                        },
                    ),
                )

                SoftcoverText(
                    text = model.subtitle,
                    isLoading = model.isLoading,
                    standIn = model.subtitle,
                    style = when (model.size) {
                        PageMastheadSize.Regular -> MaterialTheme.editorialTypography.body
                        PageMastheadSize.Compact -> MaterialTheme.editorialTypography.bodySmall
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.widthIn(max = 300.dp),
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = spacedBy(4.dp)) {
            actions()
        }
    }
}
