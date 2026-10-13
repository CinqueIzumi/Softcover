package nl.rhaydus.softcover.core.uibinding.theme

import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette
import nl.rhaydus.softcover.core.domain.model.ColorPalette

/**
 * The reader's stored spine-colour preference as the design system's palette token.
 *
 * Two enums rather than one is what lets `:core:designsystem` hold zero project dependencies: the
 * domain owns the persisted identifier, the design system owns the hex table and the palette's
 * user-facing name. The `when` is exhaustive on purpose — a sixth [ColorPalette] that has no hex
 * table yet is a compile error here rather than a crash in `softcoverColorScheme`.
 */
fun ColorPalette.toSpinePalette(): SpinePalette = when (this) {
    ColorPalette.SOFTCOVER -> SpinePalette.SOFTCOVER
    ColorPalette.VELLUM -> SpinePalette.VELLUM
    ColorPalette.INK -> SpinePalette.INK
    ColorPalette.FOXED -> SpinePalette.FOXED
    ColorPalette.SEA -> SpinePalette.SEA
}
