package nl.rhaydus.softcover.feature.widget.presentation.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.GlobalContext

internal class WidgetMaintenanceCancelWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters), KoinComponent {
    override suspend fun doWork(): Result {
        if (GlobalContext.getOrNull() == null) return Result.retry()

        get<WidgetMaintenanceScheduler>().cancelIfUnused()

        return Result.success()
    }
}
