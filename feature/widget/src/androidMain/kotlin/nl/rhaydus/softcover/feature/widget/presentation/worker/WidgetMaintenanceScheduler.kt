package nl.rhaydus.softcover.feature.widget.presentation.worker

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import nl.rhaydus.softcover.feature.widget.presentation.glance.OwnedWidgets
import java.util.concurrent.TimeUnit

internal class WidgetMaintenanceScheduler(
    private val context: Context,
    private val ownedWidgets: OwnedWidgets,
) {
    fun schedule() {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<WidgetMaintenanceWorker>(
                1,
                TimeUnit.HOURS,
            ).build(),
        )
    }

    fun requestCancelIfUnused() {
        WorkManager.getInstance(context).enqueueUniqueWork(
            CANCEL_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<WidgetMaintenanceCancelWorker>().build(),
        )
    }

    suspend fun cancelIfUnused() {
        val manager = GlanceAppWidgetManager(context)

        val anyPlaced = ownedWidgets.widgets.any { manager.getGlanceIds(it.javaClass).isNotEmpty() }

        if (anyPlaced.not()) WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    private companion object {
        const val WORK_NAME = "widget_maintenance"
        const val CANCEL_WORK_NAME = "widget_maintenance_cancel"
    }
}
