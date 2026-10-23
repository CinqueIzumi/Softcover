package nl.rhaydus.softcover.showcase.live.control.mapper

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.control.ColorPalettePreviewTileUiModel
import nl.rhaydus.softcover.core.component.control.SegmentUiModel
import nl.rhaydus.softcover.core.component.control.SegmentedControlUiModel
import nl.rhaydus.softcover.core.component.control.SegmentedControlVariant
import nl.rhaydus.softcover.core.component.control.ThemePreviewTileUiModel
import nl.rhaydus.softcover.core.component.control.ThemeTilePainting
import nl.rhaydus.softcover.core.component.control.ToggleRowUiModel
import nl.rhaydus.softcover.core.component.control.ToggleRowVariant
import nl.rhaydus.softcover.core.component.control.ToggleUiModel
import nl.rhaydus.softcover.core.component.control.ToggleVariant
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette
import nl.rhaydus.softcover.showcase.live.control.model.ControlHeadersUiModel
import nl.rhaydus.softcover.showcase.live.control.model.IconToggleUiModel
import nl.rhaydus.softcover.showcase.live.control.state.ControlLiveUiState

internal fun controlLiveState(): ControlLiveUiState = ControlLiveUiState(
    topBar = TopBarUiModel(title = "Controls"),
    headers = ControlHeadersUiModel(
        toggles = SectionHeaderUiModel.Section(
            eyebrow = "Toggle",
            description = "A glyph that swaps when you flip it, filled or bare.",
        ),
        rows = SectionHeaderUiModel.Section(
            eyebrow = "Toggle row",
            description = "The whole row flips the switch, in plain and inset spacing.",
        ),
        segments = SectionHeaderUiModel.Section(
            eyebrow = "Segmented control",
            description = "One option at a time, as a track or set into a sentence.",
        ),
        themes = SectionHeaderUiModel.Section(
            eyebrow = "Theme tiles",
            description = "Miniature pages that stand for a brightness.",
        ),
        palettes = SectionHeaderUiModel.Section(
            eyebrow = "Palette tiles",
            description = "Miniature pages led by each accent.",
        ),
    ),
    iconToggles = persistentListOf(
        IconToggleUiModel(
            key = FILLED,
            toggle = ToggleUiModel(
                checked = true,
                description = "Bookmark",
                variant = ToggleVariant.Filled,
                on = SoftcoverIcon.BookmarkAdded,
                off = SoftcoverIcon.BookmarkAdd,
            ),
        ),
        IconToggleUiModel(
            key = BARE,
            toggle = ToggleUiModel(
                checked = false,
                description = "Show spoilers",
                variant = ToggleVariant.Bare,
                on = SoftcoverIcon.Visibility,
                off = SoftcoverIcon.VisibilityOff,
            ),
        ),
    ),
    toggleRows = persistentListOf(
        ToggleRowUiModel(
            key = "reminders",
            title = "Reading reminders",
            gloss = "A quiet nudge at the end of the day.",
            checked = true,
        ),
        ToggleRowUiModel(
            key = "awake",
            title = "Keep the screen awake",
            gloss = null,
            checked = false,
            variant = ToggleRowVariant.Inset,
        ),
    ),
    trackSegments = SegmentedControlUiModel(
        options = persistentListOf(
            SegmentUiModel(
                key = "list",
                label = "List",
            ),
            SegmentUiModel(
                key = "grid",
                label = "Grid",
            ),
            SegmentUiModel(
                key = "shelf",
                label = "Shelf",
            ),
        ),
        selected = "list",
    ),
    proseSegments = SegmentedControlUiModel(
        options = persistentListOf(
            SegmentUiModel(
                key = "public",
                label = "public",
            ),
            SegmentUiModel(
                key = "friends",
                label = "friends only",
            ),
            SegmentUiModel(
                key = "private",
                label = "private",
            ),
        ),
        selected = "private",
        variant = SegmentedControlVariant.Prose(
            prefix = "This shelf is ",
            suffix = ".",
        ),
    ),
    themeTiles = themeTiles(selected = ThemeTilePainting.LIGHT),
    paletteTiles = paletteTiles(selected = SpinePalette.entries.first()),
)

internal fun themeTiles(selected: ThemeTilePainting) = listOf(
    ThemeTilePainting.LIGHT to "Light",
    ThemeTilePainting.DARK to "Dark",
    ThemeTilePainting.DIM to "Dim",
    ThemeTilePainting.SPLIT to "Auto",
)
    .map { (painting, label) ->
        ThemePreviewTileUiModel(
            key = painting,
            label = label,
            painting = painting,
            selected = painting == selected,
            palette = SpinePalette.entries.first(),
            dynamicColor = false,
        )
    }
    .toImmutableList()

internal fun paletteTiles(selected: SpinePalette) = SpinePalette.entries
    .take(4)
    .map {
        ColorPalettePreviewTileUiModel(
            key = it,
            palette = it,
            selected = it == selected,
        )
    }
    .toImmutableList()

private const val FILLED = "filled"
private const val BARE = "bare"
