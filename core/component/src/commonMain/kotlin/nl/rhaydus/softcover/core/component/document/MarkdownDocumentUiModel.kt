package nl.rhaydus.softcover.core.component.document

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel

/**
 * Everything [MarkdownDocument] renders: a document's [blocks] in reading order.
 *
 * @property isLoading Masks the list items in place and the heading text, keeping the real layout.
 */
@Immutable
data class MarkdownDocumentUiModel(
    val blocks: ImmutableList<MarkdownDocumentBlockUiModel>,
    val isLoading: Boolean = false,
) {
    companion object : UiModelPreviews<MarkdownDocumentUiModel> {
        override val previews: ImmutableMap<String, MarkdownDocumentUiModel> = persistentMapOf(
            "Release entries" to MarkdownDocumentUiModel(
                blocks = persistentListOf(
                    MarkdownDocumentBlockUiModel.SectionHeading(
                        header = SectionHeaderUiModel.Section(
                            eyebrow = "3.2.0",
                            headline = "Widgets, and a lot of small things",
                        ),
                    ),
                    MarkdownDocumentBlockUiModel.ListGroup(
                        items = persistentListOf(
                            MarkdownDocumentListItemUiModel(
                                marker = "•",
                                spans = persistentListOf(
                                    MarkdownDocumentSpanUiModel(text = "A home screen widget for "),
                                    MarkdownDocumentSpanUiModel(
                                        text = "Currently reading",
                                        bold = true,
                                    ),
                                ),
                            ),
                            MarkdownDocumentListItemUiModel(
                                marker = "•",
                                spans = persistentListOf(
                                    MarkdownDocumentSpanUiModel(
                                        text = "A long line wraps under its own text, not under the bullet, " +
                                            "so the list stays easy to scan",
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            "Prose" to MarkdownDocumentUiModel(
                blocks = persistentListOf(
                    MarkdownDocumentBlockUiModel.PlainHeading(text = "Under consideration"),
                    MarkdownDocumentBlockUiModel.Paragraph(
                        spans = persistentListOf(
                            MarkdownDocumentSpanUiModel(text = "Plain text, "),
                            MarkdownDocumentSpanUiModel(
                                text = "emphasis",
                                italic = true,
                            ),
                            MarkdownDocumentSpanUiModel(text = ", "),
                            MarkdownDocumentSpanUiModel(
                                text = "code",
                                code = true,
                            ),
                            MarkdownDocumentSpanUiModel(text = " and "),
                            MarkdownDocumentSpanUiModel(
                                text = "a link",
                                url = "https://hardcover.app",
                            ),
                            MarkdownDocumentSpanUiModel(text = "."),
                        ),
                    ),
                    MarkdownDocumentBlockUiModel.ThematicBreak,
                    MarkdownDocumentBlockUiModel.ListGroup(
                        items = persistentListOf(
                            MarkdownDocumentListItemUiModel(
                                marker = "1.",
                                spans = persistentListOf(MarkdownDocumentSpanUiModel(text = "First step")),
                            ),
                            MarkdownDocumentListItemUiModel(
                                marker = "2.",
                                spans = persistentListOf(MarkdownDocumentSpanUiModel(text = "Second step")),
                            ),
                        ),
                    ),
                    MarkdownDocumentBlockUiModel.BlockQuote(
                        spans = persistentListOf(
                            MarkdownDocumentSpanUiModel(text = "An aside to the reader, set off by a quiet bar."),
                        ),
                    ),
                ),
            ),
            "Loading" to MarkdownDocumentUiModel(
                blocks = persistentListOf(
                    MarkdownDocumentBlockUiModel.SectionHeading(
                        header = SectionHeaderUiModel.Section(
                            eyebrow = "0.0.0",
                            headline = "Entry title",
                            isLoading = true,
                        ),
                    ),
                    MarkdownDocumentBlockUiModel.ListGroup(
                        items = persistentListOf(
                            MarkdownDocumentListItemUiModel(
                                marker = "•",
                                spans = persistentListOf(
                                    MarkdownDocumentSpanUiModel(text = "A line describing what changed"),
                                ),
                            ),
                            MarkdownDocumentListItemUiModel(
                                marker = "•",
                                spans = persistentListOf(
                                    MarkdownDocumentSpanUiModel(text = "A line describing what changed"),
                                ),
                            ),
                        ),
                    ),
                ),
                isLoading = true,
            ),
        )
    }
}
