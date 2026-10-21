package nl.rhaydus.softcover.showcase.appearance.screen

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
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SchemeBrightness
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.showcase.appearance.event.AppearanceSheetEvent

@Composable
internal fun AppearanceSheet(
    model: SheetScaffoldUiModel,
    paletteChips: ChipSet<SpinePalette>,
    brightnessChips: ChipSet<SchemeBrightness>,
    onEvent: (AppearanceSheetEvent) -> Unit,
) {
    SheetScaffold(
        model = model,
        onEvent = { event ->
            if (event == SheetScaffoldEvent.Dismissed) onEvent(AppearanceSheetEvent.Dismissed)
        },
    ) {
        ControlGroup(
            label = "Spine colour",
            chips = paletteChips,
            onSelect = { onEvent(AppearanceSheetEvent.PaletteSelected(palette = it)) },
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        ControlGroup(
            label = "Brightness",
            chips = brightnessChips,
            onSelect = { onEvent(AppearanceSheetEvent.BrightnessSelected(brightness = it)) },
        )
    }
}

@Composable
private fun <T : Any> ControlGroup(
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
