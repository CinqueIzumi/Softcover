package nl.rhaydus.softcover.core.domain.model

import kotlinx.serialization.Serializable

/**
 * Structured, lossless representation of a review body. Hardcover stores reviews as a Slate document —
 * a list of paragraph blocks, each holding inline text [runs][ReviewRun] that can independently carry
 * bold / italic / spoiler marks. This is the app's canonical in-memory form: it round-trips through the
 * `review_slate` field, is the single source the editor edits, and is what the display renders.
 *
 * Note the per-run [ReviewRun.spoiler] mark is distinct from the whole-review `review_has_spoilers`
 * flag — the former hides an inline span, the latter gates the entire review behind a tap-to-reveal.
 */
@Serializable
data class ReviewDocument(
    val paragraphs: List<ReviewParagraph>,
) {
    companion object {
        val EMPTY = ReviewDocument(paragraphs = emptyList())
    }
}

fun ReviewDocument.isBlank(): Boolean = paragraphs.all { paragraph ->
    paragraph.runs.all { it.text.isBlank() }
}

fun ReviewDocument.plainText(): String = paragraphs.joinToString(separator = "\n") { paragraph ->
    paragraph.runs.joinToString(separator = "") { it.text }
}

/**
 * Collapse a document to a canonical shape: drop empty runs and merge adjacent runs that carry the
 * same marks. Two documents with identical visible content and formatting are then structurally equal,
 * regardless of how their runs happened to be split (the editor and the server slice runs differently).
 */
fun ReviewDocument.canonical(): ReviewDocument = ReviewDocument(
    paragraphs = paragraphs.map { paragraph ->
        val merged = mutableListOf<ReviewRun>()

        paragraph.runs
            .filter { it.text.isNotEmpty() }
            .forEach { run ->
                val last = merged.lastOrNull()

                if (last != null &&
                    last.bold == run.bold &&
                    last.italic == run.italic &&
                    last.spoiler == run.spoiler
                ) {
                    merged[merged.lastIndex] = last.copy(text = last.text + run.text)
                } else {
                    merged.add(run)
                }
            }

        ReviewParagraph(runs = merged)
    },
)
