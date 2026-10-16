package nl.rhaydus.softcover.feature.settings.presentation.state

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.feature.settings.presentation.mapper.addWidgetRowsFor
import nl.rhaydus.softcover.feature.settings.presentation.model.AddWidgetRowUiModel
import nl.rhaydus.softcover.feature.settings.presentation.model.PlacedWidgetRowUiModel
import nl.rhaydus.toad.UiState

private const val WIDGETS_TITLE = "Widgets"

internal data class WidgetsSettingsUiState(
    val topBar: TopBarUiModel = TopBarUiModel(title = WIDGETS_TITLE),
    val masthead: PageMastheadUiModel = PageMastheadUiModel(
        eyebrow = "Personalisation",
        title = WIDGETS_TITLE,
    ),
    val placedHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Placed",
        headline = "On your home screen",
    ),
    val placedRows: ImmutableList<PlacedWidgetRowUiModel> = persistentListOf(),
    val emptyPlacedGloss: String = "Nothing on your home screen yet.",
    val addHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Available",
        headline = "Add a widget",
    ),
    val addRows: ImmutableList<AddWidgetRowUiModel> = addWidgetRowsFor(),
    val canPin: Boolean = false,
    val pinUnsupportedGloss: String = "Long-press your home screen and choose Widgets to add one.",
) : UiState
