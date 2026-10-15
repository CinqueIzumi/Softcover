package nl.rhaydus.softcover.core.component.document

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * An aside to the reader, not a citation - a quiet accent bar rather than a boxed card. [IntrinsicSize.Min]
 * lets the bar's `fillMaxHeight` match the text's measured height instead of collapsing to zero, since
 * the [Row] itself has no height of its own to fill.
 */
@Composable
internal fun MarkdownDocumentBlockQuote(
    spans: ImmutableList<MarkdownDocumentSpanUiModel>,
    onEvent: (MarkdownDocumentEvent) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outlineVariant),
        )

        Spacer(modifier = Modifier.width(Spacing.md))

        SoftcoverText(
            text = markdownAnnotatedString(
                spans = spans,
                onEvent = onEvent,
            ),
            style = MaterialTheme.editorialTypography.body.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
        )
    }
}
