package nl.rhaydus.softcover.core.designsystem.preset

/**
 * A minimal JSON writer for the token file. Objects keep insertion order and the output is indented with
 * two spaces, so a re-export is byte-identical and a token change reads as a one-line diff.
 */
internal object Json {
    private const val HEX_RADIX = 16
    private const val UNICODE_DIGITS = 4
    private const val INDENT = "  "

    fun write(value: Map<String, Any?>): String = buildString {
        append(
            value,
            depth = 0,
        )
        append('\n')
    }

    private fun StringBuilder.append(
        value: Any?,
        depth: Int,
    ) {
        when (value) {
            is Map<*, *> -> appendObject(
                value,
                depth,
            )
            is List<*> -> appendArray(
                value,
                depth,
            )
            is String -> appendString(value)
            is Int -> append(value)
            is Boolean -> append(value)
            else -> error("Unsupported JSON value: $value")
        }
    }

    private fun StringBuilder.appendObject(
        map: Map<*, *>,
        depth: Int,
    ) {
        if (map.isEmpty()) {
            append("{}")
            return
        }

        append("{\n")
        map.entries.forEachIndexed { index, (key, value) ->
            append(INDENT.repeat(depth + 1))
            appendString(key as String)
            append(": ")
            append(
                value,
                depth + 1,
            )
            append(if (index < map.size - 1) ",\n" else "\n")
        }
        append(INDENT.repeat(depth))
        append('}')
    }

    private fun StringBuilder.appendArray(
        list: List<*>,
        depth: Int,
    ) {
        if (list.isEmpty()) {
            append("[]")
            return
        }

        append("[\n")
        list.forEachIndexed { index, value ->
            append(INDENT.repeat(depth + 1))
            append(
                value,
                depth + 1,
            )
            append(if (index < list.size - 1) ",\n" else "\n")
        }
        append(INDENT.repeat(depth))
        append(']')
    }

    private fun StringBuilder.appendString(text: String) {
        append('"')
        text.forEach { char ->
            when {
                char == '"' -> append("\\\"")
                char == '\\' -> append("\\\\")
                char == '\n' -> append("\\n")
                char < ' ' -> append("\\u").append(char.code.toString(HEX_RADIX).padStart(
                    UNICODE_DIGITS,
                    '0',
                ),)
                else -> append(char)
            }
        }
        append('"')
    }
}
