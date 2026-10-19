package nl.rhaydus.softcover.feature.widget.presentation.glance

import java.util.concurrent.ConcurrentHashMap

/**
 * App widget ids with a running Glance session. A session keeps its widget current through its own flows, so a
 * background render of a live instance is skipped rather than racing the session's frames.
 */
internal class LiveWidgetSessions {
    private val appWidgetIds: MutableSet<Int> = ConcurrentHashMap.newKeySet()

    fun markLive(appWidgetId: Int) {
        appWidgetIds.add(appWidgetId)
    }

    fun markEnded(appWidgetId: Int) {
        appWidgetIds.remove(appWidgetId)
    }

    fun isLive(appWidgetId: Int): Boolean = appWidgetId in appWidgetIds
}
