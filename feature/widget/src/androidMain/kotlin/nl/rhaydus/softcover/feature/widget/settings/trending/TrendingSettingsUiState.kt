package nl.rhaydus.softcover.feature.widget.settings.trending

import nl.rhaydus.softcover.core.component.button.PillButtonContent
import nl.rhaydus.softcover.core.component.button.PillButtonSize
import nl.rhaydus.softcover.core.component.button.PillButtonTone
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.feature.widget.trending.TrendingRefreshCadence
import nl.rhaydus.toad.UiState

internal data class TrendingSettingsUiState(
    val themeConfiguration: ThemeConfiguration = ThemeConfiguration(),
    val titleHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Trending this week",
        headline = "How often should it refresh?",
    ),
    val cadenceChips: ChipSet<TrendingRefreshCadence> = ChipSet(),
    val done: PillButtonUiModel = PillButtonUiModel(
        key = "done",
        content = PillButtonContent.Label(text = "Done"),
        tone = PillButtonTone.Filled,
        size = PillButtonSize.Large,
    ),
) : UiState
