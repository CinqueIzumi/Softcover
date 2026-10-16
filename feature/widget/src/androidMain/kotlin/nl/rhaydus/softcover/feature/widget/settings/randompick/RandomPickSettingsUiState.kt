package nl.rhaydus.softcover.feature.widget.settings.randompick

import nl.rhaydus.softcover.core.component.button.PillButtonContent
import nl.rhaydus.softcover.core.component.button.PillButtonSize
import nl.rhaydus.softcover.core.component.button.PillButtonTone
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.feature.widget.randompick.RandomPickCadence
import nl.rhaydus.softcover.feature.widget.randompick.RandomPickSource
import nl.rhaydus.toad.UiState

internal data class RandomPickSettingsUiState(
    val themeConfiguration: ThemeConfiguration = ThemeConfiguration(),
    val topBar: TopBarUiModel = TopBarUiModel(title = "Widget settings"),
    val titleHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Random pick",
        headline = "What should it show?",
    ),
    val sourceHeader: SectionHeaderUiModel = SectionHeaderUiModel.Label(eyebrow = "Pick from"),
    val cadenceHeader: SectionHeaderUiModel = SectionHeaderUiModel.Label(eyebrow = "New pick"),
    val sourceChips: ChipSet<RandomPickSource> = ChipSet(),
    val cadenceChips: ChipSet<RandomPickCadence> = ChipSet(),
    val done: PillButtonUiModel = PillButtonUiModel(
        key = "done",
        content = PillButtonContent.Label(text = "Done"),
        tone = PillButtonTone.Filled,
        size = PillButtonSize.Large,
    ),
) : UiState
