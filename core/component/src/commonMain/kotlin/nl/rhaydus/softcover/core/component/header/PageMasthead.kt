package nl.rhaydus.softcover.core.component.header

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.statistic.StatNumber
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
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

                Spacer(modifier = Modifier.height(Spacing.xxs))
            }

            SoftcoverText(
                text = model.title,
                standIn = "Page title",
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
                            PageMastheadSize.Regular -> Spacing.xs
                            PageMastheadSize.Compact -> Spacing.xxs
                        },
                    ),
                )

                MastheadSubtitle(
                    model = model,
                    modifier = Modifier.widthIn(max = 300.dp),
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = spacedBy(Spacing.xxs)) {
            actions()
        }
    }
}

@Composable
fun MastheadSubtitle(
    model: PageMastheadUiModel,
    modifier: Modifier = Modifier,
) {
    val subtitle = model.subtitle ?: return

    when (subtitle) {
        is PageMastheadSubtitle.Text -> SoftcoverText(
            text = subtitle.text,
            isLoading = model.isLoading,
            standIn = subtitle.text,
            style = when (model.size) {
                PageMastheadSize.Regular -> MaterialTheme.editorialTypography.body
                PageMastheadSize.Compact -> MaterialTheme.editorialTypography.bodySmall
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )

        is PageMastheadSubtitle.Counts -> CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
        ) {
            Row(modifier = modifier) {
                StatNumber(
                    model = subtitle.titles,
                    modifier = Modifier.alignByBaseline(),
                )

                if (subtitle.pages != null) {
                    StatNumber(
                        model = subtitle.pages,
                        modifier = Modifier.alignByBaseline(),
                    )
                }
            }
        }
    }
}
