package nl.rhaydus.softcover.feature.widget.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import nl.rhaydus.common.AppLog
import nl.rhaydus.common.runCatchingCancellable
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.GlobalContext

internal class WidgetMaintenanceWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters), KoinComponent {
    override suspend fun doWork(): Result {
        if (GlobalContext.getOrNull() == null) return Result.retry()

        get<OwnedWidgets>().widgets.forEach { widget ->
            runCatchingCancellable { widget.updateAll(applicationContext) }
                .onFailure { error ->
                    AppLog.e(
                        error,
                        "Maintenance update of a widget failed",
                    )
                }
        }

        return Result.success()
    }
}
