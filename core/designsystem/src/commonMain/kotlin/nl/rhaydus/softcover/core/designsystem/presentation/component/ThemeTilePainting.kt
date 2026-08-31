package nl.rhaydus.softcover.core.designsystem.presentation.component

/**
 * Which of the light/dark pair a [ThemePreviewTile] paints.
 *
 * One enum rather than two booleans because "dark" and "split down the diagonal" are mutually
 * exclusive: a flat `darkTheme` + `split` pair would let a caller ask for both. The tile takes this
 * instead of the reader's stored `ThemeMode` so the design system stays free of the preference layer
 * — the Appearance screen decides that a `SYSTEM` choice is the [SPLIT] painting.
 */
enum class ThemeTilePainting {
    LIGHT,
    DARK,

    /** Light above a bottom-left-to-top-right seam, dark below — "whichever your device is". */
    SPLIT,
}
