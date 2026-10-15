package nl.rhaydus.softcover.feature.book_detail.presentation.state

private val IsbnCharacters = Regex("[0-9X]+")

/** The [query] as an ISBN to prefill the scanner with, or null when it cannot be one. */
internal fun isbnPrefillFor(query: String): String? {
    val candidate = query
        .filterNot { it == '-' || it.isWhitespace() }
        .uppercase()

    return candidate.takeIf { it.isNotEmpty() && IsbnCharacters.matches(it) }
}
