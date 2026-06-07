package nl.rhaydus.softcover.core.notification

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

/**
 * App-supplied branding for system notifications: the status-bar [smallIcon] silhouette and the
 * [accentColor] used to tint the notification. Supplied by `:app` (which owns the drawable/color
 * resources) so this module stays free of any design-system dependency.
 */
data class NotificationAppearance(
    @param:DrawableRes val smallIcon: Int,
    @param:ColorRes val accentColor: Int,
)
