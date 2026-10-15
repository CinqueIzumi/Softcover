package nl.rhaydus.softcover.feature.widget.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

internal class WidgetMaintenanceCancelWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters), KoinComponent {
    override suspend fun doWork(): Result {
        get<WidgetMaintenanceScheduler>().cancelIfUnused()

        return Result.success()
    }
}
