package nl.rhaydus.softcover.core.designsystem.presentation.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

// Single-pass HTML parser: each `continue` advances past a distinct token class (tag, entity, plain char); collapsing
// them would obscure the per-branch index bookkeeping.
@Suppress("LoopWithTooManyJumpStatements")
fun htmlToAnnotatedString(html: String): AnnotatedString {
    val builder = AnnotatedString.Builder()
    val openSpans = ArrayDeque<Int>()

    var index = 0

    while (index < html.length) {
        val char = html[index]

        if (char == '<') {
            val closingBracket = html.indexOf(
                char = '>',
                startIndex = index,
            )

            if (closingBracket < 0) {
                builder.append(char)
                index += 1
                continue
            }

            val tag = html.substring(
                startIndex = index + 1,
                endIndex = closingBracket,
            )
                .trim()
                .trimEnd('/')
                .trim()
                .lowercase()

            when (tag) {
                "br" -> builder.append('\n')
                "p", "div" -> Unit
                "/p", "/div" -> builder.append("\n\n")
                "strong", "b" -> openSpans.addLast(
                    builder.pushStyle(style = SpanStyle(fontWeight = FontWeight.Bold)),
                )

                "em", "i" -> openSpans.addLast(
                    builder.pushStyle(style = SpanStyle(fontStyle = FontStyle.Italic)),
                )

                "/strong", "/b", "/em", "/i" -> {
                    if (openSpans.isNotEmpty()) builder.pop(index = openSpans.removeLast())
                }
            }

            index = closingBracket + 1
            continue
        }

        if (char == '&') {
            val semicolon = html.indexOf(
                char = ';',
                startIndex = index,
            )

            if (semicolon < 0 || semicolon - index > 10) {
                builder.append(char)
                index += 1
                continue
            }

            val entity = html.substring(
                startIndex = index + 1,
                endIndex = semicolon,
            )

            val decoded = decodeEntity(entity = entity)

            if (decoded == null) {
                builder.append(char)
                index += 1
                continue
            }

            builder.append(decoded)
            index = semicolon + 1
            continue
        }

        builder.append(char)
        index += 1
    }

    while (openSpans.isNotEmpty()) builder.pop(index = openSpans.removeLast())

    val result = builder.toAnnotatedString()

    val trimEnd = result.text.indexOfLast { it.isWhitespace().not() } + 1

    if (trimEnd == result.text.length) return result

    return result.subSequence(
        startIndex = 0,
        endIndex = trimEnd,
    )
}

private fun decodeEntity(entity: String): String? {
    if (entity.startsWith(prefix = "#x") || entity.startsWith(prefix = "#X")) {
        return entity.substring(startIndex = 2).toIntOrNull(radix = 16)?.toChar()?.toString()
    }

    if (entity.startsWith(prefix = "#")) {
        return entity.substring(startIndex = 1).toIntOrNull()?.toChar()?.toString()
    }

    return when (entity) {
        "amp" -> "&"
        "lt" -> "<"
        "gt" -> ">"
        "quot" -> "\""
        "apos" -> "'"
        "nbsp" -> " "
        else -> null
    }
}
