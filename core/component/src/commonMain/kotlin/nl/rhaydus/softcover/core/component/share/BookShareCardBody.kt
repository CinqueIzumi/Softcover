package nl.rhaydus.softcover.core.component.share

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.OverMediaInk
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@Composable
internal fun BookShareCardBody(model: BookShareCardUiModel) {
    if (model.statsLine != null) {
        SoftcoverText(
            text = model.statsLine,
            style = MaterialTheme.editorialTypography.bodySmall,
            color = OverMediaInk.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
        )
    }

    if (model.description != null && model.description.isNotBlank()) {
        GlassPanel(modifier = Modifier.fillMaxWidth()) {
            SoftcoverText(
                text = model.description,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = OverMediaInk.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                maxLines = when (model.format) {
                    ShareCardFormat.Story -> 5
                    ShareCardFormat.FullText -> Int.MAX_VALUE
                },
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    ShareCardTags(
        tags = model.tags,
        format = model.format,
    )
}
