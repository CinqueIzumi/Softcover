package nl.rhaydus.softcover.feature.widget.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import nl.rhaydus.softcover.feature.widget.trending.TrendingStore
import nl.rhaydus.softcover.feature.widget.trending.resolveTrendingRefreshInterval

internal class TrendingRefreshScheduler(
    private val context: Context,
    private val store: TrendingStore,
) {
    fun requestReschedule() {
        WorkManager.getInstance(context).enqueueUniqueWork(
            RESCHEDULE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<TrendingRescheduleWorker>().build(),
        )
    }

    suspend fun currentInterval(): Duration? = resolveTrendingRefreshInterval(store.placedCadences())

    suspend fun reschedule() {
        val interval = currentInterval()
        val workManager = WorkManager.getInstance(context)

        if (interval == null) {
            workManager.cancelUniqueWork(WORK_NAME)

            return
        }

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<TrendingRefreshWorker>(
                interval.inWholeHours,
                TimeUnit.HOURS,
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                )
                .build(),
        )
    }

    private companion object {
        const val WORK_NAME = "trending_refresh"
        const val RESCHEDULE_WORK_NAME = "trending_refresh_reschedule"
    }
}
