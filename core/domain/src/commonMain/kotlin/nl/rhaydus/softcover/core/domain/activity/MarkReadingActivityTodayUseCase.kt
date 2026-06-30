package nl.rhaydus.softcover.core.domain.activity

/**
 * Optimistically marks today as an active reading day so reading-activity surfaces (the Reading
 * screen's streak strip) update the moment a book is read, instead of waiting for a server refresh.
 *
 * A `core:domain` contract so book-operation use cases can trigger the mark without depending on the
 * profile module; the implementation lives in `core:profile`, bound via Koin. It is a best-effort
 * side-effect that logs and swallows its own failures, so it returns no caller-relevant outcome.
 */
interface MarkReadingActivityTodayUseCase {
    suspend operator fun invoke()
}
