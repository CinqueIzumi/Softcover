package nl.rhaydus.softcover.feature.widget.presentation.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/** Unique work names keep several placed quote widgets from fanning out into parallel fetches. */
internal class QuoteRefreshScheduler(private val context: Context) {
    fun refreshNow() {
        WorkManager.getInstance(context).enqueueUniqueWork(
            CATCH_UP_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<QuoteRefreshWorker>()
                .setConstraints(constraints())
                .build(),
        )
    }

    fun scheduleDaily() {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<QuoteRefreshWorker>(
                1,
                TimeUnit.DAYS,
            )
                .setConstraints(constraints())
                .build(),
        )
    }

    fun cancel() {
        val workManager = WorkManager.getInstance(context)

        workManager.cancelUniqueWork(WORK_NAME)
        workManager.cancelUniqueWork(CATCH_UP_WORK_NAME)
    }

    private fun constraints(): Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    private companion object {
        const val WORK_NAME = "quote_refresh"
        const val CATCH_UP_WORK_NAME = "quote_refresh_catch_up"
    }
}
