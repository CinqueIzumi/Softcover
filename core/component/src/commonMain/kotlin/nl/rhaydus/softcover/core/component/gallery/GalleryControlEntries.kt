package nl.rhaydus.softcover.core.component.gallery

import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.control.ColorPalettePreviewTile
import nl.rhaydus.softcover.core.component.control.ColorPalettePreviewTileUiModel
import nl.rhaydus.softcover.core.component.control.SegmentedControl
import nl.rhaydus.softcover.core.component.control.SegmentedControlUiModel
import nl.rhaydus.softcover.core.component.control.ThemePreviewTile
import nl.rhaydus.softcover.core.component.control.ThemePreviewTileUiModel
import nl.rhaydus.softcover.core.component.control.Toggle
import nl.rhaydus.softcover.core.component.control.ToggleRow
import nl.rhaydus.softcover.core.component.control.ToggleRowUiModel
import nl.rhaydus.softcover.core.component.control.ToggleUiModel
import nl.rhaydus.softcover.core.component.sidebar.Sidebar
import nl.rhaydus.softcover.core.component.sidebar.SidebarUiModel

internal val controlEntries: ImmutableList<GalleryEntry> = persistentListOf(
    galleryEntry(
        name = "ThemePreviewTile",
        family = GalleryFamily.CONTROL,
        blurb = "One choice in the theme picker: the app's own page in miniature, painted in " +
            "the scheme that choice would actually give.",
        previews = ThemePreviewTileUiModel,
        content = { model, modifier ->
            ThemePreviewTile(
                model = model,
                onEvent = {},
                modifier = modifier.width(120.dp),
            )
        },
    ),
    galleryEntry(
        name = "ColorPalettePreviewTile",
        family = GalleryFamily.CONTROL,
        blurb = "One choice in the spine-colour picker: the same miniature, painted in that " +
            "palette entire — its paper as well as its ink.",
        previews = ColorPalettePreviewTileUiModel,
        content = { model, modifier ->
            ColorPalettePreviewTile(
                model = model,
                onEvent = {},
                modifier = modifier.width(120.dp),
            )
        },
    ),
    galleryEntry(
        name = "SegmentedControl",
        family = GalleryFamily.CONTROL,
        blurb = "A one-of-N choice: a full-width pill track, or the options as words in a sentence.",
        previews = SegmentedControlUiModel,
        content = { model, modifier ->
            SegmentedControl(
                model = model,
                onEvent = {},
                modifier = modifier,
            )
        },
    ),
    galleryEntry(
        name = "Toggle",
        family = GalleryFamily.CONTROL,
        blurb = "A two-state control: a filled or bare icon that swaps glyphs.",
        previews = ToggleUiModel,
        content = { model, modifier ->
            Toggle(
                model = model,
                onEvent = {},
                modifier = modifier,
            )
        },
    ),
    galleryEntry(
        name = "ToggleRow",
        family = GalleryFamily.CONTROL,
        blurb = "A labelled switch whose whole row is the tap target; the switch only displays the state.",
        previews = ToggleRowUiModel,
        content = { model, modifier ->
            ToggleRow(
                model = model,
                onEvent = {},
                modifier = modifier,
            )
        },
    ),
    galleryEntry(
        name = "Sidebar",
        family = GalleryFamily.SIDEBAR,
        blurb = "A desktop source list: sections of rows, each with an optional leading icon, " +
            "a selected tint, a trailing arrow, or a long press.",
        previews = SidebarUiModel,
        content = { model, modifier ->
            Sidebar(
                model = model,
                onEvent = {},
                modifier = modifier,
            )
        },
    ),
)
