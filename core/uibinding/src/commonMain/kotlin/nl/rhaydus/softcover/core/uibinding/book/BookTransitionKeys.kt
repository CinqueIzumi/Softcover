package nl.rhaydus.softcover.core.uibinding.book

import nl.rhaydus.softcover.core.component.transition.BookTransitionElement
import nl.rhaydus.softcover.core.component.transition.BookTransitionKey
import nl.rhaydus.softcover.core.component.transition.bookTransitionKey
import nl.rhaydus.softcover.core.domain.model.Book

fun Book.transitionKey(
    element: BookTransitionElement,
    surface: String? = null,
): BookTransitionKey = bookTransitionKey(
    element = element,
    bookId = id,
    surface = surface,
)
