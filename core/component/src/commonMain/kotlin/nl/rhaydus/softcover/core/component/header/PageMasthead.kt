package nl.rhaydus.softcover.core.component.header

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * Names the page, once per page: an optional no-bar eyebrow, the title, then an optional subtitle.
 * [PageMastheadUiModel.size] picks the type scale for the surface — [PageMastheadSize.Regular] for a
 * page's own masthead, [PageMastheadSize.Compact] where it shares space with other chrome. Inert — a
 * masthead is read, never tapped.
 */
@Composable
fun PageMasthead(
    model: PageMastheadUiModel,
    modifier: Modifier = Modifier,
) {
    val dimensions = PageMastheadDimensions.forSize(
        size = model.size,
        typography = MaterialTheme.editorialTypography,
    )

    Column(modifier = modifier.fillMaxWidth()) {
        if (model.eyebrow != null) {
            Text(
                text = model.eyebrow.uppercase(),
                style = MaterialTheme.editorialTypography.eyebrow,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(6.dp))
        }

        Text(
            text = model.title,
            style = dimensions.titleStyle,
            color = MaterialTheme.colorScheme.onSurface,
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
}
