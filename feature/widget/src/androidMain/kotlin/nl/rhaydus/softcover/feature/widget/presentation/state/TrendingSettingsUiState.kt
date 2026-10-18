package nl.rhaydus.softcover.feature.widget.presentation.state

import nl.rhaydus.softcover.core.component.button.PillButtonContent
import nl.rhaydus.softcover.core.component.button.PillButtonSize
import nl.rhaydus.softcover.core.component.button.PillButtonTone
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeed
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeedKind
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingRefreshCadence
import nl.rhaydus.toad.UiState

internal data class TrendingSettingsUiState(
    val themeConfiguration: ThemeConfiguration = ThemeConfiguration(),
    val topBar: TopBarUiModel = TopBarUiModel(title = "Widget settings"),
    val showHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Show",
        headline = "What should it list?",
    ),
    val periodHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Period",
        headline = "Over which period?",
    ),
    val cadenceHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Refresh",
        headline = "How often should it refresh?",
    ),
    val kindChips: ChipSet<TrendingFeedKind> = ChipSet(),
    val periodChips: ChipSet<TrendingFeed> = ChipSet(),
    val cadenceChips: ChipSet<TrendingRefreshCadence> = ChipSet(),
    val done: PillButtonUiModel = PillButtonUiModel(
        key = "done",
        content = PillButtonContent.Label(text = "Done"),
        tone = PillButtonTone.Filled,
        size = PillButtonSize.Large,
    ),
) : UiState
