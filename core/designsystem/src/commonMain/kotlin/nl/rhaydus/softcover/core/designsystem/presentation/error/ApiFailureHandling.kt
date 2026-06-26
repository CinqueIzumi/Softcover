package nl.rhaydus.softcover.core.designsystem.presentation.error

import kotlin.coroutines.cancellation.CancellationException
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.domain.logging.AppLog

/**
 * The standard presentation handling for a failed use-case [Result]: log the error and surface a
 * user-facing snackbar for it (mapped via [toUserMessage]). Replaces the scattered
 * `.onFailure { AppLog.e(...) }` swallow folds and re-homes the generic error toast the network seam
 * used to emit from the data layer (D1) — now presentation owns both the copy and whether to show it.
 *
 * Rethrows [CancellationException] so coroutine cancellation is never swallowed. Returns the original
 * [Result] so it can still be chained with `.onSuccess { … }`.
 */
fun <T> Result<T>.onApiFailure(logContext: String? = null): Result<T> = onFailure { error ->
    if (error is CancellationException) throw error

    AppLog.e(
        throwable = error,
        message = logContext ?: error.message ?: "Operation failed",
    )

    error.toUserMessage()?.let { message -> SnackBarManager.showSnackbar(title = message) }
}
