package nl.rhaydus.softcover.feature.books.domain.usecase

/**
 * Result of a shelf-status mutation (`MarkBookAsRead`, `MarkBookAsReading`, `MarkBookAsWantToRead`).
 *
 * - [Applied] — the book moved to a new status; downstream success effects (events, celebration
 *   bursts, haptics) should fire.
 * - [NoChange] — the book was already on the target shelf, so the use case short-circuited
 *   without touching local cache or the remote. Callers must gate "did it succeed?" side
 *   effects on [Applied] so a tap on the already-active shelf chip stays inert.
 */
sealed interface ShelfMutationOutcome {
    data object Applied : ShelfMutationOutcome
    data object NoChange : ShelfMutationOutcome
}
