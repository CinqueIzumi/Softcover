package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.component.chip.ChipFlow
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.gallery.GalleryFamily
import nl.rhaydus.softcover.core.component.gallery.GalleryThemedRegion
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.presentation.theme.LocalThemeConfiguration
import nl.rhaydus.softcover.core.uibinding.theme.toSchemeBrightness
import nl.rhaydus.softcover.feature.settings.presentation.action.ComponentGalleryAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnGalleryFamilySelectedAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnGalleryPaletteSelectedAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnGalleryThemeModeSelectedAction
import nl.rhaydus.softcover.feature.settings.presentation.state.ComponentGalleryUiState
// region Gallery content
/**
 * The Component Gallery screen body, shared by the mobile [ComponentGalleryScreen] page and the
 * desktop standalone fallback (`component-contract.md` § 7.5). Opens with the two override chip rows
 * (brightness, spine colour) and a family filter row — all three stay in the app's own current look,
 * since they are chrome, not previewed content. Everything from
 * [GalleryThemedRegion] down renders inside a [SoftcoverTheme] resolved from [state]'s overrides, so
 * only the previewed material steps into the chosen brightness/palette.
 */
@Composable
internal fun ComponentGalleryContent(
    state: ComponentGalleryUiState,
    runAction: (ComponentGalleryAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SoftcoverText(
            text = "Every component in the library, rendered from its own preview fixtures — in any " +
                "brightness, and any spine colour.",
            style = MaterialTheme.editorialTypography.body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(Spacing.xxxl))

        SectionHeader(model = state.previewControlsHeader)

        Spacer(modifier = Modifier.height(Spacing.md))

        GalleryOverrideControls(
            state = state,
            runAction = runAction,
        )

        Spacer(modifier = Modifier.height(Spacing.xl))

        GalleryFamilyFilterRow(
            familyChips = state.familyChips,
            runAction = runAction,
        )

        Spacer(modifier = Modifier.height(Spacing.section))

        GalleryPreview(state = state)
    }
}
// endregion
// region Controls
@Composable
private fun GalleryOverrideControls(
    state: ComponentGalleryUiState,
    runAction: (ComponentGalleryAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        GalleryControlLabel(text = "Brightness")

        Spacer(modifier = Modifier.height(Spacing.sm))

        ChipFlow(
            set = state.themeModeChips,
            onEvent = { event, mode ->
                if (event is ChipEvent.Clicked) runAction(OnGalleryThemeModeSelectedAction(mode = mode))
            },
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        GalleryControlLabel(text = "Spine colour")

        Spacer(modifier = Modifier.height(Spacing.sm))

        ChipFlow(
            set = state.paletteChips,
            onEvent = { event, palette ->
                if (event is ChipEvent.Clicked) runAction(OnGalleryPaletteSelectedAction(palette = palette))
            },
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        SoftcoverText(
            text = "A highlighted chip is the override in effect — tap it again to hand the gallery " +
                "back to the app's own current setting.",
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun GalleryFamilyFilterRow(
    familyChips: ChipSet<GalleryFamily>,
    runAction: (ComponentGalleryAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        GalleryControlLabel(text = "Family")

        Spacer(modifier = Modifier.height(Spacing.sm))

        ChipFlow(
            set = familyChips,
            onEvent = { event, family ->
                if (event is ChipEvent.Clicked) runAction(OnGalleryFamilySelectedAction(family = family))
            },
        )
    }
}

/**
 * A bar-less, in-flow sub-label (§2.3's third eyebrow register) for a control living inside the
 * "Preview controls" [SectionHeader] already opened above — never paired with an accent bar of its own.
 */
@Composable
private fun GalleryControlLabel(text: String) {
    SoftcoverText(
        text = text.uppercase(),
        style = MaterialTheme.editorialTypography.eyebrowSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
// endregion
// region Themed region
/**
 * The previewed gallery: [state]'s overrides, each falling back to [LocalThemeConfiguration] (the app's
 * own current setting) where `null`.
 */
@Composable
private fun GalleryPreview(state: ComponentGalleryUiState) {
    val configuration = LocalThemeConfiguration.current

    val themeMode = state.themeModeOverride ?: configuration.themeMode

    // Dynamic colour replaces the chosen palette outright (see SoftcoverTheme's KDoc) — so once a
    // palette override is in effect here, dynamic colour must step aside, or the wallpaper scheme
    // would silently override the override right back.
    GalleryThemedRegion(
        palette = state.resolvedPalette,
        brightness = themeMode.toSchemeBrightness(systemInDark = isSystemInDarkTheme()),
        sectionHeaders = state.familySectionHeaders,
        family = state.selectedFamily,
        dynamicColor = configuration.useDynamicColor && state.paletteOverride == null,
    )
}
// endregion
