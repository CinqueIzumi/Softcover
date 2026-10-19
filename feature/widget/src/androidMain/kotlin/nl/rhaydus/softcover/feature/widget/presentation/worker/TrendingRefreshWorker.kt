package nl.rhaydus.softcover.feature.widget.presentation.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import nl.rhaydus.common.AppLog
import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.softcover.core.domain.model.NO_USER_ID
import nl.rhaydus.softcover.feature.widget.presentation.glance.TrendingWidget
import nl.rhaydus.softcover.feature.widget.presentation.glance.TrendingWidgetDependencies
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.GlobalContext

private const val MAX_RETRIES = 3

internal class TrendingRefreshWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters), KoinComponent {
    override suspend fun doWork(): Result {
        if (GlobalContext.getOrNull() == null) return Result.retry()

        val dependencies = get<TrendingWidgetDependencies>()

        val userId = dependencies.environment.getUserIdAsFlow().firstOrNull()

        if (userId == null || userId == NO_USER_ID) return Result.success()

        val plan = get<TrendingRefreshScheduler>().currentPlan() ?: return Result.success()

        val failed = coroutineScope {
            plan.feeds
                .map { feed ->
                    async {
                        // Half the interval: WorkManager runs a periodic job a little early at times, and a maxAge
                        // equal to the interval would then skip the fetch and leave the list stale for a whole
                        // extra interval.
                        dependencies.refreshTrendingFeed(
                            feed = feed,
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

        runCatchingCancellable { dependencies.renderer.renderAll(TrendingWidget()) }
            .onFailure { error ->
                AppLog.e(
                    error,
                    "Repainting the trending widget failed",
                )
            }

        return if (failed && runAttemptCount < MAX_RETRIES) Result.retry() else Result.success()
    }
}
