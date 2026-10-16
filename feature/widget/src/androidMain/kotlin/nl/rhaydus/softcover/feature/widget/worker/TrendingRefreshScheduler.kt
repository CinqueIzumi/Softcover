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
import nl.rhaydus.softcover.feature.widget.trending.TrendingRefreshPlan
import nl.rhaydus.softcover.feature.widget.trending.TrendingStore
import nl.rhaydus.softcover.feature.widget.trending.trendingRefreshPlan

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

    suspend fun currentPlan(): TrendingRefreshPlan? = trendingRefreshPlan(store.placedSettings())

    suspend fun currentInterval(): Duration? = currentPlan()?.interval

    suspend fun reschedule() {
        val interval = currentInterval()
        val workManager = WorkManager.getInstance(context)

        if (interval == null) {
            workManager.cancelUniqueWork(WORK_NAME)

            return
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        workManager.enqueueUniqueWork(
            CATCH_UP_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<TrendingRefreshWorker>()
                .setConstraints(constraints)
                .build(),
        )

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<TrendingRefreshWorker>(
                interval.inWholeHours,
                TimeUnit.HOURS,
            )
                .setConstraints(constraints)
                .build(),
        )
    }

    private companion object {
        const val WORK_NAME = "trending_refresh"
        const val CATCH_UP_WORK_NAME = "trending_refresh_catch_up"
        const val RESCHEDULE_WORK_NAME = "trending_refresh_reschedule"
    }
}
