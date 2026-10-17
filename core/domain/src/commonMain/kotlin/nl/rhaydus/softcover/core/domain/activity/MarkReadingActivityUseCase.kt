package nl.rhaydus.softcover.core.domain.activity

/**
 * Optimistically marks the day of [actionAt] as an active reading day, so reading activity updates the
 * moment a book is read instead of waiting for a server refresh. [actionAt] is an ISO-8601 instant;
 * `null` means now.
 *
 * A `core:domain` contract so book-operation use cases can trigger the mark without depending on the
 * profile module; the implementation lives in `core:profile`, bound via Koin. It is a best-effort
 * side-effect that logs and swallows its own failures, so it returns no caller-relevant outcome.
 */
interface MarkReadingActivityUseCase {
    suspend operator fun invoke(actionAt: String?)
}
