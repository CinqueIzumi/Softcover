package nl.rhaydus.softcover.core.domain.connectivity

/** The attempt count at which a queued user-book write is parked as poison and no longer replayed. */
const val USER_BOOK_WRITE_MAX_ATTEMPTS: Int = 5
