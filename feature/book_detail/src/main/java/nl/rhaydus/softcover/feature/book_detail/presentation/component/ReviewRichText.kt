package nl.rhaydus.softcover.feature.book_detail.presentation.component

import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.ReviewParagraph
import nl.rhaydus.softcover.core.domain.model.ReviewRun
import nl.rhaydus.softcover.core.domain.model.canonical

/**
 * Pure helpers for the review editor. It keeps the review as plain text plus a flat list of
 * [ReviewMark]s over half-open character ranges, rather than as a [ReviewDocument] tree — that makes
 * selection-based toggling and shifting marks across edits cheap. These functions maintain that mark
 * list and convert between the editor buffer and the structured [ReviewDocument].
 */

/** Drop empty marks and merge overlapping or touching marks of the same type into the fewest ranges. */
fun normalizeMarks(marks: List<ReviewMark>): List<ReviewMark> = marks
    .filter { it.start < it.end }
    .groupBy { it.type }
    .flatMap { (type, group) ->
        val merged = mutableListOf<ReviewMark>()

        group.sortedBy { it.start }.forEach { mark ->
            val last = merged.lastOrNull()

            if (last != null && mark.start <= last.end) {
                merged[merged.lastIndex] = last.copy(end = maxOf(
                    last.end,
                    mark.end,
                ),)
            } else {
                merged.add(mark.copy(type = type))
            }
        }

        merged
    }
    .sortedWith(compareBy(
        { it.start },
        { it.type },
    ),)

/**
 * Shift the marks to track a text edit. The edited span is found by diffing the common prefix and
 * suffix of [oldText] and [newText]; each mark endpoint then maps left of the edit unchanged, right of
 * it by the length delta, and collapses to the edit point if it fell inside the replaced span. A mark
 * that strictly straddles a pure insertion grows to cover the inserted text, while one ending exactly
 * at the insertion point does not — so typing just after a bold word stays unbolded.
 */
fun applyEditToMarks(
    marks: List<ReviewMark>,
    oldText: String,
    newText: String,
): List<ReviewMark> {
    if (oldText == newText) return normalizeMarks(marks)

    val prefix = commonPrefixLength(
        a = oldText,
        b = newText,
    )

    val maxSuffix = minOf(
        oldText.length - prefix,
        newText.length - prefix,
    )

    var suffix = 0

    while (suffix < maxSuffix &&
        oldText[oldText.length - 1 - suffix] == newText[newText.length - 1 - suffix]
    ) {
        suffix += 1
    }

    val removedStart = prefix
    val removedEnd = oldText.length - suffix
    val delta = newText.length - oldText.length

    fun mapPosition(position: Int): Int = when {
        position <= removedStart -> position
        position >= removedEnd -> position + delta
        else -> removedStart
    }

    return normalizeMarks(
        marks.map { mark ->
            mark.copy(
                start = mapPosition(position = mark.start),
                end = mapPosition(position = mark.end),
            )
        },
    )
}

/** Whether the whole selection [[start], [end]) is already covered by a mark of [type]. */
fun isRangeMarked(
    marks: List<ReviewMark>,
    start: Int,
    end: Int,
    type: ReviewMarkType,
): Boolean {
    if (start >= end) return false

    return normalizeMarks(marks = marks).any { it.type == type && it.start <= start && it.end >= end }
}

/**
 * Toggle [type] over the selection [[start], [end]): when the selection is already fully marked the
 * mark is cleared across it (splitting any straddling mark), otherwise a mark is added over it.
 */
fun toggleMark(
    marks: List<ReviewMark>,
    start: Int,
    end: Int,
    type: ReviewMarkType,
): List<ReviewMark> {
    if (start >= end) return normalizeMarks(marks = marks)

    val normalized = normalizeMarks(marks = marks)

    if (isRangeMarked(
        marks = normalized,
        start = start,
        end = end,
        type = type,
    )) {
        val cleared = normalized.flatMap { mark ->
            if (mark.type != type || mark.end <= start || mark.start >= end) {
                listOf(mark)
            } else {
                listOfNotNull(
                    mark.copy(end = start).takeIf { it.start < it.end },
                    mark.copy(start = end).takeIf { it.start < it.end },
                )
            }
        }

        return normalizeMarks(marks = cleared)
    }

    return normalizeMarks(marks = normalized + ReviewMark(
        start = start,
        end = end,
        type = type,
    ),)
}

/** Build the structured [ReviewDocument] from the editor's plain text and marks. */
fun editorBufferToDocument(
    text: String,
    marks: List<ReviewMark>,
): ReviewDocument {
    val normalized = normalizeMarks(marks = marks)

    val paragraphs = mutableListOf<ReviewParagraph>()

    var lineStart = 0

    while (true) {
        val newline = text.indexOf(
            char = '\n',
            startIndex = lineStart,
        )
        val lineEnd = if (newline < 0) text.length else newline

        paragraphs.add(
            ReviewParagraph(
                runs = runsForLine(
                    text = text,
                    lineStart = lineStart,
                    lineEnd = lineEnd,
                    marks = normalized,
                ),
            ),
        )

        if (newline < 0) break

        lineStart = newline + 1
    }

    return ReviewDocument(paragraphs = paragraphs).canonical()
}

/** Build the editor's plain text and marks from a structured [ReviewDocument]. */
fun documentToEditorBuffer(document: ReviewDocument): ReviewEditorBuffer {
    val builder = StringBuilder()
    val marks = mutableListOf<ReviewMark>()

    document.paragraphs.forEachIndexed { index, paragraph ->
        if (index > 0) builder.append('\n')

        paragraph.runs.forEach { run ->
            val start = builder.length

            builder.append(run.text)

            val end = builder.length

            if (run.bold) marks.add(ReviewMark(
                start = start,
                end = end,
                type = ReviewMarkType.BOLD,
            ),)

            if (run.italic) marks.add(ReviewMark(
                start = start,
                end = end,
                type = ReviewMarkType.ITALIC,
            ),)

            if (run.spoiler) marks.add(ReviewMark(
                start = start,
                end = end,
                type = ReviewMarkType.SPOILER,
            ),)
        }
    }

    return ReviewEditorBuffer(
        text = builder.toString(),
        marks = normalizeMarks(marks = marks),
    )
}

private fun runsForLine(
    text: String,
    lineStart: Int,
    lineEnd: Int,
    marks: List<ReviewMark>,
): List<ReviewRun> {
    if (lineStart >= lineEnd) return emptyList()

    val boundaries = sortedSetOf(lineStart, lineEnd)

    marks.forEach { mark ->
        if (mark.end > lineStart && mark.start < lineEnd) {
            boundaries.add(mark.start.coerceIn(
                lineStart,
                lineEnd,
            ),)
            boundaries.add(mark.end.coerceIn(
                lineStart,
                lineEnd,
            ),)
        }
    }

    val points = boundaries.toList()

    return (0 until points.size - 1).mapNotNull { index ->
        val from = points[index]
        val to = points[index + 1]

        if (from >= to) return@mapNotNull null

        ReviewRun(
            text = text.substring(
                startIndex = from,
                endIndex = to,
            ),
            bold = covers(
                marks = marks,
                from = from,
                to = to,
                type = ReviewMarkType.BOLD,
            ),
            italic = covers(
                marks = marks,
                from = from,
                to = to,
                type = ReviewMarkType.ITALIC,
            ),
            spoiler = covers(
                marks = marks,
                from = from,
                to = to,
                type = ReviewMarkType.SPOILER,
            ),
        )
    }
}

private fun covers(
    marks: List<ReviewMark>,
    from: Int,
    to: Int,
    type: ReviewMarkType,
): Boolean = marks.any { it.type == type && it.start <= from && it.end >= to }

private fun commonPrefixLength(
    a: String,
    b: String,
): Int {
    val max = minOf(
        a.length,
        b.length,
    )

    var index = 0

    while (index < max && a[index] == b[index]) index += 1

    return index
}
