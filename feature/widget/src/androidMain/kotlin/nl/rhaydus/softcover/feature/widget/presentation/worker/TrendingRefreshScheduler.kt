package nl.rhaydus.softcover.feature.widget.presentation.worker

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingRefreshPlan
import nl.rhaydus.softcover.feature.widget.domain.model.trendingRefreshPlan
import nl.rhaydus.softcover.feature.widget.domain.usecase.GetPlacedTrendingSettingsUseCase
import nl.rhaydus.softcover.feature.widget.presentation.glance.TrendingWidget
import java.util.concurrent.TimeUnit

internal class TrendingRefreshScheduler(
    private val context: Context,
    private val getPlacedTrendingSettings: GetPlacedTrendingSettingsUseCase,
) {
    fun requestReschedule() {
        WorkManager.getInstance(context).enqueueUniqueWork(
            RESCHEDULE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<TrendingRescheduleWorker>().build(),
        )
    }

    suspend fun currentPlan(): TrendingRefreshPlan? {
        val manager = GlanceAppWidgetManager(context)

        val appWidgetIds = manager.getGlanceIds(TrendingWidget::class.java)
            .map { glanceId -> manager.getAppWidgetId(glanceId) }

        return trendingRefreshPlan(getPlacedTrendingSettings(appWidgetIds = appWidgetIds).getOrThrow())
    }

    suspend fun reschedule() {
        val interval = currentPlan()?.interval
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
