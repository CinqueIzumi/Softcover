package nl.rhaydus.softcover.core.domain.result

import nl.rhaydus.softcover.core.domain.logging.AppLog

/**
 * The standard use-case body wrapper. Delegates to [runCatchingCancellable] for the cancellation-safe
 * `Result` conversion, then logs any failure once, here at the use-case boundary — so a failure is
 * never silently dropped even when the presentation layer chooses not to surface it.
 *
 * This is the single place the app's log strategy ([AppLog]) is bound to the cancellation primitive,
 * rather than re-passing it at every call site: every use case writes `runCatchingLogged { … }`
 * instead of `runCatching { … }` (flagged advisory by `scripts/style-check.sh`), and logging is guaranteed
 * with zero per-site ceremony.
 *
 * Whether to *surface* the failure to the user stays a separate, presentation-layer decision
 * (`onApiFailure` / a UiState error slot) — this never authors user-facing copy. A use case composed
 * from others (unwrapping them via [Result.getOrThrow]) logs at each boundary it crosses; that is a
 * deliberate call-chain trace, not redundant noise. Pass [context] to label the log line; omit it to
 * fall back to the throwable's own message.
 */
inline fun <T> runCatchingLogged(
    context: String? = null,
    block: () -> T,
): Result<T> = runCatchingCancellable(block).onFailure { error ->
    AppLog.e(
        throwable = error,
        message = context ?: error.message ?: error.toString(),
    )
}
