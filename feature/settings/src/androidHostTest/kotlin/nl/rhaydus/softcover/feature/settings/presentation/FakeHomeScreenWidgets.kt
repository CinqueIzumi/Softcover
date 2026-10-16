package nl.rhaydus.softcover.feature.settings.presentation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import nl.rhaydus.softcover.core.presentation.widget.HomeScreenWidgetKind
import nl.rhaydus.softcover.core.presentation.widget.HomeScreenWidgets
import nl.rhaydus.softcover.core.presentation.widget.PlacedHomeScreenWidget

internal class FakeHomeScreenWidgets(
    override val isSupported: Boolean = true,
    var canPinNow: Boolean = false,
) : HomeScreenWidgets {
    val placed = MutableSharedFlow<List<PlacedHomeScreenWidget>>()

    override fun canPin(): Boolean = canPinNow

    override fun observePlaced(): Flow<List<PlacedHomeScreenWidget>> = placed

    override fun openSettings(widgetId: Int) = Unit

    override fun requestPin(kind: HomeScreenWidgetKind) = Unit
}
