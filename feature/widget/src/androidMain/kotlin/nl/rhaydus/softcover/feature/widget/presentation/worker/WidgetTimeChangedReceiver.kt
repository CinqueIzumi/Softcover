package nl.rhaydus.softcover.feature.widget.presentation.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/** A clock or time zone change moves the day boundary Daily picks roll over on, so the widgets repaint right away. */
internal class WidgetTimeChangedReceiver : BroadcastReceiver(), KoinComponent {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action != Intent.ACTION_TIME_CHANGED && intent.action != Intent.ACTION_TIMEZONE_CHANGED) return

        get<WidgetMaintenanceScheduler>().requestRepaint()
    }
}
