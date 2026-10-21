package nl.rhaydus.softcover.showcase.appearance.screen

import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.showcase.appearance.action.OnAppearanceDismissedAction
import nl.rhaydus.softcover.showcase.appearance.action.OnBrightnessSelectedAction
import nl.rhaydus.softcover.showcase.appearance.action.OnPaletteSelectedAction
import nl.rhaydus.softcover.showcase.appearance.event.AppearanceSheetEvent
import nl.rhaydus.softcover.showcase.appearance.screenmodel.AppearanceDependencies
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceState
import nl.rhaydus.softcover.showcase.appearance.state.AppearanceUiState
import nl.rhaydus.toad.ActionDependencies
import nl.rhaydus.toad.LocalVariables
import nl.rhaydus.toad.UiAction
import nl.rhaydus.toad.UiEvent

@Composable
internal fun <D, S : AppearanceUiState<S>, E : UiEvent, V : LocalVariables> AppearanceSheetHost(
    appearance: AppearanceState,
    runAction: (UiAction<D, S, E, V>) -> Unit,
) where D : ActionDependencies, D : AppearanceDependencies {
    appearance.sheet.modelOrNull?.let { sheet ->
        AppearanceSheet(
            model = sheet,
            paletteChips = appearance.paletteChips,
            brightnessChips = appearance.brightnessChips,
            onEvent = { event ->
                when (event) {
                    is AppearanceSheetEvent.PaletteSelected -> runAction(OnPaletteSelectedAction(palette = event.palette))
                    is AppearanceSheetEvent.BrightnessSelected -> runAction(
                        OnBrightnessSelectedAction(brightness = event.brightness),
                    )
                    AppearanceSheetEvent.Dismissed -> runAction(OnAppearanceDismissedAction())
                }
            },
        )
    }
}
