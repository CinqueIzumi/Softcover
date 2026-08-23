package nl.rhaydus.softcover.core.presentation.error

import kotlin.coroutines.cancellation.CancellationException
import nl.rhaydus.designsystem.util.SnackBarManager

/**
 * The standard presentation handling for a failed use-case [Result]: surface a user-facing snackbar
 * for it (mapped via [toUserMessage]), or nothing when the kind maps to `null`. Re-homes the generic
 * error toast the network seam used to emit from the data layer (D1) — presentation now owns the copy
 * and whether to show it.
 *
 * Surface-only by design: the failure was already **logged** at the use-case boundary by
 * `runCatchingLogged`, so this does not log again. That split is what makes a forgotten fold safe — a
 * missing `onApiFailure` costs at most a toast, never a swallowed or unlogged failure.
 *
 * Rethrows [CancellationException] defensively (a `runCatchingLogged` result never carries one, but a
 * hand-rolled `runCatching` source might). Returns the original [Result] so it can still be chained
 * with `.onSuccess { … }`.
 */
fun <T> Result<T>.onApiFailure(): Result<T> = onFailure { error ->
    if (error is CancellationException) throw error

    error.toUserMessage()?.let { message -> SnackBarManager.showSnackbar(title = message) }
}
