package nl.rhaydus.softcover.feature.widget.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.firstOrNull
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.domain.model.NO_USER_ID
import nl.rhaydus.softcover.feature.widget.trending.TrendingWidget
import nl.rhaydus.softcover.feature.widget.trending.TrendingWidgetDependencies
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

internal class TrendingRefreshWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters), KoinComponent {
    override suspend fun doWork(): Result {
        val dependencies = get<TrendingWidgetDependencies>()

        val userId = dependencies.getUserIdAsFlow().firstOrNull()

        if (userId == null || userId == NO_USER_ID) return Result.success()

        val interval = get<TrendingRefreshScheduler>().currentInterval() ?: return Result.success()

        var failed = false

        // Half the period: WorkManager runs a periodic job a little early at times, and a maxAge equal to the
        // period would then skip the fetch and leave the list stale for a whole extra period.
        dependencies.refreshTrendingBooks(maxAge = interval / 2).onFailure { error ->
            failed = true

            AppLog.e(
                error,
                "Refreshing trending books failed",
            )
        }

        if (failed) return Result.retry()

        TrendingWidget().updateAll(applicationContext)

        return Result.success()
    }
}
