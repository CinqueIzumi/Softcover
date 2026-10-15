package nl.rhaydus.softcover.feature.widget.worker

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
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

    suspend fun cancelIfUnused() {
        val manager = GlanceAppWidgetManager(context)

        val anyPlaced = ownedWidgets.widgets.any { manager.getGlanceIds(it.javaClass).isNotEmpty() }

        if (anyPlaced.not()) WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    private companion object {
        const val WORK_NAME = "widget_maintenance"
    }
}
