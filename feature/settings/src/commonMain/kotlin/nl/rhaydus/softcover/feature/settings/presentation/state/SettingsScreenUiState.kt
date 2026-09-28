package nl.rhaydus.softcover.feature.settings.presentation.state

import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.header.PageMastheadSize
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette
import nl.rhaydus.softcover.core.domain.model.ColorPalette
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.ThemeMode
import nl.rhaydus.softcover.core.domain.model.UiScale
import nl.rhaydus.softcover.feature.settings.presentation.mapper.paletteChoicesFor
import nl.rhaydus.softcover.feature.settings.presentation.mapper.themeChoicesFor
import nl.rhaydus.softcover.feature.settings.presentation.model.PaletteChoice
import nl.rhaydus.softcover.feature.settings.presentation.model.ThemeChoice
import nl.rhaydus.toad.UiState

internal data class SettingsScreenUiState(
    val useFloatingBarChecked: Boolean = true,

    /** Mobile only; desktop uses [settingsSidebarMasthead] instead. */
    val settingsPageMasthead: PageMastheadUiModel = PageMastheadUiModel(
        title = "Settings",
        subtitle = "Tune Softcover to match how you read.",
    ),

    /** Desktop only; mobile uses [settingsPageMasthead] instead. */
    val settingsSidebarMasthead: PageMastheadUiModel = PageMastheadUiModel(
        title = "Settings",
        subtitle = "Tune Softcover to match how you read.",
        size = PageMastheadSize.Compact,
    ),

    val accountSidebarLabel: SectionHeaderUiModel = SectionHeaderUiModel.Label(eyebrow = "Account"),
    val personaliseSidebarLabel: SectionHeaderUiModel = SectionHeaderUiModel.Label(eyebrow = "Personalise"),
    val privacySidebarLabel: SectionHeaderUiModel = SectionHeaderUiModel.Label(eyebrow = "Privacy"),
    val aboutSidebarLabel: SectionHeaderUiModel = SectionHeaderUiModel.Label(eyebrow = "About"),

    val settingsMenuAccountHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Account",
        headline = "Your reader",
    ),
    val settingsMenuPersonalisationHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Personalisation",
        headline = "Make it yours",
    ),
    val settingsMenuAboutHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "About",
        headline = "The fine print",
    ),

    val appUpdateSimulatorHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Debug",
        headline = "App update simulator",
    ),

    val aboutCreditsHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Credits",
        headline = "Powered by Hardcover",
    ),
    val aboutSourceHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Source",
        headline = "Built in the open",
    ),
    val aboutContactHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Contact",
        headline = "Say hello",
    ),

    val aboutPaneMasthead: PageMastheadUiModel = PageMastheadUiModel(
        eyebrow = "About",
        title = "About Softcover",
    ),

    val appearancePaneMasthead: PageMastheadUiModel = PageMastheadUiModel(
        eyebrow = "Personalise",
        title = "Appearance",
        subtitle = "Make Softcover yours.",
    ),
    val themeSectionHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Theme",
        headline = "The coat it wears",
    ),
    val spineColourSectionHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Spine colour",
        headline = "The paper and the ink",
    ),
    val uiScaleSectionHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Display scale",
        headline = "Text & interface size",
        description = "Scales all text and controls. Try a larger size if the app looks too small on your display.",
    ),
    val dateStyleSectionHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Date notation",
        headline = "How dates read",
    ),
    val displaySectionHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Display",
        headline = "The look",
    ),
    val themeMode: ThemeMode = ThemeMode.DEFAULT,
    val colorPalette: ColorPalette = ColorPalette.DEFAULT,
    val useDynamicColorChecked: Boolean = false,
    val readingStreakEnabledChecked: Boolean = true,
    val shelfSwipeEnabledChecked: Boolean = false,
    val userDateStyle: DateStyle = DateStyle.DAY_MONTH_YEAR,
    val dateStyleExamples: Map<DateStyle, String> = emptyMap(),
    val dropDownExpanded: Boolean = false,
    val appVersionName: String = "",
    val appVersionCode: Int = 0,
    val uiScale: UiScale = UiScale.DEFAULT,
    val spinePalette: SpinePalette = SpinePalette.DEFAULT,
    val paletteGloss: String = SpinePalette.DEFAULT.gloss,
    val themeChoices: ImmutableList<ThemeChoice> = themeChoicesFor(
        selected = ThemeMode.DEFAULT,
        palette = SpinePalette.DEFAULT,
        dynamicColor = false,
    ),
    val paletteChoices: ImmutableList<PaletteChoice> = paletteChoicesFor(selected = ColorPalette.DEFAULT),
) : UiState
