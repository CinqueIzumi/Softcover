package nl.rhaydus.softcover.feature.settings.presentation.state

import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.callout.CalloutUiModel
import nl.rhaydus.softcover.core.component.control.ColorPalettePreviewTileUiModel
import nl.rhaydus.softcover.core.component.control.ThemePreviewTileUiModel
import nl.rhaydus.softcover.core.component.control.ToggleRowUiModel
import nl.rhaydus.softcover.core.component.header.PageMastheadSize
import nl.rhaydus.softcover.core.component.header.PageMastheadSubtitle
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sidebar.SidebarUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette
import nl.rhaydus.softcover.core.domain.model.ColorPalette
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.ThemeMode
import nl.rhaydus.softcover.core.domain.model.UiScale
import nl.rhaydus.softcover.feature.settings.presentation.mapper.dateStyleRowsFor
import nl.rhaydus.softcover.feature.settings.presentation.mapper.displayToggleRowsFor
import nl.rhaydus.softcover.feature.settings.presentation.mapper.dynamicColorSlotFor
import nl.rhaydus.softcover.feature.settings.presentation.mapper.paletteChoicesFor
import nl.rhaydus.softcover.feature.settings.presentation.mapper.settingsSidebarFor
import nl.rhaydus.softcover.feature.settings.presentation.mapper.spineColourSectionFor
import nl.rhaydus.softcover.feature.settings.presentation.mapper.themeChoicesFor
import nl.rhaydus.softcover.feature.settings.presentation.mapper.uiScaleRowsFor
import nl.rhaydus.softcover.feature.settings.presentation.model.DisplayToggle
import nl.rhaydus.softcover.feature.settings.presentation.model.DynamicColorSlotUiModel
import nl.rhaydus.softcover.feature.settings.presentation.model.SettingsChoiceRowUiModel
import nl.rhaydus.softcover.feature.settings.presentation.model.SettingsSidebarKey
import nl.rhaydus.softcover.feature.settings.presentation.model.SpineColourSectionUiModel
import nl.rhaydus.softcover.feature.settings.presentation.screen.SettingsCategory
import nl.rhaydus.softcover.feature.settings.presentation.util.settingsDisplayToggleAvailability
import nl.rhaydus.toad.UiState

private const val ABOUT_TITLE = "About"
private const val APPEARANCE_TITLE = "Appearance"

internal data class SettingsScreenUiState(
    /** Mobile only; desktop uses [settingsSidebarMasthead] instead. */
    val settingsPageMasthead: PageMastheadUiModel = PageMastheadUiModel(
        title = "Settings",
        subtitle = PageMastheadSubtitle.Text("Set Softcover up the way you like it."),
    ),

    /** Desktop only; mobile uses [settingsPageMasthead] instead. */
    val settingsSidebarMasthead: PageMastheadUiModel = PageMastheadUiModel(
        title = "Settings",
        subtitle = PageMastheadSubtitle.Text("Set Softcover up the way you like it."),
        size = PageMastheadSize.Compact,
    ),

    val selectedCategory: SettingsCategory = SettingsCategory.APPEARANCE,

    val settingsSidebar: SidebarUiModel<SettingsSidebarKey> = settingsSidebarFor(selectedCategory = selectedCategory),

    val settingsMenuAccountHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Account",
        headline = "Your account",
    ),
    val settingsMenuPersonalisationHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Personalisation",
        headline = "Look and feel",
    ),
    val settingsMenuAboutHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "About",
        headline = "The fine print",
    ),

    val appUpdateCallout: CalloutUiModel? = null,

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
        headline = "Open source",
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
        eyebrow = "Personalisation",
        title = "Appearance",
        subtitle = PageMastheadSubtitle.Text("Change how Softcover looks."),
    ),
    val themeSectionHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Theme",
        headline = "Light, dark or dim",
    ),
    val themeGloss: String =
        "Dim is a warmer, softer dark for reading at night. System follows your device's light and dark setting.",
    val spineColourSectionHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Spine colour",
        headline = "Paper and ink",
    ),
    val uiScaleSectionHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Display scale",
        headline = "Text & interface size",
        description = "Scales all text and controls. Try a larger size if the app looks too small on your display.",
    ),
    val dateStyleSectionHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Date notation",
        headline = "How dates are written",
    ),
    val displaySectionHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Display",
        headline = "Display",
    ),
    val themeMode: ThemeMode = ThemeMode.DEFAULT,
    val colorPalette: ColorPalette = ColorPalette.DEFAULT,
    val displayToggleRows: ImmutableList<ToggleRowUiModel<DisplayToggle>> = settingsDisplayToggleAvailability().let { availability ->
        displayToggleRowsFor(
            showFloatingBarToggle = availability.showsFloatingBarToggle,
            showShelfSwipeToggle = availability.showsShelfSwipeToggle,
            useFloatingBarChecked = true,
            shelfSwipeEnabledChecked = false,
            readingStreakEnabledChecked = true,
        )
    },
    val dynamicColorSlot: DynamicColorSlotUiModel = dynamicColorSlotFor(
        themeMode = ThemeMode.DEFAULT,
        supportsDynamicColor = settingsDisplayToggleAvailability().showsDynamicColorToggle,
        useDynamicColor = false,
    ),
    val spineColourSection: SpineColourSectionUiModel = spineColourSectionFor(
        themeMode = ThemeMode.DEFAULT,
        useDynamicColor = false,
        paletteGloss = SpinePalette.DEFAULT.gloss,
    ),
    val dateStyleRows: ImmutableList<SettingsChoiceRowUiModel<DateStyle>> = dateStyleRowsFor(
        examples = emptyMap(),
        selected = DateStyle.DAY_MONTH_YEAR,
    ),
    val appVersionName: String = "",
    val appVersionCode: Int = 0,
    val versionLabel: String = "Version $appVersionName ($appVersionCode)",
    val uiScaleRows: ImmutableList<SettingsChoiceRowUiModel<UiScale>> = uiScaleRowsFor(selected = UiScale.DEFAULT),
    val spinePalette: SpinePalette = SpinePalette.DEFAULT,
    val themeChoices: ImmutableList<ThemePreviewTileUiModel<ThemeMode>> = themeChoicesFor(
        selected = ThemeMode.DEFAULT,
        palette = SpinePalette.DEFAULT,
        dynamicColor = false,
    ),
    val paletteChoices: ImmutableList<ColorPalettePreviewTileUiModel<ColorPalette>> = paletteChoicesFor(selected = ColorPalette.DEFAULT),
    val aboutTopBar: TopBarUiModel = TopBarUiModel(
        title = ABOUT_TITLE,
    ),
    val appearanceTopBar: TopBarUiModel = TopBarUiModel(
        title = APPEARANCE_TITLE,
    ),
) : UiState
