package nl.rhaydus.softcover.core.profile.domain.model

import nl.rhaydus.softcover.core.domain.model.Book

/** The read finished in the fewest calendar days in range. */
data class QuickestRead(
    val book: Book,
    val duration: ReadDuration,
)
