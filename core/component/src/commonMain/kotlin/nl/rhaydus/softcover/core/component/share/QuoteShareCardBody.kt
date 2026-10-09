package nl.rhaydus.softcover.core.component.share

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@Composable
internal fun QuoteShareCardBody(content: QuoteShareCardUiModel) {
    Box(modifier = Modifier.fillMaxWidth()) {
        SoftcoverText(
            text = "“",
            style = MaterialTheme.editorialTypography.quoteGlyph,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
            modifier = Modifier.align(Alignment.TopStart),
        )

        Column(modifier = Modifier.padding(top = Spacing.x4l)) {
            SoftcoverText(
                text = content.quote,
                style = MaterialTheme.editorialTypography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            val byline = buildString {
                append(content.sourceAuthor.uppercase())
                append(" — ")
                append(content.sourceTitle.uppercase())

                if (content.page != null) {
                    append(" · p. ")
                    append(content.page)
                }
            }

            SoftcoverText(
                text = byline,
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
