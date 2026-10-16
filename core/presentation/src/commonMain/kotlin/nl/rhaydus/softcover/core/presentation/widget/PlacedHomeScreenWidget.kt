package nl.rhaydus.softcover.core.presentation.widget

/**
 * One home-screen widget instance. [summary] is that instance's configuration in words, null when the
 * kind has no configuration.
 */
data class PlacedHomeScreenWidget(
    val id: Int,
    val kind: HomeScreenWidgetKind,
    val summary: String? = null,
    val hasSettings: Boolean,
)
