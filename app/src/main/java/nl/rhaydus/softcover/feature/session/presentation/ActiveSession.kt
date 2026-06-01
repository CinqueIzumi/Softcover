package nl.rhaydus.softcover.feature.session.presentation

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.personal.domain.model.ReadingSession

/** The currently-running reading session paired with the book it belongs to. */
data class ActiveSession(
    val session: ReadingSession,
    val book: Book,
)
