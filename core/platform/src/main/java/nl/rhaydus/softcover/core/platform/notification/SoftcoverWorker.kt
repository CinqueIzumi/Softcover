package nl.rhaydus.softcover.core.platform.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlin.coroutines.cancellation.CancellationException
import nl.rhaydus.softcover.core.domain.logging.AppLog

/**
 * Base class for scheduled work that produces notifications.
 *
 * Subclasses implement [work] with their own logic; [doWork] wraps it with a single Timber
 * catch so a thrown worker doesn't silently fail. No concrete workers exist yet — this is the
 * shared shape future deadline/release/recap workers will adopt.
 */
internal abstract class SoftcoverWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters) {
    abstract suspend fun work(): Result

    // A scheduled worker must never crash the process; any subclass failure is logged and reported as a failed Result.
    @Suppress("TooGenericExceptionCaught")
    final override suspend fun doWork(): Result {
        return try {
            work()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            AppLog.e(
                throwable,
                "SoftcoverWorker ${this::class.simpleName} failed",
            )

            Result.failure()
        }
    }
}
