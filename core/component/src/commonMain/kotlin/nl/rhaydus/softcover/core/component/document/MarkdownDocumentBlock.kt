package nl.rhaydus.softcover.core.component.document

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@Composable
internal fun MarkdownDocumentBlock(
    block: MarkdownDocumentBlockUiModel,
    isLoading: Boolean,
    onEvent: (MarkdownDocumentEvent) -> Unit,
) {
    when (block) {
        is MarkdownDocumentBlockUiModel.SectionHeading -> SectionHeader(model = block.header)

        is MarkdownDocumentBlockUiModel.PlainHeading -> SoftcoverText(
            text = block.text,
            style = MaterialTheme.editorialTypography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            isLoading = isLoading,
        )

        is MarkdownDocumentBlockUiModel.ListGroup -> MarkdownDocumentList(
            items = block.items,
            isLoading = isLoading,
            onEvent = onEvent,
        )

        is MarkdownDocumentBlockUiModel.Paragraph -> SoftcoverText(
            text = markdownAnnotatedString(
                spans = block.spans,
                onEvent = onEvent,
            ),
            style = MaterialTheme.editorialTypography.review.copy(color = MaterialTheme.colorScheme.onSurface),
        )

        is MarkdownDocumentBlockUiModel.BlockQuote -> MarkdownDocumentBlockQuote(
            spans = block.spans,
            onEvent = onEvent,
        )

        MarkdownDocumentBlockUiModel.ThematicBreak -> Divider()
    }
}

internal fun MarkdownDocumentBlockUiModel.leadingSpacing() = when (this) {
    is MarkdownDocumentBlockUiModel.ListGroup -> Spacing.sm
    is MarkdownDocumentBlockUiModel.SectionHeading, is MarkdownDocumentBlockUiModel.PlainHeading -> Spacing.section
    is MarkdownDocumentBlockUiModel.ThematicBreak -> Spacing.xxl
    is MarkdownDocumentBlockUiModel.BlockQuote -> Spacing.lg
    is MarkdownDocumentBlockUiModel.Paragraph -> Spacing.md
}
