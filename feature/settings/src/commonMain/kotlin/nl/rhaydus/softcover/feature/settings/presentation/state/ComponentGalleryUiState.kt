package nl.rhaydus.softcover.feature.settings.presentation.state

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toPersistentMap
import nl.rhaydus.softcover.core.component.gallery.GalleryFamily
import nl.rhaydus.softcover.core.component.gallery.GalleryRegistry
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette
import nl.rhaydus.softcover.core.domain.model.ColorPalette
import nl.rhaydus.softcover.core.domain.model.ThemeMode
import nl.rhaydus.softcover.feature.settings.presentation.mapper.galleryPaletteChoicesFor
import nl.rhaydus.softcover.feature.settings.presentation.model.GalleryPaletteChoice
import nl.rhaydus.toad.UiState

private const val COMPONENT_GALLERY_TITLE = "Component gallery"

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
    val previewControlsHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Preview controls",
        headline = "See it in every light",
    ),

    val familySectionHeaders: ImmutableMap<GalleryFamily, SectionHeaderUiModel> = GalleryRegistry.families
        .associateWith { family ->
            SectionHeaderUiModel.Section(
                eyebrow = "Family",
                headline = family.label,
            )
        }
        .toPersistentMap(),
    val selectedFamily: GalleryFamily? = null,
    val themeModeOverride: ThemeMode? = null,
    val paletteOverride: ColorPalette? = null,
    val currentPalette: SpinePalette = SpinePalette.DEFAULT,
    val resolvedPalette: SpinePalette = SpinePalette.DEFAULT,
    val paletteChipOptions: ImmutableList<GalleryPaletteChoice> = galleryPaletteChoicesFor(selected = null),
    val topBar: TopBarUiModel = TopBarUiModel(
        title = COMPONENT_GALLERY_TITLE,
    ),
) : UiState
