package nl.rhaydus.softcover.feature.widget.presentation.worker

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import nl.rhaydus.softcover.feature.widget.presentation.glance.OwnedWidgets
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
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

    fun requestRepaint() {
        WorkManager.getInstance(context).enqueueUniqueWork(
            REPAINT_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<WidgetMaintenanceWorker>().build(),
        )
    }

    suspend fun scheduleNextMidnight() {
        if (anyPlaced().not()) return

        val zone = ZoneId.systemDefault()
        val now = ZonedDateTime.now(zone)
        val midnight = now.toLocalDate().plusDays(1).atStartOfDay(zone)

        WorkManager.getInstance(context).enqueueUniqueWork(
            MIDNIGHT_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<WidgetMaintenanceWorker>()
                .setInitialDelay(
                    Duration.between(
                        now,
                        midnight,
                    ).toMillis() + MIDNIGHT_SLACK_MILLIS,
                    TimeUnit.MILLISECONDS,
                )
                .build(),
        )
    }

    suspend fun cancelIfUnused() {
        if (anyPlaced()) return

        val workManager = WorkManager.getInstance(context)

        workManager.cancelUniqueWork(WORK_NAME)
        workManager.cancelUniqueWork(MIDNIGHT_WORK_NAME)
    }

    private suspend fun anyPlaced(): Boolean {
        val manager = GlanceAppWidgetManager(context)

        return ownedWidgets.widgets.any { manager.getGlanceIds(it.javaClass).isNotEmpty() }
    }

    private companion object {
        const val WORK_NAME = "widget_maintenance"
        const val CANCEL_WORK_NAME = "widget_maintenance_cancel"
        const val REPAINT_WORK_NAME = "widget_maintenance_repaint"
        const val MIDNIGHT_WORK_NAME = "widget_maintenance_midnight"
        const val MIDNIGHT_SLACK_MILLIS = 5_000L
    }
}
