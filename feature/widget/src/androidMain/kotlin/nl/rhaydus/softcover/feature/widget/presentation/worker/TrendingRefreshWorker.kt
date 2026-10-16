package nl.rhaydus.softcover.feature.widget.presentation.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.domain.model.NO_USER_ID
import nl.rhaydus.softcover.feature.widget.presentation.glance.TrendingWidget
import nl.rhaydus.softcover.feature.widget.presentation.glance.TrendingWidgetDependencies
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.GlobalContext

internal class TrendingRefreshWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters), KoinComponent {
    override suspend fun doWork(): Result {
        if (GlobalContext.getOrNull() == null) return Result.retry()

        val dependencies = get<TrendingWidgetDependencies>()

        val userId = dependencies.getUserIdAsFlow().firstOrNull()

        if (userId == null || userId == NO_USER_ID) return Result.success()

        val plan = get<TrendingRefreshScheduler>().currentPlan() ?: return Result.success()

        val failed = coroutineScope {
            plan.periods
                .map { period ->
                    async {
                        // Half the period: WorkManager runs a periodic job a little early at times, and a maxAge
                        // equal to the period would then skip the fetch and leave the list stale for a whole
                        // extra period.
                        dependencies.refreshTrendingBooks(
                            period = period,
                            maxAge = plan.interval / 2,
                        ).onFailure { error ->
                            AppLog.e(
                                error,
                                "Refreshing trending books failed",
                            )
                        }.isFailure
                    }
                }
                .awaitAll()
                .any { it }
        }

        TrendingWidget().updateAll(applicationContext)

        return if (failed) Result.retry() else Result.success()
    }
}
