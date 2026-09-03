package nl.rhaydus.softcover.feature.settings.presentation.state

import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.gallery.GalleryFamily
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette
import nl.rhaydus.softcover.core.domain.model.ColorPalette
import nl.rhaydus.softcover.core.domain.model.ThemeMode
import nl.rhaydus.softcover.feature.settings.presentation.mapper.galleryPaletteChoicesFor
import nl.rhaydus.softcover.feature.settings.presentation.model.GalleryPaletteChoice
import nl.rhaydus.toad.UiState

/**
 * `null` on [themeModeOverride] and [paletteOverride] means "follow the app's own current setting" —
 * that is what lets the gallery open in the reader's own theme and step off it deliberately, rather
 * than forcing a fixed preview theme on entry.
 *
 * [currentPalette] mirrors the app's own live spine colour — kept in step by
 * [nl.rhaydus.softcover.feature.settings.presentation.collector.GalleryThemeConfigurationCollector] —
 * and [resolvedPalette] is [paletteOverride] mapped, or [currentPalette] when there is no override:
 * the value [nl.rhaydus.softcover.feature.settings.presentation.screen.ComponentGalleryContent]'s
 * themed region actually paints. Both are mapped off the composition (`component-contract.md` R9),
 * so the render never calls `toSpinePalette()` itself. [paletteChipOptions] is the spine-colour
 * override row's chip list, mapped the same way.
 */
internal data class ComponentGalleryUiState(
    val selectedFamily: GalleryFamily? = null,
    val themeModeOverride: ThemeMode? = null,
    val paletteOverride: ColorPalette? = null,
    val currentPalette: SpinePalette = SpinePalette.DEFAULT,
    val resolvedPalette: SpinePalette = SpinePalette.DEFAULT,
    val paletteChipOptions: ImmutableList<GalleryPaletteChoice> = galleryPaletteChoicesFor(selected = null),
) : UiState
