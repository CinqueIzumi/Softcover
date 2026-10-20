package nl.rhaydus.softcover.showcase.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.component.chip.ChipFlow
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.gallery.GalleryThemedRegion
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.showcase.action.OnBrightnessSelectedAction
import nl.rhaydus.softcover.showcase.action.OnFamilySelectedAction
import nl.rhaydus.softcover.showcase.action.OnPaletteSelectedAction
import nl.rhaydus.softcover.showcase.action.ShowcaseAction
import nl.rhaydus.softcover.showcase.state.ShowcaseUiState

/**
 * The showcase's one screen: a palette, brightness and family switcher in the default look, above the
 * component gallery painted in the chosen one.
 */
@Composable
internal fun ShowcaseRoot(
    state: ShowcaseUiState,
    runAction: (ShowcaseAction) -> Unit,
) {
    SoftcoverTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .safeDrawingPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.xl),
            ) {
                SoftcoverText(
                    text = "Every component, in any spine colour and any brightness.",
                    style = MaterialTheme.editorialTypography.body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                ControlRow(
                    label = "Spine colour",
                    chips = state.paletteChips,
                    onSelect = { runAction(OnPaletteSelectedAction(palette = it)) },
                )

                Spacer(modifier = Modifier.height(Spacing.lg))

                ControlRow(
                    label = "Brightness",
                    chips = state.brightnessChips,
                    onSelect = { runAction(OnBrightnessSelectedAction(brightness = it)) },
                )

                Spacer(modifier = Modifier.height(Spacing.lg))

                ControlRow(
                    label = "Family",
                    chips = state.familyChips,
                    onSelect = { runAction(OnFamilySelectedAction(family = it)) },
                )

                Spacer(modifier = Modifier.height(Spacing.section))

                GalleryThemedRegion(
                    palette = state.selectedPalette,
                    brightness = state.selectedBrightness,
                    sectionHeaders = state.sectionHeaders,
                    family = state.selectedFamily,
                )
            }
        }
    }
}

@Composable
private fun <T : Any> ControlRow(
    label: String,
    chips: ChipSet<T>,
    onSelect: (T) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SoftcoverText(
            text = label.uppercase(),
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        ChipFlow(
            set = chips,
            onEvent = { event, option ->
                if (event is ChipEvent.Clicked) onSelect(option)
            },
        )
    }
}
