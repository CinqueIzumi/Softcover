package nl.rhaydus.softcover.feature.widget.settings

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

/** The data URI keeps each instance's PendingIntent distinct, since Intents differing only in extras share one. */
internal fun widgetSettingsIntent(
    context: Context,
    activityClass: Class<*>,
    appWidgetId: Int,
): Intent = Intent(
    context,
    activityClass,
).apply {
    action = AppWidgetManager.ACTION_APPWIDGET_CONFIGURE
    data = "softcover://widget/settings/${activityClass.simpleName}/$appWidgetId".toUri()
    putExtra(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        appWidgetId,
    )
}
