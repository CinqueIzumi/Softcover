package nl.rhaydus.softcover.feature.widget.presentation.activity

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

/**
 * The unique data per widget makes the launcher-free settings task distinct per widget, and `CLEAR_TASK` replaces it
 * on a second tap instead of stacking another.
 */
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
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    putExtra(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        appWidgetId,
    )
}
