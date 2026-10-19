package nl.rhaydus.softcover.feature.widget.presentation.worker

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import nl.rhaydus.softcover.feature.widget.domain.usecase.GetNextWeeklyPickExpiryUseCase
import nl.rhaydus.softcover.feature.widget.presentation.glance.OwnedWidgets
import nl.rhaydus.softcover.feature.widget.presentation.glance.RandomPickWidget
import nl.rhaydus.softcover.feature.widget.presentation.image.WidgetImageStore
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import kotlin.time.Clock

internal class WidgetMaintenanceScheduler(
    private val context: Context,
    private val ownedWidgets: OwnedWidgets,
    private val imageStore: WidgetImageStore,
    private val getNextWeeklyPickExpiry: GetNextWeeklyPickExpiryUseCase,
) {
    fun schedule() {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<WidgetMaintenanceWorker>(
                24,
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
                    ).toMillis() + SLACK_MILLIS,
                    TimeUnit.MILLISECONDS,
                )
                .build(),
        )
    }

    suspend fun scheduleNextPickExpiry() {
        val manager = GlanceAppWidgetManager(context)

        val appWidgetIds = manager.getGlanceIds(RandomPickWidget::class.java)
            .map { glanceId -> manager.getAppWidgetId(glanceId) }

        val expiry = getNextWeeklyPickExpiry(appWidgetIds = appWidgetIds).getOrThrow()
        val workManager = WorkManager.getInstance(context)

        if (expiry == null) {
            workManager.cancelUniqueWork(PICK_EXPIRY_WORK_NAME)

            return
        }

        workManager.enqueueUniqueWork(
            PICK_EXPIRY_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<WidgetMaintenanceWorker>()
                .setInitialDelay(
                    (expiry - Clock.System.now()).inWholeMilliseconds + SLACK_MILLIS,
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
        workManager.cancelUniqueWork(PICK_EXPIRY_WORK_NAME)

        imageStore.clear()
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
        const val PICK_EXPIRY_WORK_NAME = "widget_maintenance_pick_expiry"
        const val SLACK_MILLIS = 5_000L
    }
}
