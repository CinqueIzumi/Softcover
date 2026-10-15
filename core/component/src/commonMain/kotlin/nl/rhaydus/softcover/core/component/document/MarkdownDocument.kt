package nl.rhaydus.softcover.core.component.document

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A parsed markdown document as an editorial spread rather than raw markdown: eyebrow-and-headline
 * headings through the Section header register, consecutive list entries as one hanging-indent list, a
 * hairline rule for a thematic break and a quiet accent-bar aside for a block quote. A tapped link is
 * reported as [MarkdownDocumentEvent.LinkClicked]; opening it is the host's job.
 */
@Composable
fun MarkdownDocument(
    model: MarkdownDocumentUiModel,
    onEvent: (MarkdownDocumentEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        model.blocks.forEachIndexed { index, block ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(block.leadingSpacing()))
            }

            MarkdownDocumentBlock(
                block = block,
                isLoading = model.isLoading,
                onEvent = onEvent,
            )
        }
    }
}
