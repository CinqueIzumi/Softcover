package nl.rhaydus.softcover.core.uibinding.book

import nl.rhaydus.softcover.core.designsystem.presentation.transition.BookTransitionElement
import nl.rhaydus.softcover.core.designsystem.presentation.transition.bookTransitionKey
import nl.rhaydus.softcover.core.domain.model.Book

/** The shared-element key for this book's title, built from the same ids as its cover key. */
fun Book.titleTransitionKey(surface: String? = null): String? = bookTransitionKey(
    element = BookTransitionElement.Title,
    editionId = currentEdition?.id,
    bookId = id,
    surface = surface,
)

/** The shared-element key for this book's rating, built from the same ids as its cover key. */
fun Book.ratingTransitionKey(surface: String? = null): String? = bookTransitionKey(
    element = BookTransitionElement.Rating,
    editionId = currentEdition?.id,
    bookId = id,
    surface = surface,
)
