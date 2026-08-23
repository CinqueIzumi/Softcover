package nl.rhaydus.softcover.feature.settings.presentation.state

import nl.rhaydus.softcover.core.component.gallery.GalleryFamily
import nl.rhaydus.softcover.core.domain.model.ColorPalette
import nl.rhaydus.softcover.core.domain.model.ThemeMode
import nl.rhaydus.toad.UiState

/**
 * `null` on [themeModeOverride] and [paletteOverride] means "follow the app's own current setting" —
 * that is what lets the gallery open in the reader's own theme and step off it deliberately, rather
 * than forcing a fixed preview theme on entry.
 */
internal data class ComponentGalleryUiState(
    val selectedFamily: GalleryFamily? = null,
    val themeModeOverride: ThemeMode? = null,
    val paletteOverride: ColorPalette? = null,
) : UiState
