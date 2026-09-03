package nl.rhaydus.softcover.core.uibinding.richtext

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.richtext.RichTextParagraph
import nl.rhaydus.softcover.core.component.richtext.RichTextRun
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.ReviewParagraph
import nl.rhaydus.softcover.core.domain.model.ReviewRun

/**
 * The review body a feature holds, as the rich text the library renders.
 *
 * Promoted straight to `:core:uibinding` rather than starting feature-local (R6's usual path) because
 * it lands with two consumers already: `feature:book_detail`'s verdict block and share sheet, and
 * `feature:reading`'s verdict sheet.
 */
fun ReviewDocument.toRichTextUiModel(): RichTextUiModel = RichTextUiModel(
    paragraphs = paragraphs.map { paragraph ->
        RichTextParagraph(
            runs = paragraph.runs.map { run ->
                RichTextRun(
                    text = run.text,
                    bold = run.bold,
                    italic = run.italic,
                    spoiler = run.spoiler,
                )
            }.toImmutableList(),
        )
    }.toImmutableList(),
)

/**
 * The inverse — rich text the reader has just edited, as the review body the app persists.
 *
 * **This direction is why the mapping is a pair rather than a one-way adapter.** Most UI models are
 * write-once: a feature maps domain -> UI and the component renders it. The verdict sheet is an
 * *editor*, so the edited model has to travel back out through `onSave` and be persisted. A one-way
 * mapper would have forced the sheet to keep emitting `ReviewDocument`, which is exactly the domain
 * type R4 exists to keep out of the library.
 *
 * The pair is lossless in both directions: the two shapes carry the same four fields per run, and the
 * only asymmetry is the collection type (R3 requires `ImmutableList` on the UI side). Round-tripping
 * is covered by `RichTextMapperTest`.
 */
fun RichTextUiModel.toReviewDocument(): ReviewDocument = ReviewDocument(
    paragraphs = paragraphs.map { paragraph ->
        ReviewParagraph(
            runs = paragraph.runs.map { run ->
                ReviewRun(
                    text = run.text,
                    bold = run.bold,
                    italic = run.italic,
                    spoiler = run.spoiler,
                )
            },
        )
    },
)
