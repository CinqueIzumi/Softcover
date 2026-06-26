package nl.rhaydus.softcover.core.domain.result

import kotlin.coroutines.cancellation.CancellationException

/**
 * A cancellation-aware [runCatching]: identical to the stdlib version, except a
 * [CancellationException] is re-thrown rather than captured. Bare `runCatching` swallows it, which
 * silently breaks structured concurrency — a cancelled child runs its failure path instead of
 * unwinding, and the parent's cancellation signal is lost. Use this anywhere a `Result` is built
 * inside coroutine code.
 *
 * Deliberately a **pure primitive**: no logging, no app coupling. It is the F4 foundation-upstream
 * candidate (see `docs/working/foundation-upstream-candidates.md`) destined for `nl.rhaydus:core-ui`;
 * this app-local copy is a drop-in delete once that lands. Callers that want a failure logged compose
 * it (`runCatchingCancellable(block).onFailure { … }`) — the use-case standard [runCatchingLogged]
 * binds that once so it never has to be repeated.
 *
 * The [CancellationException] caught here is `kotlin.coroutines.cancellation.CancellationException`,
 * the multiplatform type, so the rethrow holds on every target — not just the JVM.
 */
// Catching every non-cancellation Throwable is the defining behaviour of a `runCatching` primitive.
@Suppress("TooGenericExceptionCaught")
inline fun <T> runCatchingCancellable(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (error: Throwable) {
    Result.failure(error)
}
