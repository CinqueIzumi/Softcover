package nl.rhaydus.softcover.feature.settings.data.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.feature.settings.domain.model.MarkdownBlock
import nl.rhaydus.softcover.feature.settings.domain.model.MarkdownSpan

/**
 * Parses raw markdown into [MarkdownBlock]s. This is a pure function over a [String] - no IO, no
 * Compose - because it runs over human-written documents (GitHub milestone descriptions, a hand-kept
 * changelog) rather than markdown this app controls: anything unrecognised must render as plain text,
 * and nothing may throw or be silently dropped. Supports ATX headings (`#`/`##`/`###`), paragraphs
 * (blank-line separated, soft-wrapped lines joined with a space), `-`/`*` bullets, `1.` ordered items,
 * `>` block quotes, and `---` thematic breaks; every other line is folded into the surrounding paragraph as plain text.
 *
 * The leading H1 is dropped: every published document opens with its title (`# Softcover roadmap`,
 * `# Softcover changelog`), and the screen's own chrome (a top bar / page header) already supplies it,
 * so keeping it would duplicate it in the body.
 */
internal fun String.toMarkdownBlocks(): ImmutableList<MarkdownBlock> {
    val blocks = MarkdownParser().parse(markdown = this)
    val firstBlock = blocks.firstOrNull()

    return if (firstBlock is MarkdownBlock.Heading && firstBlock.level == 1) {
        blocks.drop(1).toImmutableList()
    } else {
        blocks.toImmutableList()
    }
}

private class MarkdownParser {
    private val blocks = mutableListOf<MarkdownBlock>()
    private val paragraphLines = mutableListOf<String>()
    private val quoteLines = mutableListOf<String>()

    fun parse(markdown: String): List<MarkdownBlock> {
        markdown
            .replace(
                "\r\n",
                "\n",
            )
            .split("\n")
            .forEach(::consumeLine)

        flushParagraph()
        flushQuote()

        return blocks.toList()
    }

    private fun consumeLine(rawLine: String) {
        val line = rawLine.trim()

        val heading = HEADING_REGEX.matchEntire(line)
        val quote = QUOTE_REGEX.matchEntire(line)

        when {
            line.isEmpty() -> {
                flushParagraph()
                flushQuote()
            }

            heading != null -> consumeHeading(heading)

            THEMATIC_BREAK_REGEX.matches(line) -> consumeThematicBreak()

            quote != null -> {
                flushParagraph()
                quoteLines.add(quote.groupValues[1].trim())
            }

            else -> consumeNonQuoteLine(line)
        }
    }

    private fun consumeHeading(heading: MatchResult) {
        flushParagraph()
        flushQuote()

        blocks.add(
            MarkdownBlock.Heading(
                level = heading.groupValues[1].length,
                spans = heading.groupValues[2].trim().toMarkdownSpans(),
            ),
        )
    }

    private fun consumeThematicBreak() {
        flushParagraph()
        flushQuote()
        blocks.add(MarkdownBlock.ThematicBreak)
    }

    private fun consumeNonQuoteLine(line: String) {
        flushQuote()

        val bullet = BULLET_REGEX.matchEntire(line)
        val ordered = ORDERED_REGEX.matchEntire(line)

        when {
            bullet != null -> {
                flushParagraph()
                blocks.add(MarkdownBlock.BulletListItem(spans = bullet.groupValues[1].trim().toMarkdownSpans()))
            }

            ordered != null -> {
                flushParagraph()

                blocks.add(
                    MarkdownBlock.OrderedListItem(
                        index = ordered.groupValues[1].toIntOrNull() ?: nextOrderedIndex(),
                        spans = ordered.groupValues[2].trim().toMarkdownSpans(),
                    ),
                )
            }

            else -> paragraphLines.add(line)
        }
    }

    // Only reached when a `\d+` capture overflows `Int`. Numbering restarts with each list, so count
    // back over the run this item belongs to rather than every ordered item in the document — otherwise
    // a second list that also overflowed would carry on from where the first one stopped.
    private fun nextOrderedIndex(): Int = blocks.takeLastWhile { it is MarkdownBlock.OrderedListItem }.size + 1

    private fun flushParagraph() {
        if (paragraphLines.isEmpty()) return

        blocks.add(MarkdownBlock.Paragraph(spans = paragraphLines.joinToString(separator = " ").toMarkdownSpans()))
        paragraphLines.clear()
    }

    private fun flushQuote() {
        if (quoteLines.isEmpty()) return

        blocks.add(MarkdownBlock.BlockQuote(spans = quoteLines.joinToString(separator = " ").toMarkdownSpans()))
        quoteLines.clear()
    }

    private companion object {
        val HEADING_REGEX = Regex("^(#{1,3})\\s+(.+)$")
        val THEMATIC_BREAK_REGEX = Regex("^(-{3,}|\\*{3,}|_{3,})$")
        val QUOTE_REGEX = Regex("^>\\s?(.*)$")
        val BULLET_REGEX = Regex("^[-*]\\s+(.+)$")
        val ORDERED_REGEX = Regex("^(\\d+)\\.\\s+(.+)$")
    }
}

/**
 * Parses `**bold**`, `*italic*` / `_italic_`, `` `code` ``, and `[text](url)` into [MarkdownSpan]s.
 * An underscore between two letters or digits (`snake_case`) is literal text, not an italic marker.
 * Marks toggle independently rather than nesting (see [MarkdownSpan]'s doc), and an unterminated
 * marker (e.g. a stray `*`) simply keeps that mark active for the remaining text instead of throwing
 * - the run is still shown, just styled. When nothing was ever flushed (e.g. the whole input was
 * marker characters with no text), the original raw text is returned verbatim as a single plain span
 * so content is never silently dropped.
 */
private fun String.toMarkdownSpans(): ImmutableList<MarkdownSpan> {
    val spans = mutableListOf<MarkdownSpan>()
    val buffer = StringBuilder()
    var bold = false
    var italic = false
    var code = false
    var index = 0

    fun flush() {
        if (buffer.isEmpty()) return

        spans.add(
            MarkdownSpan(
                text = buffer.toString(),
                bold = bold,
                italic = italic,
                code = code,
            ),
        )
        buffer.clear()
    }

    while (index < length) {
        val isBoldMarker = code.not() && index + 1 < length && this[index] == '*' && this[index + 1] == '*'
        val isItalicMarker = code.not() && (this[index] == '*' || isUnderscoreMarker(index))
        val linkMatch = if (code.not() && this[index] == '[') LINK_REGEX.matchAt(
            this,
            index,
        ) else null

        when {
            isBoldMarker -> {
                flush()
                bold = bold.not()
                index += 2
            }

            isItalicMarker -> {
                flush()
                italic = italic.not()
                index += 1
            }

            this[index] == '`' -> {
                flush()
                code = code.not()
                index += 1
            }

            linkMatch != null -> {
                flush()

                spans.add(
                    MarkdownSpan(
                        text = linkMatch.groupValues[1],
                        bold = bold,
                        italic = italic,
                        code = code,
                        url = linkMatch.groupValues[2],
                    ),
                )

                index += linkMatch.value.length
            }

            else -> {
                buffer.append(this[index])
                index += 1
            }
        }
    }

    flush()

    return spans.ifEmpty { listOf(MarkdownSpan(text = this)) }.toImmutableList()
}

private val LINK_REGEX = Regex("""\[([^\]]*)]\(([^)]*)\)""")

private fun String.isUnderscoreMarker(index: Int): Boolean {
    if (this[index] != '_') return false

    val followsWordChar = getOrNull(index - 1)?.isLetterOrDigit() == true
    val precedesWordChar = getOrNull(index + 1)?.isLetterOrDigit() == true

    return (followsWordChar && precedesWordChar).not()
}
