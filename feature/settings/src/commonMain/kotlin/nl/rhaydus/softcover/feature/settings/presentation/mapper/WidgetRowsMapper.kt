package nl.rhaydus.softcover.feature.settings.presentation.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.presentation.widget.HomeScreenWidgetKind
import nl.rhaydus.softcover.core.presentation.widget.PlacedHomeScreenWidget
import nl.rhaydus.softcover.feature.settings.presentation.model.AddWidgetRowUiModel
import nl.rhaydus.softcover.feature.settings.presentation.model.PlacedWidgetRowUiModel

private const val NO_SETTINGS_GLOSS = "Nothing to set up"

internal fun placedWidgetRowsFor(widgets: List<PlacedHomeScreenWidget>): ImmutableList<PlacedWidgetRowUiModel> =
    widgets.map { widget ->
        PlacedWidgetRowUiModel(
            widgetId = widget.id,
            title = widget.kind.title(),
            gloss = widget.summary ?: NO_SETTINGS_GLOSS,
            icon = widget.kind.icon(),
            hasSettings = widget.hasSettings,
        )
    }.toImmutableList()

internal fun addWidgetRowsFor(): ImmutableList<AddWidgetRowUiModel> = HomeScreenWidgetKind.entries.map { kind ->
    AddWidgetRowUiModel(
        kind = kind,
        title = kind.title(),
        gloss = kind.description(),
        icon = kind.icon(),
    )
}.toImmutableList()

private fun HomeScreenWidgetKind.title(): String = when (this) {
    HomeScreenWidgetKind.CurrentlyReading -> "Currently reading"
    HomeScreenWidgetKind.RandomPick -> "Random pick"
    HomeScreenWidgetKind.Trending -> "Trending"
    HomeScreenWidgetKind.Quote -> "Quote of the day"
}

private fun HomeScreenWidgetKind.description(): String = when (this) {
    HomeScreenWidgetKind.CurrentlyReading ->
        "The book you are reading now, with your progress and a way back to your place."

    HomeScreenWidgetKind.RandomPick -> "A book from your shelves, chosen for you."

    HomeScreenWidgetKind.Trending -> "What readers are picking up, ranked, two books at a time."

    HomeScreenWidgetKind.Quote -> "One of your saved quotes each day, with arrows to browse the rest."
}

private fun HomeScreenWidgetKind.icon(): SoftcoverIcon = when (this) {
    HomeScreenWidgetKind.CurrentlyReading -> SoftcoverIcon.Reading
    HomeScreenWidgetKind.RandomPick -> SoftcoverIcon.MenuBook
    HomeScreenWidgetKind.Trending -> SoftcoverIcon.StarFilled
    HomeScreenWidgetKind.Quote -> SoftcoverIcon.Bookmark
}
