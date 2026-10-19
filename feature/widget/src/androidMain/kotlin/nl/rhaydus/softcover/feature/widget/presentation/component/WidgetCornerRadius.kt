package nl.rhaydus.softcover.feature.widget.presentation.component

import android.os.Build
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.cornerRadius

internal fun widgetCornerRadius(): GlanceModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    GlanceModifier.cornerRadius(android.R.dimen.system_app_widget_background_radius)
} else {
    GlanceModifier
}
