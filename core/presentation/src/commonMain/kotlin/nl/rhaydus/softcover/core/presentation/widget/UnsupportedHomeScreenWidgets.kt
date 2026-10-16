package nl.rhaydus.softcover.core.presentation.widget

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/** Bound where the platform has no home-screen widgets (iOS, desktop). */
object UnsupportedHomeScreenWidgets : HomeScreenWidgets {
    override val isSupported: Boolean = false

    override fun canPin(): Boolean = false

    override fun observePlaced(): Flow<List<PlacedHomeScreenWidget>> = emptyFlow()

    override fun openSettings(widgetId: Int) = Unit

    override fun requestPin(kind: HomeScreenWidgetKind) = Unit
}
