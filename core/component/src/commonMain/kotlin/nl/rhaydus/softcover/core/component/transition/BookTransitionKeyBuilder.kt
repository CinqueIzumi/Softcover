package nl.rhaydus.softcover.core.component.transition

/**
 * Keyed on the book rather than an edition so it stays stable when the destination resolves a different
 * current edition than the source row used. Pass a [surface] when the same book can appear in several visible
 * carousels on one screen, so each card registers a distinct key.
 */
fun bookTransitionKey(
    element: BookTransitionElement,
    bookId: Int,
    surface: String? = null,
): BookTransitionKey {
    val base = "book-${element.keyPart}-book-$bookId"

    return BookTransitionKey(value = if (surface != null) "$base-$surface" else base)
}
