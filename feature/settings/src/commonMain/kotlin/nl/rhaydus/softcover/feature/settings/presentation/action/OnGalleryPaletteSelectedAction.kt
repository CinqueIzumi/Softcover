package nl.rhaydus.softcover.feature.settings.presentation.action

import nl.rhaydus.softcover.core.domain.model.ColorPalette
import nl.rhaydus.softcover.core.uibinding.theme.toSpinePalette
import nl.rhaydus.softcover.feature.settings.presentation.event.ComponentGalleryEvent
import nl.rhaydus.softcover.feature.settings.presentation.mapper.galleryPaletteChoicesFor
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.ComponentGalleryDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.ComponentGalleryLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.ComponentGalleryUiState
import nl.rhaydus.toad.ActionScope

/**
 * Overrides the gallery's rendered spine colour with [palette], so a component can be previewed
 * against a palette other than the reader's own current setting. Selecting the already-selected
 * palette clears the override back to `null` (tap-to-toggle-off), returning the gallery to the
 * app's own palette without a separate "reset" action. [resolvedPalette][ComponentGalleryUiState.resolvedPalette]
 * and [paletteChipOptions][ComponentGalleryUiState.paletteChipOptions] are remapped here too, off the
 * composition (`component-contract.md` R9).
 */
internal class OnGalleryPaletteSelectedAction(val palette: ColorPalette) : ComponentGalleryAction {
    override suspend fun execute(
        dependencies: ComponentGalleryDependencies,
        scope: ActionScope<ComponentGalleryUiState, ComponentGalleryEvent, ComponentGalleryLocalVariables>,
    ) {
        scope.setState {
            val nextOverride = if (it.paletteOverride == palette) null else palette

            it.copy(
                paletteOverride = nextOverride,
                resolvedPalette = nextOverride?.toSpinePalette() ?: it.currentPalette,
                paletteChipOptions = galleryPaletteChoicesFor(selected = nextOverride),
            )
        }
    }
}
