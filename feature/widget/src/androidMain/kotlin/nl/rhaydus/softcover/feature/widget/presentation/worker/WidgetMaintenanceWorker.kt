package nl.rhaydus.softcover.feature.widget.presentation.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import nl.rhaydus.common.AppLog
import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.softcover.feature.widget.presentation.glance.BackgroundWidgetRenderer
import nl.rhaydus.softcover.feature.widget.presentation.glance.OwnedWidgets
import nl.rhaydus.softcover.feature.widget.presentation.image.WidgetImageStore
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.GlobalContext

internal class WidgetMaintenanceWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters), KoinComponent {
    override suspend fun doWork(): Result {
        if (GlobalContext.getOrNull() == null) return Result.retry()

        val renderer = get<BackgroundWidgetRenderer>()

        get<OwnedWidgets>().widgets.forEach { widget -> renderer.renderAll(widget) }

        runCatchingCancellable { get<WidgetImageStore>().prune() }
            .onFailure { error ->
                AppLog.e(
                    error,
                    "Pruning the widget images failed",
                )
            }

        runCatchingCancellable { get<WidgetMaintenanceScheduler>().scheduleNextMidnight() }
            .onFailure { error ->
                AppLog.e(
                    error,
                    "Scheduling the midnight widget update failed",
                )
            }

        runCatchingCancellable { get<WidgetMaintenanceScheduler>().scheduleNextPickExpiry() }
            .onFailure { error ->
                AppLog.e(
                    error,
                    "Scheduling the Random pick expiry widget update failed",
                )
            }

        return Result.success()
    }
}
