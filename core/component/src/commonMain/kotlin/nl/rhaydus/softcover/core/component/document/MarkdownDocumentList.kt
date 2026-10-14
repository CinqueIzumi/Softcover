package nl.rhaydus.softcover.core.component.document

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.state.skeletonMask
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@Composable
internal fun MarkdownDocumentList(
    items: ImmutableList<MarkdownDocumentListItemUiModel>,
    isLoading: Boolean,
    onEvent: (MarkdownDocumentEvent) -> Unit,
) {
    Column {
        items.forEachIndexed { index, item ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(Spacing.xs))
            }

            SoftcoverText(
                text = markdownAnnotatedString(
                    spans = item.spans,
                    onEvent = onEvent,
                    leadingMarker = item.marker,
                ),
                modifier = Modifier.skeletonMask(visible = isLoading),
                style = MaterialTheme.editorialTypography.review.copy(color = MaterialTheme.colorScheme.onSurface),
            )
        }
    }
}
