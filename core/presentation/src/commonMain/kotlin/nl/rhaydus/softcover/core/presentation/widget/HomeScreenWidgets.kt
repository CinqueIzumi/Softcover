package nl.rhaydus.softcover.core.presentation.widget

import kotlinx.coroutines.flow.Flow

/** The app's window onto the home-screen widgets the user has placed; only Android has any. */
interface HomeScreenWidgets {
    val isSupported: Boolean

    /** Read at call time: the launcher can change while the app runs. */
    fun canPin(): Boolean

    /** Re-emits when an instance is added or removed, or its settings change. */
    fun observePlaced(): Flow<List<PlacedHomeScreenWidget>>

    fun openSettings(widgetId: Int)

    fun requestPin(kind: HomeScreenWidgetKind)
}
